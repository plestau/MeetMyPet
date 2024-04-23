package com.example.trabajo_final

import FragmentInferior
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MisAnuncios : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var anunciosRecyclerView: RecyclerView
    private lateinit var anunciosAdapter: AnuncioAdapter
    private val anunciosAñadidosIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_anuncios)

        auth = FirebaseAuth.getInstance()
        anunciosRecyclerView = findViewById(R.id.recyclerViewMisAnuncios)
        anunciosAdapter = AnuncioAdapter(listOf())
        anunciosRecyclerView.layoutManager = LinearLayoutManager(this)
        anunciosRecyclerView.adapter = anunciosAdapter

        mostrarAnunciosUsuario()

        val fragmentInferior = FragmentInferior()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_inferior, fragmentInferior)
            commit()
        }

        FragmentInferior.actividadActual = "MisAnuncios"
    }

    private fun mostrarAnunciosUsuario() {
        // Obtén el rol del usuario de SharedPreferences
        val sharedPref = getSharedPreferences("userRole", Context.MODE_PRIVATE)
        val userRol = sharedPref.getString("role", "user")

        // Decide qué referencia usar basándote en el rol del usuario
        val anunciosRef = if (userRol == "admin") {
            FirebaseDatabase.getInstance().getReference("app/anuncios")
        } else {
            val currentUser = auth.currentUser
            FirebaseDatabase.getInstance().getReference("app/anuncios").orderByChild("usuarioDueño").equalTo(currentUser?.uid)
        }

        anunciosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val anuncios = mutableListOf<Anuncio>()
                dataSnapshot.children.forEach { anuncio ->
                    val anuncioObject = anuncio.getValue(Anuncio::class.java)
                    if (anuncioObject != null) {
                        anuncios.add(anuncioObject)
                        anuncioObject.id?.let { anunciosAñadidosIds.add(it) }
                    }
                }
                anunciosAdapter.updateData(anuncios)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MisAnuncios, "Error al cargar anuncios", Toast.LENGTH_SHORT).show()
            }
        })
    }
}