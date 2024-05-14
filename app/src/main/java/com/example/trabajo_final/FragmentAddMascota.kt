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
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class FragmentAddMascota : Fragment(), OnBackPressedInFragmentListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var mascotaImageView: ImageView
    private var mascotaPic: Uri? = null
    private val accesoGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                mascotaPic = uri
                mascotaImageView.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_mascota, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        mascotaImageView = view.findViewById(R.id.imagenMascota)
        mascotaImageView.setOnClickListener {
            mostrarDialogoSeleccion()
        }

        val btnAddMascota = view.findViewById<Button>(R.id.btnAddMascota)
        btnAddMascota.setOnClickListener {
            val nombre = view.findViewById<EditText>(R.id.nombre).text.toString()
            val raza = view.findViewById<Spinner>(R.id.raza).selectedItem.toString()
            val edadString = view.findViewById<EditText>(R.id.edad).text.toString()
            val sexo = view.findViewById<Spinner>(R.id.sexo).selectedItem.toString()
            val esterilizado = view.findViewById<CheckBox>(R.id.esterilizado).isChecked
            val biografia = view.findViewById<EditText>(R.id.biografia).text.toString()

            val edad = edadString.toIntOrNull()
            if (edad == null) {
                Toast.makeText(context, "La edad debe ser un número entero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (edad < 0) {
                Toast.makeText(requireContext(), "La edad no puede ser negativa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(nombre.isEmpty() || raza == "Seleccione tipo animal:" || sexo == "Seleccione sexo:" || biografia.isEmpty()) {
                Toast.makeText(context, "Faltan datos en el formulario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val dbRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}/mascotas")
            val stRef = FirebaseStorage.getInstance().getReference("app/usuarios/${user?.uid}/mascotas")

            CoroutineScope(Dispatchers.IO).launch {
                val mascotaPicUrl = if (mascotaPic != null) {
                    val mascotaPicRef = stRef.child("${dbRef.push().key}.jpg")
                    mascotaPicRef.putFile(mascotaPic!!).await()
                    mascotaPicRef.downloadUrl.await().toString()
                } else {
                    null
                }

                val mascota = Mascota(
                    id = dbRef.push().key,
                    raza = raza,
                    nombre = nombre.lowercase(),
                    foto = mascotaPicUrl,
                    edad = edad,
                    sexo = sexo,
                    esterilizado = esterilizado,
                    biografia = biografia,
                    valoraciones = arrayListOf(),
                    usuarioId = user?.uid,
                    borrable = true,
                    estado_noti = Estado.CREADO
                )

                dbRef.child(mascota.id!!).setValue(mascota).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Mascota añadida con éxito", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                    val fragmentInferior = parentFragmentManager.findFragmentById(R.id.fragment_inferior)
                    if (fragmentInferior != null && fragmentInferior.isHidden) {
                        parentFragmentManager.beginTransaction().show(fragmentInferior).commit()
                    }
                    val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
                    if (fragmentSuperiorPerfil != null && fragmentSuperiorPerfil.isHidden) {
                        parentFragmentManager.beginTransaction().show(fragmentSuperiorPerfil).commit()
                    }
                    val scrollView = activity?.findViewById<View>(R.id.scrollView)
                    scrollView?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val fragmentInferior = parentFragmentManager.findFragmentById(R.id.fragment_inferior)
        if (fragmentInferior != null && fragmentInferior.isHidden) {
            parentFragmentManager.beginTransaction().show(fragmentInferior).commit()
        }
        val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
        if (fragmentSuperiorPerfil != null && fragmentSuperiorPerfil.isHidden) {
            parentFragmentManager.beginTransaction().show(fragmentSuperiorPerfil).commit()
        }

        parentFragmentManager.popBackStack()
        activity?.findViewById<ScrollView>(R.id.scrollView)?.visibility = View.VISIBLE
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

    private val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
        if (isSuccess) {
            mascotaImageView.setImageURI(mascotaPic)
        }
    }

    private fun tomarFoto() {
        val archivo = File.createTempFile("foto", ".jpg", requireContext().cacheDir)
        mascotaPic = FileProvider.getUriForFile(requireContext(), "com.example.trabajo_final.fileprovider", archivo)
        tomarFoto.launch(mascotaPic)
    }

    private fun seleccionarDeGaleria() {
        accesoGaleria.launch("image/*")
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
        // Devuelve true para indicar que has manejado el evento de retroceso
        return true
    }
}