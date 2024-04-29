package com.example.trabajo_final

import FragmentInferior
import FragmentSuperiorPerfil
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_usuario)

        val añadirMascota = findViewById<LinearLayout>(R.id.añadirMascota)
        val verMascotas = findViewById<LinearLayout>(R.id.verMisMascotas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${currentUser?.uid}")

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val usuario = dataSnapshot.getValue(Usuario::class.java)
            val profilePicRef = FirebaseStorage.getInstance().getReference("app/usuarios/${currentUser?.uid}/profile_pic.jpg")

            findViewById<TextView>(R.id.nombreUsuario).text = usuario?.nombre
            findViewById<TextView>(R.id.correo).text = "Correo: ${usuario?.email}"
            findViewById<TextView>(R.id.telefono).text = "Teléfono: ${usuario?.n_telefono}"
            findViewById<TextView>(R.id.fechaRegistro).text = "Fecha de registro: ${usuario?.fecha_registro}"
            findViewById<RatingBar>(R.id.valoracion).rating = usuario?.valoracion?.toFloat() ?: 0f
            findViewById<RatingBar>(R.id.valoracion).apply {
                rating = usuario?.valoracion?.toFloat() ?: 0f
                isClickable = false
                isFocusable = false
            }
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                val imageView = findViewById<ImageView>(R.id.fotoPerfil)
                Glide.with(this).load(uri).transform(CircleCrop()).into(imageView)
            }
            findViewById<TextView>(R.id.biografia).text = "Biografía: ${usuario?.biografia}"
        }
        userRef.child("imagen").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageUrl = dataSnapshot.getValue(String::class.java)
                if (!imageUrl.isNullOrEmpty()) {
                    val imageView = findViewById<ImageView>(R.id.fotoPerfil)
                    Glide.with(this@PerfilUsuario).load(imageUrl).transform(CircleCrop()).into(imageView)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@PerfilUsuario, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show()
            }
        })
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usuario = dataSnapshot.getValue(Usuario::class.java)
                findViewById<TextView>(R.id.nombreUsuario).text = usuario?.nombre
                findViewById<TextView>(R.id.telefono).text = "Teléfono: ${usuario?.n_telefono}"
                findViewById<RatingBar>(R.id.valoracion).rating = usuario?.valoracion?.toFloat() ?: 0f
                findViewById<TextView>(R.id.biografia).text = usuario?.biografia
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@PerfilUsuario, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show()
            }
        })

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
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentVerMisMascotas).addToBackStack(null).commit()

            val fragmentSuperiorPerfil = supportFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
            if (fragmentSuperiorPerfil != null) {
                supportFragmentManager.beginTransaction().hide(fragmentSuperiorPerfil).commit()
            }
            findViewById<View>(R.id.scrollView)?.visibility = View.GONE
        }
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
}