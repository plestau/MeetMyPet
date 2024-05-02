package com.example.trabajo_final

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class ResultadosBusqueda : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var anuncios: MutableList<Anuncio>
    private lateinit var anuncioAdapter: AnuncioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_busqueda)

        val filtros = intent.getSerializableExtra("filtros") as? Array<Pair<String, String>> ?: arrayOf()

        val rvAnuncios = findViewById<RecyclerView>(R.id.rvAnunciosFiltrados)
        anuncios = mutableListOf()
        anuncioAdapter = AnuncioAdapter(anuncios, supportFragmentManager)
        rvAnuncios.layoutManager = LinearLayoutManager(this)
        rvAnuncios.adapter = anuncioAdapter

        dbRef = FirebaseDatabase.getInstance().getReference("app/anuncios")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                anuncios.clear()
                for (postSnapshot in snapshot.children) {
                    val anuncio = postSnapshot.getValue(Anuncio::class.java)
                    if (anuncio != null) {
                        // Filtrar anuncios según los filtros recibidos
                        if (cumpleConFiltros(anuncio, filtros)) {
                            anuncios.add(anuncio)
                        }
                    }
                }
                anuncioAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ResultadosBusqueda, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cumpleConFiltros(anuncio: Anuncio, filtros: Array<Pair<String, String>>): Boolean {
        for (filtro in filtros) {
            when (filtro.first) {
                "Tipo de anuncio" -> {
                    val tipoAnuncioText = when (filtro.second) {
                        "Paseo de mascotas" -> "Paseo"
                        "Cuidado a domicilio en casa del dueño" -> "Cuidado casa dueño"
                        "Cuidado a domicilio en casa del paseador" -> "Cuidado casa paseador"
                        else -> filtro.second
                    }
                    if (anuncio.tipoAnuncio != tipoAnuncioText) return false
                }
                "Fecha" -> {
                    val fechaFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaDate = fechaFormat.parse(filtro.second)
                    val fechaFormatted = fechaFormat.format(fechaDate)
                    if (anuncio.fecha != fechaFormatted) return false
                }
                "Ubicación" -> if (anuncio.lugar?.lowercase(Locale.getDefault()) != filtro.second.lowercase(Locale.getDefault())) return false
                "Nombre de mascota" -> if (!anuncio.nombreMascota?.contains(filtro.second)!!) return false
                "Raza de mascota" -> if (!anuncio.razaMascota?.contains(filtro.second)!!) return false
                "Precio" -> if (anuncio.precio?.toDouble() != filtro.second.toDouble()) return false
                "Título" -> if (anuncio.titulo?.lowercase(Locale.getDefault()) != filtro.second.lowercase(Locale.getDefault())) return false
            }
        }
        return true
    }
}
