package com.example.trabajo_final

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ResultadosBusqueda : AppCompatActivity() {
    private lateinit var anunciosFiltrados: MutableList<Anuncio>
    private lateinit var anuncioAdapter: AnuncioAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_busqueda)

        val rvAnunciosFiltrados = findViewById<RecyclerView>(R.id.rvAnunciosFiltrados)
        anunciosFiltrados = mutableListOf()
        anuncioAdapter = AnuncioAdapter(anunciosFiltrados, supportFragmentManager)
        rvAnunciosFiltrados.layoutManager = LinearLayoutManager(this)
        rvAnunciosFiltrados.adapter = anuncioAdapter

        dbRef = FirebaseDatabase.getInstance().getReference("app/anuncios")

        val busqueda = intent.getStringExtra("busqueda")
        if (busqueda != null) {
            filtrarAnuncios(busqueda)
        }
    }

    private fun filtrarAnuncios(busqueda: String) {
        anunciosFiltrados.clear()
        val filtros =
            busqueda.split(", ").map { it.split(": ").let { filtro -> Pair(filtro[0], filtro[1]) } }
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val anuncio = postSnapshot.getValue(Anuncio::class.java)
                    if (anuncio != null) {
                        for (filtro in filtros) {
                            when (filtro.first) {
                                "Tipo de anuncio" -> if (anuncio.tipoAnuncio?.contains(
                                        filtro.second,
                                        ignoreCase = true
                                    ) == true
                                ) {
                                    anunciosFiltrados.add(anuncio)
                                }

                                "Ubicación" -> if (anuncio.lugar?.contains(
                                        filtro.second,
                                        ignoreCase = true
                                    ) == true
                                ) {
                                    anunciosFiltrados.add(anuncio)
                                }

                                "Fecha" -> if (anuncio.fecha?.contains(
                                        filtro.second,
                                        ignoreCase = true
                                    ) == true
                                ) {
                                    anunciosFiltrados.add(anuncio)
                                }

                                "Nombre de mascota" -> if (anuncio.nombreMascota?.joinToString()
                                        ?.contains(filtro.second, ignoreCase = true) == true
                                ) {
                                    anunciosFiltrados.add(anuncio)
                                }

                                "Raza de mascota" -> if (anuncio.razaMascota?.joinToString()
                                        ?.contains(filtro.second, ignoreCase = true) == true
                                ) {
                                    anunciosFiltrados.add(anuncio)
                                }

                                "Precio" -> if (anuncio.precio.toString()
                                        .contains(filtro.second, ignoreCase = true)
                                ) {
                                    anunciosFiltrados.add(anuncio)
                                }

                                "Título" -> if (anuncio.titulo?.contains(
                                        filtro.second,
                                        ignoreCase = true
                                    ) == true
                                ) {
                                    anunciosFiltrados.add(anuncio)
                                }
                            }
                        }
                    }
                }
                anuncioAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}