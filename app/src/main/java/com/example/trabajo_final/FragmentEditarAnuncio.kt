package com.example.trabajo_final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FragmentEditarAnuncio : Fragment(), FragmentVerMisMascotas.OnMascotaAddedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var anuncio: Anuncio
    var mascotasAñadidasList = mutableListOf<Mascota>()
    private lateinit var mascotaAdapter: MascotaEnAnuncioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        anuncio = arguments?.getParcelable<Anuncio>("anuncio")!!
        auth = FirebaseAuth.getInstance()

        // Inicializa mascotasAñadidasList con las mascotas del anuncio
        mascotasAñadidasList = anuncio.nombreMascota?.mapIndexed { index, nombre ->
            Mascota(
                id = anuncio.idmascota?.get(index),
                nombre = nombre,
                raza = anuncio.razaMascota?.get(index),
                edad = anuncio.edadMascota?.get(index),
                valoracion = anuncio.valoracionMascota?.get(index),
                foto = anuncio.imagenMascota?.get(index)
            )
        }?.toMutableList() ?: mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_anuncio, container, false)
        val mascotasAñadidas = view.findViewById<RecyclerView>(R.id.mascotasAñadidasRv)
        mascotaAdapter = MascotaEnAnuncioAdapter(mascotasAñadidasList) { mascota ->
            mascotasAñadidasList.removeAll { it.id == mascota.id }
            mascotaAdapter.notifyDataSetChanged()
        }
        mascotasAñadidas.adapter = mascotaAdapter
        mascotasAñadidas.layoutManager = LinearLayoutManager(context)

        val titulo = view.findViewById<EditText>(R.id.titulo)
        val descripcion = view.findViewById<EditText>(R.id.descripcion)
        val lugar = view.findViewById<EditText>(R.id.lugar)
        val fecha = view.findViewById<EditText>(R.id.fecha)
        val hora = view.findViewById<EditText>(R.id.hora)

        // Carga los datos del anuncio en los EditText
        titulo.setText(anuncio.titulo)
        descripcion.setText(anuncio.descripcion)
        lugar.setText(anuncio.lugar)
        fecha.setText(anuncio.fecha)
        hora.setText(anuncio.hora)

        // Maneja el clic en el botón "Ver Mis Mascotas"
        val verMisMascotas = view.findViewById<LinearLayout>(R.id.verMisMascotas)
        verMisMascotas.setOnClickListener {
            val fragment = FragmentVerMisMascotas().apply {
                arguments = Bundle().apply {
                    putBoolean("fromPublicarAnuncio", true)
                    putBoolean("mascotasClicables", true)
                }
                setTargetFragment(this@FragmentEditarAnuncio, 0)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        val btnBorrarAnuncio = view.findViewById<ImageView>(R.id.borrarAnuncio)
        btnBorrarAnuncio.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro de que quieres borrar este anuncio?")
                .setPositiveButton("Sí") { _, _ ->
                    val database = FirebaseDatabase.getInstance()
                    // anuncioRef es la referencia al anuncio que se desea borrar que coge el id del anuncio de Firebase
                    val anuncioRef = database.getReference("app/anuncios").child(anuncio.id!!)
                    anuncioRef.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Anuncio borrado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity?.onBackPressed()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error al borrar el anuncio",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Maneja el clic en el botón "Editar Anuncio"
        val btnEditarAnuncio = view.findViewById<Button>(R.id.editarAnuncio)
        btnEditarAnuncio.setOnClickListener {
            val tituloText = titulo.text.toString()
            val descripcionText = descripcion.text.toString()
            val lugarText = lugar.text.toString()
            val fechaText = fecha.text.toString()
            val horaText = hora.text.toString()

            if (!fechaText.matches(Regex("^\\d{1,2}/\\d{1,2}/\\d{4}$"))) {
                Toast.makeText(
                    context,
                    "La fecha debe tener el formato D/M/AAAA",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (tituloText.isEmpty() || descripcionText.isEmpty() || lugarText.isEmpty() || fechaText.isEmpty() || horaText.isEmpty()) {
                Toast.makeText(
                    context,
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
                    context,
                    "La fecha debe tener el formato DD/MM/AAAA",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (diaEventoInt < 1 || diaEventoInt > 31 || mesEventoInt < 1 || mesEventoInt > 12 || añoEventoInt < 2024) {
                Toast.makeText(
                    context,
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
                    context,
                    "La hora debe tener el formato HH:MM",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (horaEventoInt < 0 || horaEventoInt > 23 || minutoEventoInt < 0 || minutoEventoInt > 59) {
                Toast.makeText(
                    context,
                    "La hora debe tener el formato HH:MM",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (lugarText.matches(Regex("^[0-9]*$"))) {
                Toast.makeText(
                    context,
                    "El lugar no puede ser solo números",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val anuncioRef = database.getReference("app/anuncios").child(anuncio.id!!)
            val mascotasIdList = mascotasAñadidasList.map { it.id!! }
            val mascotasNombreList = mascotasAñadidasList.map { it.nombre!! }
            val mascotasRazaList = mascotasAñadidasList.map { it.raza!! }
            val mascotasEdadList = mascotasAñadidasList.map { it.edad!! }
            val mascotasValoracionList = mascotasAñadidasList.map { it.valoracion!! }
            val mascotasImagenList = mascotasAñadidasList.map { it.foto!! }

            anuncioRef.setValue(anuncio.copy(
                titulo = tituloText,
                descripcion = descripcionText,
                lugar = lugarText,
                fecha = fechaText,
                hora = horaText,
                idmascota = mascotasIdList,
                nombreMascota = mascotasNombreList,
                razaMascota = mascotasRazaList,
                edadMascota = mascotasEdadList,
                valoracionMascota = mascotasValoracionList,
                imagenMascota = mascotasImagenList
            ))

            Toast.makeText(context, "Anuncio editado correctamente", Toast.LENGTH_SHORT).show()
            activity?.onBackPressed()
        }
        return view
    }

    override fun onMascotaAdded(mascota: Mascota) {
        mascotasAñadidasList.add(mascota)
        mascotaAdapter.notifyDataSetChanged()
    }
}
