package com.example.trabajo_final

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.appcheck.BuildConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FragmentCambiarPassword : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cambiar_password, container, false)

        val cambiarContrasena = view.findViewById<View>(R.id.cambiarContrasena)

        auth = FirebaseAuth.getInstance()

        cambiarContrasena.setOnClickListener {
            cambiarPassword()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
        if (fragmentSuperiorPerfil != null && fragmentSuperiorPerfil.isHidden) {
            parentFragmentManager.beginTransaction().show(fragmentSuperiorPerfil).commit()
        }
    }

    private fun cambiarPassword() {
        val oldPassword = view?.findViewById<EditText>(R.id.oldPassword)
        val newPassword = view?.findViewById<EditText>(R.id.newPassword)
        val confirmPassword = view?.findViewById<EditText>(R.id.confirmPassword)
        val oldPasswordText = oldPassword?.text.toString()
        val newPasswordText = newPassword?.text.toString()
        val confirmPasswordText = confirmPassword?.text.toString()

        if (oldPasswordText.isEmpty() || newPasswordText.isEmpty() || confirmPasswordText.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        val userRef = FirebaseDatabase.getInstance().getReference("app").child("usuarios").child(user!!.uid)

        if (newPasswordText == confirmPasswordText) {
            user?.updatePassword(newPasswordText)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Actualiza el campo contraseña del usuario en la base de datos
                    val userInfoUpdate = mapOf("password" to newPasswordText)
                    userRef.updateChildren(userInfoUpdate).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show()
                            val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
                            parentFragmentManager.beginTransaction().show(fragmentSuperiorPerfil!!).commit()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Error al actualizar la contraseña en la base de datos", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
        }
    }
}