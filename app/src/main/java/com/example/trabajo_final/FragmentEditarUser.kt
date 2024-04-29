package com.example.trabajo_final

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FragmentEditarUser : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_user, container, false)

        val cambiarDatosPersonales = view.findViewById<View>(R.id.cambiarDatosPersonales)
        val nombre = view.findViewById<EditText>(R.id.nombre)
        val n_telefono = view.findViewById<EditText>(R.id.n_telefono)
        val biografia = view.findViewById<EditText>(R.id.biografia)

        auth = FirebaseAuth.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}")

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val usuario = dataSnapshot.getValue(Usuario::class.java)
            nombre.setText(usuario?.nombre)
            n_telefono.setText(usuario?.n_telefono)
            biografia.setText(usuario?.biografia)
        }

        cambiarDatosPersonales.setOnClickListener {
            cambiarDatosPersonales()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun cambiarDatosPersonales() {
        val nombre = view?.findViewById<EditText>(R.id.nombre)
        val nombreText = nombre?.text.toString()

        val n_telefono = view?.findViewById<EditText>(R.id.n_telefono)
        val n_telefonoText = n_telefono?.text.toString()

        val biografia = view?.findViewById<EditText>(R.id.biografia)
        val biografiaText = biografia?.text.toString()

        val user = FirebaseAuth.getInstance().currentUser
        val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}")

        val userInfoUpdate = mapOf("nombre" to nombreText, "n_telefono" to n_telefonoText, "biografia" to biografiaText)

        userRef.updateChildren(userInfoUpdate).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Datos personales actualizados con éxito", Toast.LENGTH_SHORT).show()
                val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
                parentFragmentManager.beginTransaction().show(fragmentSuperiorPerfil!!).commit()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Error al actualizar los datos personales", Toast.LENGTH_SHORT).show()
            }
        }
    }
}