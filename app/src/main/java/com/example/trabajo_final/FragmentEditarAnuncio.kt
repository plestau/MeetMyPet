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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FragmentEditarAnuncio : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mascotasAñadidas: LinearLayout
    private lateinit var anuncio: Anuncio
    var mascotasAñadidasList = mutableListOf<Mascota>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        anuncio = arguments?.getParcelable<Anuncio>("anuncio")!!
        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val anuncioRef = database.getReference("app/anuncios").child(anuncio.id!!)
        val mascotaRef = database.getReference("app/usuarios").child(auth.currentUser!!.uid).child("mascotas")
        anuncioRef.child("idmascota").get().addOnSuccessListener { dataSnapshot ->
            val mascotasIdList = dataSnapshot.value as List<String>
            mascotasIdList.forEach { mascotaId ->
                mascotaRef.child(mascotaId).get().addOnSuccessListener { dataSnapshot ->
                    val mascota = dataSnapshot.getValue(Mascota::class.java)
                    if (mascota != null) {
                        mascotasAñadidasList.add(mascota)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (mascota in mascotasAñadidasList) {
            // Crea un nuevo TextView para el nombre de la mascota
            val mascotaTextView = TextView(context)
            mascotaTextView.text = mascota.nombre
            mascotaTextView.textSize = 20f

            // Crea un nuevo LinearLayout para contener el nombre de la mascota y el icono de borrar
            val mascotaLayout = LinearLayout(context)
            mascotaLayout.orientation = LinearLayout.HORIZONTAL
            mascotaLayout.addView(mascotaTextView)

            // Crea un nuevo ImageView para el icono de borrar
            val borrarImageView = ImageView(context)
            borrarImageView.setImageResource(R.drawable.baseline_delete_forever_24) // Reemplaza 'ic_delete' con el nombre de tu icono de borrar
            borrarImageView.setOnClickListener {
                // elimina a la mascota elegida del mascotasAñadidasLayout
                mascotasAñadidas.removeView(mascotaLayout)
                // elimina a la mascota elegida del mascotasAñadidasList
                mascotasAñadidasList.removeAll { it.nombre == mascota.nombre }
            }

            mascotaLayout.addView(borrarImageView)

            // Añade el LinearLayout al layout de mascotas añadidas
            mascotasAñadidas.addView(mascotaLayout)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_anuncio, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val titulo = view.findViewById<EditText>(R.id.titulo)
        val descripcion = view.findViewById<EditText>(R.id.descripcion)
        val lugar = view.findViewById<EditText>(R.id.lugar)
        val fecha = view.findViewById<EditText>(R.id.fecha)
        val hora = view.findViewById<EditText>(R.id.hora)

        anuncio = arguments?.getParcelable<Anuncio>("anuncio")!!
        Log.d("Anuncio", anuncio.toString())
        titulo.setText(anuncio.titulo)
        descripcion.setText(anuncio.descripcion)
        lugar.setText(anuncio.lugar)
        fecha.setText(anuncio.fecha)
        hora.setText(anuncio.hora)

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


        val btnEditarAnuncio = view.findViewById<Button>(R.id.editarAnuncio)
        btnEditarAnuncio.setOnClickListener {
            val tituloText = titulo.text.toString()
            val descripcionText = descripcion.text.toString()
            val lugarText = lugar.text.toString()
            val fechaText = fecha.text.toString()
            val horaText = hora.text.toString()

            // Aquí puedes agregar las validaciones necesarias para los campos de entrada
            if (tituloText.isEmpty() || descripcionText.isEmpty() || lugarText.isEmpty() || fechaText.isEmpty() || horaText.isEmpty()) {
                Toast.makeText(requireContext(), "Faltan datos en el formulario", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (mascotasAñadidasList.isEmpty()) {
                Toast.makeText(requireContext(), "Debes añadir al menos una mascota", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val horaEvento = horaText.split(":")
            val horaEventoInt = horaEvento[0].toIntOrNull()
            val minutoEventoInt = horaEvento[1].toIntOrNull()
            val diaEvento = fechaText.split("/")
            val diaEventoInt = diaEvento[0].toIntOrNull()
            val mesEventoInt = diaEvento[1].toIntOrNull()
            val añoEventoInt = diaEvento[2].toIntOrNull()

            if (diaEventoInt!! < 1 || diaEventoInt > 31 || mesEventoInt!! < 1 || mesEventoInt > 12 || añoEventoInt!! < 2024) {
                Toast.makeText(
                    requireContext(),
                    "La fecha debe tener el formato DD/MM/AAAA",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (horaEventoInt == null || minutoEventoInt == null) {
                Toast.makeText(
                    requireContext(),
                    "La hora debe tener el formato HH:MM",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (horaEventoInt < 0 || horaEventoInt > 23 || minutoEventoInt < 0 || minutoEventoInt > 59) {
                Toast.makeText(
                    requireContext(),
                    "La hora debe tener el formato HH:MM",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (lugarText.matches(Regex("^[0-9]*$"))) {
                Toast.makeText(
                    requireContext(),
                    "El lugar no puede ser solo números",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (currentUser == null) {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val anuncioRef = database.getReference("app/anuncios").child(anuncio.id!!)

            // Actualizar los campos del anuncio
            anuncioRef.child("titulo").setValue(tituloText)
            anuncioRef.child("descripcion").setValue(descripcionText)
            anuncioRef.child("lugar").setValue(lugarText)
            anuncioRef.child("fecha").setValue(fechaText)
            anuncioRef.child("hora").setValue(horaText)

            Toast.makeText(requireContext(), "Anuncio actualizado con éxito", Toast.LENGTH_SHORT)
                .show()

            activity?.onBackPressed()
        }

        return view
    }
}