package com.example.trabajo_final

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
        val filtros = busqueda.split(", ").map { it.split(": ").let { filtro -> Pair(filtro[0], filtro[1]) } }
        for (anuncio in anunciosFiltrados) {
            for (filtro in filtros) {
                when (filtro.first) {
                    "Tipo de anuncio" -> if (anuncio.tipoAnuncio?.contains(filtro.second, ignoreCase = true) == true) {
                        anunciosFiltrados.add(anuncio)
                    }
                    "Ubicación" -> if (anuncio.lugar?.contains(filtro.second, ignoreCase = true) == true) {
                        anunciosFiltrados.add(anuncio)
                    }
                    "Fecha" -> if (anuncio.fecha?.contains(filtro.second, ignoreCase = true) == true) {
                        anunciosFiltrados.add(anuncio)
                    }
                    "Nombre de mascota" -> if (anuncio.nombreMascota?.joinToString()?.contains(filtro.second, ignoreCase = true) == true) {
                        anunciosFiltrados.add(anuncio)
                    }
                    "Raza de mascota" -> if (anuncio.razaMascota?.joinToString()?.contains(filtro.second, ignoreCase = true) == true) {
                        anunciosFiltrados.add(anuncio)
                    }
                    "Precio" -> if (anuncio.precio.toString().contains(filtro.second, ignoreCase = true)) {
                        anunciosFiltrados.add(anuncio)
                    }
                    "Título" -> if (anuncio.titulo?.contains(filtro.second, ignoreCase = true) == true) {
                        anunciosFiltrados.add(anuncio)
                    }
                }
            }
        }
        anuncioAdapter.notifyDataSetChanged()
    }
}