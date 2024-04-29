package com.example.trabajo_final

import FragmentInferior
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FragmentPreferenciasCuenta : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var profilePic: Uri? = null

    private val seleccionarDeGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            actualizarFotoPerfil(uri)
        }
    }

    private val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
        if (isSuccess) {
            actualizarFotoPerfil(profilePic!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_preferencias_cuenta, container, false)

        val cambiarFoto = view.findViewById<View>(R.id.cambiarFotoPerfil)
        val cambiarDatosPersonales = view.findViewById<View>(R.id.cambiarDatosPersonales)
        val cambiarContrasena = view.findViewById<View>(R.id.cambiarContrasena)
        val cerrarSesion = view.findViewById<View>(R.id.cerrarSesion)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN)

        cambiarFoto.setOnClickListener {
            mostrarDialogoSeleccion()
        }

        cambiarDatosPersonales.setOnClickListener {
            val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
            if (fragmentSuperiorPerfil != null) {
                parentFragmentManager.beginTransaction().hide(fragmentSuperiorPerfil).commit()
            }

            val fragmentChangeData = FragmentEditarUser()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentChangeData).addToBackStack(null).commit()
        }

        cambiarContrasena.setOnClickListener {
            val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
            if (fragmentSuperiorPerfil != null) {
                parentFragmentManager.beginTransaction().hide(fragmentSuperiorPerfil).commit()
            }

            val fragmentChangePassword = FragmentCambiarPassword()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentChangePassword).addToBackStack(null).commit()
        }

        cerrarSesion.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut()
            requireActivity().finish()
            val intent = Intent(requireContext(), LoginOptions::class.java)
            startActivity(intent)
            // Eliminar el rol del usuario de SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("userRole", 0)
            val editor = sharedPref.edit()
            editor.remove("role")
            editor.apply()

            // Restablecer el color de la tarjeta de usuario en FragmentInferior
            val fragmentInferior = parentFragmentManager.findFragmentById(R.id.fragment_inferior) as? FragmentInferior
            fragmentInferior?.let {
                val cardPerfil: CardView = it.view?.findViewById(R.id.card_perfil)!!
                val imgPerfil: ImageView = cardPerfil.findViewById(R.id.img_perfil)
                val txtPerfil: TextView = cardPerfil.findViewById(R.id.txt_perfil)
                val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
                imgPerfil.setColorFilter(originalColor)
                txtPerfil.setTextColor(originalColor)
            }
        }

        return view
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
        val archivo = File.createTempFile("temporal", ".jpg", requireActivity().cacheDir)
        profilePic = FileProvider.getUriForFile(
            requireContext(),
            "com.example.trabajo_final.fileprovider",
            archivo
        )
        tomarFoto.launch(profilePic)
    }

    private fun seleccionarDeGaleria() {
        seleccionarDeGaleria.launch("image/*")
    }

    private fun actualizarFotoPerfil(uri: Uri) {
        val imageView = requireActivity().findViewById<ImageView>(R.id.fotoPerfil)
        Glide.with(this).load(uri).into(imageView)
        val currentUser = auth.currentUser
        val profilePicRef = FirebaseStorage.getInstance().getReference("app/usuarios/${currentUser?.uid}/profile_pic.jpg")
        profilePicRef.putFile(uri).addOnSuccessListener {
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${currentUser?.uid}")
                userRef.child("imagen").setValue(uri.toString())
            }
        }
        Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
    }
}