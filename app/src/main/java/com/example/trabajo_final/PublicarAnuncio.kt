package com.example.trabajo_final

import FragmentInferior
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
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

class PublicarAnuncio : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var mascotasAñadidas: TextView
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar_anuncio)

        val fragmentInferior = FragmentInferior()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_inferior, fragmentInferior)
            commit()
        }

        val fragment = FragmentVerMisMascotas().apply {
            arguments = Bundle().apply {
                putBoolean("fromPublicarAnuncio", true)
            }
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val titulo = findViewById<EditText>(R.id.titulo)
        val descripcion = findViewById<EditText>(R.id.descripcion)
        val lugar = findViewById<EditText>(R.id.lugar)
        val fecha = findViewById<EditText>(R.id.fecha)
        val hora = findViewById<EditText>(R.id.hora)
        mascotasAñadidas = findViewById(R.id.mascotasAñadidas)

        val verMisMascotas = findViewById<LinearLayout>(R.id.verMisMascotas)
        verMisMascotas.setOnClickListener {
            val fragment = FragmentVerMisMascotas().apply {
                arguments = Bundle().apply {
                    putBoolean("fromPublicarAnuncio", true)
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
            val tituloText = titulo.text.toString()
            val descripcionText = descripcion.text.toString()
            val lugarText = lugar.text.toString()
            val fechaText = fecha.text.toString()
            val horaText = hora.text.toString()

            if (tituloText.isEmpty() || descripcionText.isEmpty() || lugarText.isEmpty() || fechaText.isEmpty() || horaText.isEmpty()) {
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
                    val mascotasIdList = mutableListOf<String>()
                    for (mascotaSnapshot in dataSnapshot.children) {
                        val mascota = mascotaSnapshot.getValue(Mascota::class.java)
                        if (mascota != null) {
                            mascotasIdList.add(mascota.id!!)
                        }
                    }

                    if (mascotasIdList.isEmpty()) {
                        Toast.makeText(
                            this@PublicarAnuncio,
                            "Debes tener al menos una mascota para publicar un anuncio",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val anuncio = hashMapOf(
                        "titulo" to tituloText,
                        "descripcion" to descripcionText,
                        "lugar" to lugarText,
                        "fecha" to fechaText,
                        "hora" to horaText,
                        "usuarioDueño" to currentUser.uid,
                        "mascotasId" to mascotasIdList,
                        "estado" to "creado",
                        "usuarioPaseador" to ""
                    )

                    val myRefAnuncios = database.getReference("app/anuncios")

                    myRefAnuncios.push().setValue(anuncio).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@PublicarAnuncio,
                                "Anuncio publicado con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@PublicarAnuncio, MainActivity::class.java)
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
}