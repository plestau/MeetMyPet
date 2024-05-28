package com.example.trabajo_final

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.trabajo_final.R
import com.example.trabajo_final.Utilidades
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var profilePic: Uri? = null
    private lateinit var profilePicImageView: ImageView

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            profilePic = uri
            profilePicImageView.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val db_ref = FirebaseDatabase.getInstance().getReference()
        val st_ref = FirebaseStorage.getInstance().getReference()
        val lista_usuarios = Utilidades.obtenerListaUsuarios(db_ref)
        // si viene de registrarse con google o facebook, deshabilita el campo de correo
        if (intent.getStringExtra("email") != null) {
            findViewById<EditText>(R.id.email).setText(intent.getStringExtra("email"))
            findViewById<EditText>(R.id.email).isEnabled = false
        }

        profilePicImageView = findViewById(R.id.imageView)
        profilePicImageView.setOnClickListener {
            mostrarDialogoSeleccion()
        }

        btnRegister.setOnClickListener {
            val email = findViewById<EditText>(R.id.email).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()
            val name = findViewById<EditText>(R.id.name).text.toString()

            if(email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(baseContext, "Faltan datos en el formulario",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(baseContext, "El formato del correo no es válido",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password.length < 6) {
                Toast.makeText(baseContext, "La contraseña es muy corta",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (Utilidades.existeUsuario(lista_usuarios, email)) {
                Toast.makeText(baseContext, "Ya existe un usuario con ese correo",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar si el usuario está registrándose con Google con el intent "email"
            val isRegisteringWithGoogle = intent.getStringExtra("email") != null

            if (!isRegisteringWithGoogle) {
                // Si no está registrándose con Google, entonces crea el usuario en Firebase Auth
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}")
                            val role = if (email == "admin@admin.com" && password == "administrador") { "admin" } else { "user" }
                            CoroutineScope(Dispatchers.IO).launch {
                                var profilePicUrl = ""
                                if (profilePic != null) {
                                    // Guarda la imagen de perfil en una carpeta con el nombre del ID del usuario
                                    val profilePicRef = FirebaseStorage.getInstance().getReference("app/usuarios/${user?.uid}/profile_pic.jpg")
                                    val uploadTask = profilePicRef.putFile(profilePic!!)
                                    uploadTask.await() // Espera a que la foto de perfil se suba a Firebase Storage
                                    profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                                        profilePicUrl = uri.toString()

                                        val userInfo = mapOf(
                                            "id" to user?.uid,
                                            "nombre" to name,
                                            "email" to email,
                                            "password" to password,
                                            "tipo" to role,
                                            "profilePic" to profilePicUrl,
                                            "biografia" to "",
                                            "n_telefono" to "",
                                            "valoraciones" to arrayListOf<Float>(),
                                            "mascotas" to arrayListOf<String>(),
                                            "fecha_registro" to Utilidades.obtenerFechaActual(),
                                            "estado_noti" to Estado.CREADO,
                                            "user_notificacion" to "",
                                        )
                                        userRef.setValue(userInfo)

                                        Toast.makeText(baseContext, "Usuario registrado con éxito",
                                            Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this@Register, Login::class.java)
                                        startActivity(intent)
                                    }.await()
                                } else {
                                    val userInfo = mapOf(
                                        "id" to user?.uid,
                                        "nombre" to name,
                                        "email" to email,
                                        "password" to password,
                                        "tipo" to role,
                                        "profilePic" to profilePicUrl,
                                        "biografia" to "",
                                        "n_telefono" to "",
                                        "valoraciones" to arrayListOf<Float>(),
                                        "mascotas" to arrayListOf<String>(),
                                        "fecha_registro" to Utilidades.obtenerFechaActual(),
                                        "estado_noti" to Estado.CREADO,
                                        "user_notificacion" to "",
                                    )
                                    userRef.setValue(userInfo)

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(baseContext, "Usuario registrado con éxito",
                                            Toast.LENGTH_SHORT).show()
                                    }

                                    val intent = Intent(this@Register, Login::class.java)
                                    startActivity(intent)
                                }
                            }
                        } else {
                            Toast.makeText(baseContext, "Fallo al registrar",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Si está registrándose con Google, solo guarda los datos del usuario en la base de datos
                val user = auth.currentUser
                val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${user?.uid}")
                val role = if (email == "admin@admin.com" && password == "administrador") {
                    "admin"
                } else {
                    "user"
                }
                CoroutineScope(Dispatchers.IO).launch {
                    var profilePicUrl = ""
                    if (profilePic != null) {
                        // Guarda la imagen de perfil en una carpeta con el nombre del ID del usuario
                        val profilePicRef = FirebaseStorage.getInstance().getReference("app/usuarios/${user?.uid}/profile_pic.jpg")
                        profilePicRef.putFile(profilePic!!).addOnSuccessListener {
                            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                                profilePicUrl = uri.toString()
                                Log.d("Register", "URL de la foto de perfil: $profilePicUrl")

                                val userInfo = mapOf(
                                    "id" to user?.uid,
                                    "nombre" to name,
                                    "email" to email,
                                    "password" to password,
                                    "tipo" to role,
                                    "profilePic" to profilePicUrl,
                                    "biografia" to "",
                                    "n_telefono" to "",
                                    "valoraciones" to arrayListOf<Float>(),
                                    "mascotas" to arrayListOf<String>(),
                                    "fecha_registro" to Utilidades.obtenerFechaActual(),
                                    "estado_noti" to Estado.CREADO,
                                    "user_notificacion" to "",
                                )
                                userRef.setValue(userInfo)

                                val intent = Intent(this@Register, LoginOptions::class.java)
                                startActivity(intent)
                            }
                        }.await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(baseContext, "Usuario registrado con éxito",
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val userInfo = mapOf(
                            "id" to user?.uid,
                            "nombre" to name,
                            "email" to email,
                            "password" to password,
                            "tipo" to role,
                            "profilePic" to profilePicUrl,
                            "biografia" to "",
                            "n_telefono" to "",
                            "valoraciones" to arrayListOf<Float>(),
                            "mascotas" to arrayListOf<String>(),
                            "fecha_registro" to Utilidades.obtenerFechaActual(),
                            "estado_noti" to Estado.CREADO,
                            "user_notificacion" to "",
                        )
                        userRef.setValue(userInfo)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(baseContext, "Usuario registrado con éxito",
                                Toast.LENGTH_SHORT).show()
                        }

                        val intent = Intent(this@Register, LoginOptions::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun mostrarDialogoSeleccion() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        val builder = AlertDialog.Builder(this)
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
            profilePicImageView.setImageURI(profilePic)
        }
    }

    private fun tomarFoto() {
        val archivo = File.createTempFile("temporal", ".jpg", cacheDir)
        profilePic = FileProvider.getUriForFile(
            this,
            "com.example.trabajo_final.fileprovider",
            archivo
        )
        tomarFoto.launch(profilePic)
    }

    private fun seleccionarDeGaleria() {
        accesoGaleria.launch("image/*")
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que quieres salir?")
            .setPositiveButton("Sí") { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .show()
    }
}