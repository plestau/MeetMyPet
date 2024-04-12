package com.example.trabajo_final

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
        val builder = AlertDialog.Builder(requireContext())
        val oldPassword = view?.findViewById<EditText>(R.id.oldPassword)
        val newPassword = view?.findViewById<EditText>(R.id.newPassword)
        val confirmPassword = view?.findViewById<EditText>(R.id.confirmPassword)

        with(builder) {
            setTitle("Cambiar contraseña")
            setPositiveButton("Cambiar") { dialog, which ->
                val oldPasswordText = oldPassword?.text.toString()
                val newPasswordText = newPassword?.text.toString()
                val confirmPasswordText = confirmPassword?.text.toString()

                if (newPasswordText == oldPasswordText) {
                    Toast.makeText(requireContext(), "La nueva contraseña no puede ser igual a la antigua", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val user = FirebaseAuth.getInstance().currentUser
                val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}")

                userRef.get().addOnSuccessListener { dataSnapshot ->
                    val passwordInDatabase = dataSnapshot.child("password").value.toString()

                    if (oldPasswordText != passwordInDatabase) {
                        Toast.makeText(requireContext(), "La contraseña antigua no es correcta", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    if (newPasswordText == confirmPasswordText) {
                        val userInfoUpdate = mapOf("password" to newPasswordText)
                        user?.updatePassword(newPasswordText)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show()
                                val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
                                parentFragmentManager.beginTransaction().show(fragmentSuperiorPerfil!!).commit()
                                parentFragmentManager.popBackStack()
                                userRef.updateChildren(userInfoUpdate)
                            } else {
                                Toast.makeText(requireContext(), "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Operación cancelada", Toast.LENGTH_SHORT).show()
            }
            show()
        }
    }
}