package com.example.trabajo_final

import FragmentInferior
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class PublicarAnuncio : AppCompatActivity(), FragmentVerMisMascotas.OnMascotaAddedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var mascotaAdapter: MascotaEnAnuncioAdapter
    val mascotasAñadidasList = mutableListOf<Mascota>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar_anuncio)

        val fragmentInferior = FragmentInferior()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_inferior, fragmentInferior)
            commit()
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val titulo = findViewById<EditText>(R.id.titulo)
        val descripcion = findViewById<EditText>(R.id.descripcion)
        val lugar = findViewById<EditText>(R.id.lugar)
        val fecha = findViewById<EditText>(R.id.fecha)
        val hora = findViewById<EditText>(R.id.hora)
        val tipoAnuncio = findViewById<Spinner>(R.id.tipoAnuncio)
        val precio = findViewById<EditText>(R.id.precio)
        val mascotasAñadidas = findViewById<RecyclerView>(R.id.mascotasAñadidasRv)
        mascotaAdapter = MascotaEnAnuncioAdapter(mascotasAñadidasList) { mascota ->
            mascotasAñadidasList.removeAll { it.nombre == mascota.nombre }
        }

        mascotasAñadidas.adapter = mascotaAdapter
        mascotasAñadidas.layoutManager = LinearLayoutManager(this)

        val verMisMascotas = findViewById<LinearLayout>(R.id.verMisMascotas)
        verMisMascotas.setOnClickListener {
            val fragment = FragmentVerMisMascotas().apply {
                arguments = Bundle().apply {
                    putBoolean("fromPublicarAnuncio", true)
                    putBoolean("mascotasClicables", true)
                    putParcelableArrayList("mascotasAñadidasList", ArrayList(mascotasAñadidasList))
                }
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commitAllowingStateLoss()
            scrollView.visibility = View.GONE
        }

        val btnPublicarAnuncio = findViewById<Button>(R.id.btnPublicarAnuncio)

        btnPublicarAnuncio.setOnClickListener {
            val tituloText = titulo.text.toString().lowercase()
            val descripcionText = descripcion.text.toString()
            val lugarText = lugar.text.toString().lowercase()
            val fechaText = fecha.text.toString()
            val horaText = hora.text.toString()
            val tipoAnuncioText = when (tipoAnuncio.selectedItem) {
                "Paseo de mascotas" -> "Paseo"
                "Cuidado a domicilio en casa del dueño" -> "Cuidado casa dueño"
                "Cuidado a domicilio en casa del paseador" -> "Cuidado casa paseador"
                else -> ""
            }
            val precioText = precio.text.toString()

            // si el precio no es un número, se muestra un mensaje de error
            if (precioText.toFloat() < 0) {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "El precio no puede ser negativo",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!fechaText.matches(Regex("^\\d{1,2}/\\d{1,2}/\\d{4}$"))) {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "La fecha debe tener el formato D/M/AAAA",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (tituloText.isEmpty() || descripcionText.isEmpty() || lugarText.isEmpty() || fechaText.isEmpty() || horaText.isEmpty() || precioText.isEmpty() || tipoAnuncioText == "Seleccione tipo de anuncio:") {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "Todos los campos deben estar llenos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val fechaEvento = fechaText.split("/")
            val diaEventoInt = fechaEvento[0].toIntOrNull()
            val mesEventoInt = fechaEvento[1].toIntOrNull()
            val añoEventoInt = fechaEvento[2].toIntOrNull()
            if (diaEventoInt == null || mesEventoInt == null || añoEventoInt == null) {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "La fecha debe tener el formato DD/MM/AAAA",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (diaEventoInt < 1 || diaEventoInt > 31 || mesEventoInt < 1 || mesEventoInt > 12 || añoEventoInt < 2024) {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "La fecha debe tener el formato DD/MM/AAAA",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val horaEvento = horaText.split(":")
            val horaEventoInt = horaEvento[0].toIntOrNull()
            val minutoEventoInt = horaEvento[1].toIntOrNull()
            if (horaEventoInt == null || minutoEventoInt == null) {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "La hora debe tener el formato HH:MM",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (horaEventoInt < 0 || horaEventoInt > 23 || minutoEventoInt < 0 || minutoEventoInt > 59) {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "La hora debe tener el formato HH:MM",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (lugarText.matches(Regex("^[0-9]*$"))) {
                Toast.makeText(
                    this@PublicarAnuncio,
                    "El lugar no puede ser solo números",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (currentUser == null) {
                Toast.makeText(this@PublicarAnuncio, "Usuario no autenticado", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("app/usuarios/${currentUser.uid}/mascotas")

            myRef.orderByChild("usuarioId").equalTo(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mascotasIdList = mascotasAñadidasList.map { it.id!! }
                    if (mascotasIdList.isEmpty()) {
                        Toast.makeText(
                            this@PublicarAnuncio,
                            "Debes tener al menos una mascota para publicar un anuncio",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val mascotaPicRef = database.getReference("app/usuarios/${currentUser.uid}/mascotas")
                    var todasLasMascotasTienenFoto = true

                    mascotaPicRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (mascota in mascotasAñadidasList) {
                                val mascotaPicValue = dataSnapshot.child(mascota.id!!).child("foto").value
                                if (mascotaPicValue == null || mascotaPicValue.toString().isEmpty()) {
                                    Toast.makeText(
                                        this@PublicarAnuncio,
                                        "La mascota ${mascota.nombre} no tiene foto",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    todasLasMascotasTienenFoto = false
                                    return
                                }
                                mascota.foto = mascotaPicValue.toString()
                            }

                            val myRefAnuncios = FirebaseDatabase.getInstance().getReference("app/anuncios")
                            val newAnuncioRef = myRefAnuncios.push()
                            val anuncioId = newAnuncioRef.key

                            if (todasLasMascotasTienenFoto) {
                                val anuncio = hashMapOf(
                                    "id" to anuncioId,
                                    "titulo" to tituloText,
                                    "descripcion" to descripcionText,
                                    "lugar" to lugarText,
                                    "fecha" to fechaText,
                                    "hora" to horaText,
                                    "usuarioDueño" to currentUser.uid,
                                    "estado" to "creado",
                                    "tipoAnuncio" to tipoAnuncioText,
                                    "precio" to precioText.toDouble(),
                                    "usuarioPaseador" to "",
                                    "idmascota" to mascotasIdList,
                                    "nombreMascota" to mascotasAñadidasList.map { it.nombre!! },
                                    "razaMascota" to mascotasAñadidasList.map { it.raza!! },
                                    "edadMascota" to mascotasAñadidasList.map { it.edad!! },
                                    "valoracionMascota" to mascotasAñadidasList.map { it.valoracion!! },
                                    "imagenMascota" to mascotasAñadidasList.map { it.foto?.let { foto -> foto } ?: "" }
                                )

                                // cambia el atributo borrable de las mascotas a false
                                for (mascota in mascotasAñadidasList) {
                                    val mascotaRef = FirebaseDatabase.getInstance()
                                        .getReference("app/usuarios/${mascota.usuarioId}/mascotas/${mascota.id}")
                                    mascotaRef.child("borrable").setValue(false)
                                }

                                newAnuncioRef.setValue(anuncio).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@PublicarAnuncio,
                                            "Anuncio publicado con éxito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this@PublicarAnuncio, MisAnuncios::class.java)
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            this@PublicarAnuncio,
                                            "Error al publicar el anuncio",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(
                                this@PublicarAnuncio,
                                "Error al obtener las fotos de las mascotas",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@PublicarAnuncio,
                        "Error al obtener las mascotas del usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView.visibility = View.VISIBLE
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

    override fun onMascotaAdded(mascota: Mascota) {
        mascotasAñadidasList.add(mascota)
        mascotaAdapter.notifyDataSetChanged()
    }
}