package com.example.trabajo_final

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class FragmentEditarMascota : Fragment(), OnBackPressedInFragmentListener {

    private lateinit var mascota: Mascota
    private lateinit var mascotaImageView: ImageView
    private var mascotaPic: Uri? = null
    private val accesoGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                mascotaPic = uri
                mascotaImageView.setImageURI(uri)
            }
        }
    private val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
        if (isSuccess) {
            mascotaImageView.setImageURI(mascotaPic)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_mascota, container, false)

        val nombre = view.findViewById<EditText>(R.id.nombre)
        val edad = view.findViewById<EditText>(R.id.edad)
        val raza = view.findViewById<Spinner>(R.id.raza)
        val sexo = view.findViewById<Spinner>(R.id.sexo)
        val esterilizado = view.findViewById<CheckBox>(R.id.esterilizado)
        val biografia = view.findViewById<EditText>(R.id.biografia)
        val guardarDatos = view.findViewById<Button>(R.id.editarMascota)
        mascotaImageView = view.findViewById(R.id.imagenMascota)

        mascota = arguments?.getParcelable("mascota")!!

        nombre.setText(mascota.nombre)
        edad.setText(mascota.edad.toString())
        raza.setSelection(resources.getStringArray(R.array.razas_array).indexOf(mascota.raza))
        sexo.setSelection(resources.getStringArray(R.array.sexo_array).indexOf(mascota.sexo))
        esterilizado.isChecked = mascota.esterilizado!!
        biografia.setText(mascota.biografia)
        if (!mascota.foto.isNullOrEmpty()) {
            mascotaPic = Uri.parse(mascota.foto)
            Glide.with(this)
                .load(mascotaPic)
                .placeholder(Utilidades.animacion_carga(requireContext()))
                .transform(CircleCrop())
                .into(mascotaImageView)
                .apply { Utilidades.opcionesGlide(requireContext()) }
        }

        mascotaImageView.setOnClickListener {
            mostrarDialogoSeleccion()
        }

        guardarDatos.setOnClickListener {
            if (nombre.text.toString().isEmpty() || edad.text.toString().isEmpty() || biografia.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val edadInt = edad.text.toString().toIntOrNull()
            if (edadInt == null) {
                Toast.makeText(requireContext(), "La edad debe ser un número entero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (edadInt < 0) {
                Toast.makeText(requireContext(), "La edad no puede ser negativa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (raza.selectedItem.toString() == "Seleccione raza:") {
                Toast.makeText(requireContext(), "Por favor, selecciona una raza", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sexo.selectedItem.toString() == "Seleccione sexo:") {
                Toast.makeText(requireContext(), "Por favor, selecciona un sexo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valoracion = mascota.valoracion
            val mascotaRef = FirebaseDatabase.getInstance()
                .getReference("app/usuarios/${mascota.usuarioId}/mascotas/${mascota.id}")
            val stRef =
                FirebaseStorage.getInstance().getReference("app/usuarios/${mascota.usuarioId}/mascotas")

            CoroutineScope(Dispatchers.IO).launch {
                val mascotaPicUrl = if (mascotaPic != null && "content" == mascotaPic!!.scheme) {
                    val mascotaPicRef = stRef.child("${mascota.id}.jpg")
                    mascotaPicRef.putFile(mascotaPic!!).await()
                    mascotaPicRef.downloadUrl.await().toString()
                } else {
                    // Si la imagen de la mascota ha sido eliminada, eliminar la imagen de la base de datos
                    if (mascotaPic == null && mascota.foto != null) {
                        val mascotaPicRef = stRef.child("${mascota.id}.jpg")
                        try {
                            mascotaPicRef.metadata.await()
                            mascotaPicRef.delete().await()
                        } catch (e: StorageException) {
                            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error al eliminar la imagen de la mascota",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        null
                    } else {
                        mascota.foto
                    }
                }

                // actualizar la mascota en la base de datos con setValue
                mascotaRef.setValue(mascota.copy(
                    nombre = nombre.text.toString().lowercase(),
                    edad = edadInt,
                    raza = raza.selectedItem.toString(),
                    sexo = sexo.selectedItem.toString(),
                    esterilizado = esterilizado.isChecked,
                    biografia = biografia.text.toString(),
                    foto = mascotaPicUrl,
                    valoracion = valoracion
                )).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Mascota actualizada", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }

                // Recorrer todos los anuncios en la base de datos y actualizar los atributos imagenMascota, nombreMascota y razaMascota de la mascota asociada
                val anunciosRef = FirebaseDatabase.getInstance().getReference("app/anuncios")
                CoroutineScope(Dispatchers.IO).launch {
                    val anunciosSnapshot = anunciosRef.get().await()
                    for (anuncioSnapshot in anunciosSnapshot.children) {
                        val anuncio = anuncioSnapshot.getValue(Anuncio::class.java)
                        if (anuncio?.idmascota?.contains(mascota.id) == true) {
                            val anuncioId = anuncio.id
                            if (anuncioId != null) {
                                val anuncioRef = anunciosRef.child(anuncioId)
                                val mutableNombreMascota = anuncio.nombreMascota?.toMutableList()
                                val mutableRazaMascota = anuncio.razaMascota?.toMutableList()
                                val mutableEdadMascota = anuncio.edadMascota?.toMutableList()
                                val mutableImagenMascota = anuncio.imagenMascota?.toMutableList()

                                // Buscar la mascota correcta en la lista de mascotas del anuncio
                                val mascotaIndex = anuncio.idmascota!!.indexOf(mascota.id)
                                mutableNombreMascota?.set(mascotaIndex, nombre.text.toString())
                                mutableRazaMascota?.set(mascotaIndex, raza.selectedItem.toString())
                                if (mascotaPicUrl != null) {
                                    mutableImagenMascota?.set(mascotaIndex, mascotaPicUrl)
                                } else {
                                    mutableImagenMascota?.set(mascotaIndex, "")
                                }

                                val anuncioInfoUpdate = mapOf(
                                    "nombreMascota" to mutableNombreMascota,
                                    "razaMascota" to mutableRazaMascota,
                                    "edadMascota" to mutableEdadMascota,
                                    "imagenMascota" to mutableImagenMascota
                                )

                                anuncioRef.updateChildren(anuncioInfoUpdate)
                            } else{
                                Log.e("FragmentEditarMascota", "AnuncioId es null")
                            }
                        }
                    }
                }
            }
        }

        return view
    }

    private fun mostrarDialogoSeleccion() {
        //obtener el tipo de usuario por sharedPreferences
        val sharedPref = activity?.getSharedPreferences("userRole", 0)
        val userRole = sharedPref?.getString("role", "usuario")
        val opciones = if (userRole == "admin") {
            arrayOf("Tomar foto", "Seleccionar de galería", "Eliminar foto")
        } else {
            arrayOf("Tomar foto", "Seleccionar de galería")
        }
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Elige una opción")
        builder.setItems(opciones) { _, which ->
            when (which) {
                0 -> tomarFoto()
                1 -> seleccionarDeGaleria()
                2 -> eliminarFoto()
            }
        }
        builder.show()
    }

    private fun tomarFoto() {
        val archivo = File.createTempFile("foto", ".jpg", requireContext().cacheDir)
        mascotaPic = FileProvider.getUriForFile(requireContext(), "com.example.trabajo_final.fileprovider", archivo)
        tomarFoto.launch(mascotaPic)
    }

    private fun seleccionarDeGaleria() {
        accesoGaleria.launch("image/*")
    }

    private fun eliminarFoto() {
        mascotaPic = null
        mascotaImageView.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun onBackPressedInFragment(): Boolean {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que quieres salir?")
            .setPositiveButton("Sí") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("No", null)
            .show()
        return true
    }
}