package com.example.trabajo_final

import FragmentInferior
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Buscador : AppCompatActivity() {
    private lateinit var anuncios: MutableList<Anuncio>
    private lateinit var anunciosFiltrados: MutableList<Anuncio>
    private lateinit var anuncioAdapter: AnuncioAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var historialBusqueda: MutableList<String>
    private lateinit var historialBusquedaAdapter: HistorialBusquedaAdapter
    private lateinit var filtrosAnadidos: MutableList<Pair<String, String>>
    private lateinit var filtrosAnadidosAdapter: FiltrosAnadidosAdapter
    private lateinit var userId: String
    private val MAX_HISTORIAL_SIZE = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscador)


        val spinnerFiltro = findViewById<Spinner>(R.id.spinnerFiltro)
        val editTextBusqueda = findViewById<EditText>(R.id.editTextBusqueda)
        val botonBuscar = findViewById<Button>(R.id.botonBuscar)
        val botonAnadirFiltro = findViewById<Button>(R.id.botonAñadirFiltro)
        val rvFiltrosAnadidos = findViewById<RecyclerView>(R.id.rvFiltrosAñadidos)
        val rvHistorialBusqueda = findViewById<RecyclerView>(R.id.rvHistorialBusqueda)
        val sharedPref = getSharedPreferences("HistorialBusqueda", Context.MODE_PRIVATE)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val historialBusquedaString = sharedPref.getString(userId, "")
        if (historialBusquedaString.isNullOrEmpty()) {
            historialBusqueda = mutableListOf()
        } else {
            historialBusqueda = historialBusquedaString.split("||").toMutableList()
        }
        // si el tamaño del historial es mayor que el máximo permitido, se toman los últimos elementos
        if (historialBusqueda.size > MAX_HISTORIAL_SIZE) {
            historialBusqueda = historialBusqueda.takeLast(MAX_HISTORIAL_SIZE).toMutableList()
        }
        historialBusquedaAdapter = HistorialBusquedaAdapter(historialBusqueda)
        rvHistorialBusqueda.layoutManager = LinearLayoutManager(this)
        rvHistorialBusqueda.adapter = historialBusquedaAdapter

        anuncios = mutableListOf()
        anunciosFiltrados = mutableListOf()
        anuncioAdapter = AnuncioAdapter(anunciosFiltrados, supportFragmentManager, "ResultadosBusqueda")

        filtrosAnadidos = mutableListOf()
        filtrosAnadidosAdapter = FiltrosAnadidosAdapter(filtrosAnadidos)
        rvFiltrosAnadidos.layoutManager = LinearLayoutManager(this)
        rvFiltrosAnadidos.adapter = filtrosAnadidosAdapter

        dbRef = FirebaseDatabase.getInstance().getReference("anuncios")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                anuncios.clear()
                for (postSnapshot in snapshot.children) {
                    val anuncio = postSnapshot.getValue(Anuncio::class.java)
                    if (anuncio != null) {
                        anuncios.add(anuncio)
                    }
                }
                anuncioAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Buscador, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        val fragmentInferior = FragmentInferior()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_inferior, fragmentInferior)
            commit()
        }

        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val currentDate = Calendar.getInstance()
                if (selectedDate.before(currentDate)) {
                    Toast.makeText(this, "Selecciona una fecha válida", Toast.LENGTH_SHORT).show()
                } else {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    editTextBusqueda.setText(dateFormat.format(selectedDate.time))
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val spinnerTipoAnuncio = findViewById<Spinner>(R.id.spinnerTipoAnuncio)
        val spinnerRazaMascota = findViewById<Spinner>(R.id.spinnerRazaMascota)
        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedFilter = parent.getItemAtPosition(position).toString()
                when (selectedFilter) {
                    "Tipo de anuncio" -> {
                        editTextBusqueda.visibility = View.GONE
                        spinnerTipoAnuncio.visibility = View.VISIBLE
                        spinnerRazaMascota.visibility = View.GONE
                    }
                    "Fecha" -> {
                        editTextBusqueda.visibility = View.VISIBLE
                        spinnerTipoAnuncio.visibility = View.GONE
                        spinnerRazaMascota.visibility = View.GONE
                        datePickerDialog.show()
                    }
                    "Raza de mascota" -> {
                        editTextBusqueda.visibility = View.GONE
                        spinnerTipoAnuncio.visibility = View.GONE
                        spinnerRazaMascota.visibility = View.VISIBLE
                    }
                    else -> {
                        editTextBusqueda.visibility = View.VISIBLE
                        spinnerTipoAnuncio.visibility = View.GONE
                        spinnerRazaMascota.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        botonAnadirFiltro.setOnClickListener {
            val filtroSeleccionado = spinnerFiltro.selectedItem.toString()
            var textoBusqueda = editTextBusqueda.text.toString()
            when (filtroSeleccionado) {
                "Tipo de anuncio" -> {
                    if (spinnerTipoAnuncio.selectedItemPosition != 0) {
                        textoBusqueda = spinnerTipoAnuncio.selectedItem.toString()
                    } else {
                        Toast.makeText(this, "Selecciona una opción válida", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                "Raza de mascota" -> {
                    if (spinnerRazaMascota.selectedItemPosition != 0) {
                        textoBusqueda = spinnerRazaMascota.selectedItem.toString()
                    } else {
                        Toast.makeText(this, "Selecciona una opción válida", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                "Precio" -> {
                    if (textoBusqueda.toDoubleOrNull() == null || textoBusqueda.toDouble() < 0) {
                        Toast.makeText(this, "Introduce un precio válido", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            }

            if (spinnerFiltro.selectedItemPosition != 0 && textoBusqueda.isNotEmpty()) {
                // Verifica si el filtro ya ha sido añadido
                if (filtrosAnadidos.any { it.first == filtroSeleccionado }) {
                    Toast.makeText(this, "El tipo de filtro ya ha sido añadido", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val nuevoFiltro = Pair(filtroSeleccionado, textoBusqueda)

                    if (spinnerFiltro.selectedItemPosition != 0 && textoBusqueda.isNotEmpty()) {
                        filtrosAnadidos.add(nuevoFiltro)
                        filtrosAnadidosAdapter.notifyDataSetChanged()
                        editTextBusqueda.text.clear()
                        spinnerFiltro.setSelection(0)
                    } else {
                        Toast.makeText(
                            this,
                            "Selecciona un filtro válido y asegúrate de que no esté vacío",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (filtroSeleccionado == "Tipo de anuncio") {
                        spinnerTipoAnuncio.visibility = View.GONE
                        editTextBusqueda.visibility = View.VISIBLE
                    }
                    if (filtroSeleccionado == "Raza de mascota") {
                        spinnerRazaMascota.visibility = View.GONE
                        editTextBusqueda.visibility = View.VISIBLE
                    }
                }
            }
        }

        botonBuscar.setOnClickListener {
            // Verifica que haya al menos un filtro añadido
            if (filtrosAnadidos.isEmpty()) {
                Toast.makeText(this, "Añade al menos un filtro para realizar la búsqueda", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val busqueda = filtrosAnadidos.joinToString(separator = " | ") { "${it.first}: ${it.second}" }
                val intent = Intent(this, ResultadosBusqueda::class.java)
                intent.putExtra("busqueda", busqueda)
                intent.putExtra("filtros", filtrosAnadidos.map { Pair(it.first, it.second) }.toTypedArray()) // Pasar filtros como extras
                startActivity(intent)
                // Check if the history size is equal to the maximum allowed size and if so, remove the oldest search
                if (historialBusqueda.size == MAX_HISTORIAL_SIZE) {
                    historialBusqueda.removeAt(historialBusqueda.size - 1)
                }
                historialBusqueda.add(0, busqueda)
                val historialBusquedaString = historialBusqueda.joinToString("||")
                sharedPref.edit().putString(userId, historialBusquedaString).apply()
                historialBusquedaAdapter.notifyDataSetChanged()
                Log.d("Buscador", "Filtros usados: $busqueda")

                // Limpiar los filtros después de realizar la búsqueda
                filtrosAnadidos.clear()
                filtrosAnadidosAdapter.notifyDataSetChanged()
            }
        }
    }
}