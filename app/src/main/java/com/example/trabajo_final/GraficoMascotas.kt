package com.example.trabajo_final

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GraficoMascotas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_grafico_mascotas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val database = FirebaseDatabase.getInstance()
        val userId = intent.getStringExtra("USER_ID")
        val walkerAdsRef = database.getReference("app/anuncios").orderByChild("usuarioPaseador").equalTo(userId)

        // Lee los datos de la base de datos
        walkerAdsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Crea un mapa para contar el número de cada tipo de mascota
                val petCounts = mutableMapOf<String, Float>()

                // Itera sobre los datos
                for (adSnapshot in dataSnapshot.children) {
                    // Obtiene el estado del anuncio
                    val estado = adSnapshot.child("estado").getValue(String::class.java) ?: continue

                    // Comprueba si el estado es "En curso", "terminado" o "valorado"
                    if (estado in listOf("En curso", "terminado", "valorado")) {
                        // Obtiene la lista de tipos de mascota
                        val petTypes = adSnapshot.child("razaMascota").getValue<ArrayList<String>>() ?: continue

                        // Incrementa el contador para este tipo de mascota
                        for (petType in petTypes) {
                            // Incrementa el contador para este tipo de mascota
                            petCounts[petType] = petCounts.getOrDefault(petType, 0f) + 1f
                        }
                    }
                }

                // Crea una lista de entradas para el gráfico
                val entries = petCounts.map { (type, count) -> PieEntry(count, type) }

                // Crea el conjunto de datos y configura el gráfico
                val dataSet = PieDataSet(entries, "")
                dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

                val data = PieData(dataSet)
                pieChart.data = data
                pieChart.invalidate()

                pieChart.description.isEnabled = false
                pieChart.animateY(1000)
                pieChart.centerText = "Mascotas cuidadas"
                pieChart.invalidate()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error al leer los datos
                println("Error al leer los datos: ${databaseError.toException()}")
            }
        })
    }
}