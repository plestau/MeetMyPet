package com.example.trabajo_final

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnPassOlvidada = findViewById<TextView>(R.id.btnPassOlvidada)

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.email).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(baseContext, "Faltan datos en el formulario",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(baseContext, "El formato del correo no es válido",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        var user = auth.currentUser
                        var roleRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}/tipo")
                        roleRef.get().addOnSuccessListener {
                            val role = it.value.toString()
                            val sharedPref = getSharedPreferences("userRole", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            val profilePicRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}/profilePic")
                            profilePicRef.get().addOnSuccessListener { profilePic ->
                                editor.putString("profilePic", profilePic.value.toString())
                                editor.apply()
                            }
                            Toast.makeText(baseContext, "Autenticación relizada con éxito", Toast.LENGTH_SHORT).show()
                            editor.putString("role", role)
                            editor.apply()
                            val intent = if (role == "admin") {
                                Intent(this, Buscador::class.java)
                            } else {
                                Intent(this, Buscador::class.java)
                            }
                            // si el usuario es de tipo admin, se manda por sharedPreferences el tipo de usuario
                            if(role == "admin") {
                                val sharedPref = getSharedPreferences("userRole", Context.MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putString("role", role)
                                editor.apply()
                            }
                            startActivity(intent)
                            FragmentInferior.actividadActual = "Buscar"
                        }
                    } else {
                        when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(baseContext, "Correo o contraseña incorrectos",
                                    Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(baseContext, "Fallo al autenticar",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }

        btnPassOlvidada.setOnClickListener {
            // recuperar la contraseña

        }
    }
}