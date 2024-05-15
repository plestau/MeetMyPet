package com.example.trabajo_final

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PerfilUsuario : AppCompatActivity(), FragmentVerMisMascotas.OnMascotaAddedListener {
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_usuario)

        val context: Context = this
        val añadirMascota = findViewById<LinearLayout>(R.id.añadirMascota)
        val verMascotas = findViewById<LinearLayout>(R.id.verMisMascotas)
        val sharedPref = getSharedPreferences("userRole", Context.MODE_PRIVATE)
        val userRol = sharedPref.getString("role", "user")
        val verMascotasText = findViewById<TextView>(R.id.verMascotasTxt)
        val verGraficoMascotas = findViewById<TextView>(R.id.verGraficoMascotas)
        val imgAñadirMascotas = findViewById<ImageView>(R.id.añadirMascotaImg)
        val imgVerMascotas = findViewById<ImageView>(R.id.verMisMascotasImg)

        val color: Int = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            ContextCompat.getColor(context, R.color.white)
        } else {
            ContextCompat.getColor(context, R.color.black)
        }
        imgAñadirMascotas.setColorFilter(color)
        imgVerMascotas.setColorFilter(color)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (userRol == "admin") {
            verMascotasText.text = "Ver mascotas en app"
        }

        val fragmentSuperior = FragmentSuperiorPerfil()
        val fragmentInferior = FragmentInferior()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_superior_perfil, fragmentSuperior)
            commit()
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_inferior, fragmentInferior)
            commit()
        }

        userId = intent.getStringExtra("USER_ID")
        if (userId != null) {
            loadUserData(userId!!)
        } else {
            auth = FirebaseAuth.getInstance()
            userId = auth.currentUser?.uid
            if (userId != null) {
                loadUserData(userId!!)
            } else {
                Toast.makeText(context, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        añadirMascota.setOnClickListener {
            val fragmentAddMascota = FragmentAddMascota()
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentAddMascota).addToBackStack(null).commit()

            val fragmentSuperiorPerfil = supportFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
            if (fragmentSuperiorPerfil != null) {
                supportFragmentManager.beginTransaction().hide(fragmentSuperiorPerfil).commit()
            }
            findViewById<View>(R.id.scrollView)?.visibility = View.GONE
        }

        verMascotas.setOnClickListener {
            val fragmentVerMisMascotas = FragmentVerMisMascotas().apply {
                arguments = Bundle().apply {
                    putBoolean("mascotasClicables", false)
                    putString("USER_ID", userId)
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentVerMisMascotas).addToBackStack(null).commit()

            val fragmentSuperiorPerfil = supportFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
            if (fragmentSuperiorPerfil != null) {
                supportFragmentManager.beginTransaction().hide(fragmentSuperiorPerfil).commit()
            }
            findViewById<View>(R.id.scrollView)?.visibility = View.GONE
        }
        verGraficoMascotas.setOnClickListener {
            val intent = Intent(context, GraficoMascotas::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
    }

    private fun loadUserData(userId: String) {
        auth = FirebaseAuth.getInstance()
        val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/$userId")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usuario = dataSnapshot.getValue(Usuario::class.java)
                if (usuario != null) {
                    val profilePicRef = FirebaseStorage.getInstance().getReference("app/usuarios/$userId/profile_pic.jpg")

                    findViewById<TextView>(R.id.nombreUsuario).text = usuario.nombre
                    findViewById<TextView>(R.id.correo).text = "Correo: ${usuario.email}"
                    findViewById<TextView>(R.id.telefono).text = "Teléfono: ${usuario.n_telefono}"
                    findViewById<TextView>(R.id.fechaRegistro).text = "Fecha de registro: ${usuario.fecha_registro}"
                    findViewById<RatingBar>(R.id.valoracion).rating = usuario.valoraciones?.average()?.toFloat() ?: 0f
                    findViewById<TextView>(R.id.biografia).text = "Biografía: ${usuario.biografia}"

                    profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageView = findViewById<ImageView>(R.id.fotoPerfil)
                        Glide.with(this@PerfilUsuario).load(uri).placeholder(Utilidades.animacion_carga(this@PerfilUsuario)).transform(CircleCrop()).into(imageView).apply { Utilidades.opcionesGlide(this@PerfilUsuario) }
                    }.addOnFailureListener {
                        // Manejo de errores al cargar la imagen de perfil
                        Toast.makeText(this@PerfilUsuario, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show()
                    }

                    val currentUserId = auth.currentUser?.uid

                    // Comprueba si el usuario actual es diferente del usuario cuyo perfil se está visualizando
                    if (currentUserId != null && currentUserId != userId) {
                        val añadirMascota = findViewById<LinearLayout>(R.id.añadirMascota)
                        añadirMascota.visibility = View.GONE
                        val separator = findViewById<View>(R.id.separator3)
                        separator.visibility = View.GONE
                        val fragmentSuperiorPerfil = supportFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
                        if (fragmentSuperiorPerfil != null) {
                            supportFragmentManager.beginTransaction().remove(fragmentSuperiorPerfil).commit()
                        }
                    }
                } else {
                    // Manejo de errores si no se encuentra el usuario
                    Toast.makeText(this@PerfilUsuario, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejo de errores en la lectura de datos de Firebase
                Toast.makeText(this@PerfilUsuario, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is OnBackPressedInFragmentListener && fragment.onBackPressedInFragment()) {
        } else if (fragment != null && fragment.isVisible) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()

            // Mostramos los Fragments superior e inferior
            val fragmentSuperiorPerfil =
                supportFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
            if (fragmentSuperiorPerfil != null) {
                supportFragmentManager.beginTransaction().show(fragmentSuperiorPerfil).commit()
            }
            val fragmentInferior = supportFragmentManager.findFragmentById(R.id.fragment_inferior)
            if (fragmentInferior != null) {
                supportFragmentManager.beginTransaction().show(fragmentInferior).commit()
            }
            findViewById<View>(R.id.scrollView)?.visibility = View.VISIBLE
        } else {
            // Si no hay ningún Fragment visible en el contenedor, llamamos al onBackPressed de la superclase
            super.onBackPressed()
        }
    }

    override fun onMascotaAdded(mascota: Mascota) {
        val mascotasRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${auth.currentUser?.uid}/mascotas")
        mascotasRef.push().setValue(mascota)
    }

    override fun onResume() {
        super.onResume()
        FragmentInferior.actividadActual = "PerfilUsuario"
    }
}
