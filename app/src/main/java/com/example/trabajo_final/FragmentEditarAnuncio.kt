package com.example.trabajo_final

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.math.BigDecimal

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
                valoraciones = anuncio.valoracionMascota?.get(index)?.let { listOf(it) },
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
        titulo.setText(anuncio.titulo?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        val descripcion = view.findViewById<EditText>(R.id.descripcion)
        val lugar = view.findViewById<EditText>(R.id.lugar)
        lugar.setText(anuncio.lugar?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        val fecha = view.findViewById<EditText>(R.id.fecha)
        val hora = view.findViewById<EditText>(R.id.hora)
        val precio = view.findViewById<TextView>(R.id.precio)
        val tipoAnuncioSpinner = view.findViewById<Spinner>(R.id.tipoAnuncio)

        // Crea un ArrayAdapter usando el array de tipos de anuncios
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tipos_anuncios_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Aplica el adaptador al Spinner
        tipoAnuncioSpinner.adapter = adapter

        // Mapea el tipo de anuncio almacenado a los valores del Spinner
        val tipoAnuncioText = when (anuncio.tipoAnuncio) {
            "Paseo" -> "Paseo de mascotas"
            "Cuidado casa dueño" -> "Cuidado a domicilio en casa del dueño"
            "Cuidado casa paseador" -> "Cuidado a domicilio en casa del paseador"
            else -> ""
        }

        // Establece el valor seleccionado del Spinner al tipo de anuncio del anuncio actual
        val tipoAnuncioPosition = adapter.getPosition(tipoAnuncioText)
        tipoAnuncioSpinner.setSelection(tipoAnuncioPosition)

        // Carga los datos del anuncio en los EditText
        titulo.setText(anuncio.titulo?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        descripcion.setText(anuncio.descripcion)
        lugar.setText(anuncio.lugar?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        fecha.setText(anuncio.fecha)
        hora.setText(anuncio.hora)
        precio.text = anuncio.precio?.toString()

        // Maneja el clic en el botón "Ver Mis Mascotas"
        val verMisMascotas = view.findViewById<LinearLayout>(R.id.verMisMascotas)
        verMisMascotas.setOnClickListener {
            val fragment = FragmentVerMisMascotas().apply {
                arguments = Bundle().apply {
                    putBoolean("fromPublicarAnuncio", true)
                    putBoolean("mascotasClicables", true)
                    putString("USER_ID", auth.currentUser?.uid) // Pass the user ID
                    putParcelableArrayList("mascotasAñadidasList", ArrayList(mascotasAñadidasList))
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
                    val anuncioRef = database.getReference("app/anuncios").child(anuncio.id!!)
                    anuncioRef.child("user_notificacion").setValue(anuncio.usuarioDueño)
                    anuncioRef.child("estado_noti").setValue(Estado.ELIMINADO).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            activity?.onBackPressed()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error al marcar el anuncio como borrado",
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
            val precioText = precio.text.toString()
            val tipoAnuncioText = when (tipoAnuncioSpinner.selectedItem) {
                "Paseo de mascotas" -> "Paseo"
                "Cuidado a domicilio en casa del dueño" -> "Cuidado casa dueño"
                "Cuidado a domicilio en casa del paseador" -> "Cuidado casa paseador"
                else -> ""
            }
            if (precioText.toFloat() < 0) {
                Toast.makeText(
                    context,
                    "El precio no puede ser negativo",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (tituloText.isEmpty() || descripcionText.isEmpty() || lugarText.isEmpty() || fechaText.isEmpty() || horaText.isEmpty() || precioText.isEmpty() || tipoAnuncioSpinner.selectedItem == "Seleccione tipo de anuncio:") {
                Toast.makeText(
                    context,
                    "Todos los campos deben estar llenos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!fechaText.matches(Regex("^\\d{1,2}/\\d{1,2}/\\d{2}(\\d{2})?$"))) {
                Toast.makeText(
                    context,
                    "La fecha debe tener el formato D/M/AA o D/M/AAAA",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val fechaEvento = fechaText.split("/")
            val diaEventoInt = fechaEvento[0].toIntOrNull()
            val mesEventoInt = fechaEvento[1].toIntOrNull()
            var añoEventoInt = fechaEvento[2].toIntOrNull()

            // Añade "20" al principio del año si solo tiene dos dígitos
            if (añoEventoInt != null && añoEventoInt < 100) {
                añoEventoInt += 2000
            }
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
            val mascotasValoracionList = mascotasAñadidasList.map { it.valoraciones?.get(0)?.toFloat() ?: 0f }
            val mascotasImagenList = mascotasAñadidasList.map { it.foto!! }

            anuncioRef.setValue(anuncio.copy(
                titulo = tituloText.lowercase(),
                descripcion = descripcionText,
                lugar = lugarText.lowercase(),
                fecha = Utilidades.convertDateToStandardFormat(fechaText),
                hora = horaText,
                precio = precioText.toDouble(),
                tipoAnuncio = tipoAnuncioText,
                idmascota = mascotasIdList,
                nombreMascota = mascotasNombreList,
                razaMascota = mascotasRazaList,
                edadMascota = mascotasEdadList,
                valoracionMascota = mascotasValoracionList,
                imagenMascota = mascotasImagenList,
                estado_noti = Estado.EDITADO,
                user_notificacion = anuncio.usuarioDueño
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
