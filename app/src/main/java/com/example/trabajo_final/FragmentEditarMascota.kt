package com.example.trabajo_final

import android.net.Uri
import android.os.Bundle
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
                .transform(CircleCrop())
                .into(mascotaImageView)
        }

        mascotaImageView.setOnClickListener {
            mostrarDialogoSeleccion()
        }

        guardarDatos.setOnClickListener {
            if (validarDatos()) {
                guardarDatos()
            }
        }

        return view
    }

    private fun guardarDatos() {
        val nombre = view?.findViewById<EditText>(R.id.nombre)?.text.toString()
        val edad = view?.findViewById<EditText>(R.id.edad)?.text.toString().toInt()
        val raza = view?.findViewById<Spinner>(R.id.raza)?.selectedItem.toString()
        val sexo = view?.findViewById<Spinner>(R.id.sexo)?.selectedItem.toString()
        val esterilizado = view?.findViewById<CheckBox>(R.id.esterilizado)?.isChecked
        val biografia = view?.findViewById<EditText>(R.id.biografia)?.text.toString()

        val mascotaRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${mascota.usuarioId}/mascotas/${mascota.id}")
        val stRef = FirebaseStorage.getInstance().getReference("app/usuarios/${mascota.usuarioId}/mascotas")

        CoroutineScope(Dispatchers.IO).launch {
            val mascotaPicUrl = if (mascotaPic != null) {
                val mascotaPicRef = stRef.child("${mascota.id}.jpg")
                mascotaPicRef.putFile(mascotaPic!!).await()
                mascotaPicRef.downloadUrl.await().toString()
            } else {
                mascota.foto
            }

            val mascotaInfoUpdate = mapOf("nombre" to nombre, "edad" to edad, "raza" to raza, "sexo" to sexo, "esterilizado" to esterilizado, "biografia" to biografia, "foto" to mascotaPicUrl)

            mascotaRef.updateChildren(mascotaInfoUpdate).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Datos de la mascota actualizados con éxito", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar los datos de la mascota", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarDialogoSeleccion() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Elige una opción")
        builder.setItems(opciones) { _, which ->
            when (which) {
                0 -> tomarFoto()
                1 -> seleccionarDeGaleria()
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

    private fun validarDatos(): Boolean {
        val nombre = view?.findViewById<EditText>(R.id.nombre)?.text.toString()
        val edadStr = view?.findViewById<EditText>(R.id.edad)?.text.toString()
        val raza = view?.findViewById<Spinner>(R.id.raza)?.selectedItem.toString()
        val sexo = view?.findViewById<Spinner>(R.id.sexo)?.selectedItem.toString()
        val biografia = view?.findViewById<EditText>(R.id.biografia)?.text.toString()

        if (nombre.isEmpty() || edadStr.isEmpty() || raza.isEmpty() || sexo.isEmpty() || biografia.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (mascotaPic == null) {
            Toast.makeText(requireContext(), "Por favor, seleccione una foto", Toast.LENGTH_SHORT).show()
            return false
        }

        if ( edadStr.toInt() < 0 ) {
            Toast.makeText(requireContext(), "La edad no puede ser negativa", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!edadStr.all { it.isDigit() }) {
            Toast.makeText(requireContext(), "La edad debe ser un número entero", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
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