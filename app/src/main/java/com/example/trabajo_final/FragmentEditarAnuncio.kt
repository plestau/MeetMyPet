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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FragmentEditarAnuncio : Fragment(), FragmentVerMisMascotas.OnMascotaAddedListener {

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
                        actualizarUIConMascotaAñadida(mascota)
                        Log.d("FragmentEditarAnuncio", "mascotasAñadidasList inside get(): $mascotasAñadidasList")
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_anuncio, container, false)
        mascotasAñadidas = view.findViewById(R.id.mascotasAñadidasLayout)

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
            // Realiza las validaciones y actualiza el anuncio
            // (Código de validación y actualización del anuncio aquí)
        }

        return view
    }

    // Método para actualizar la UI con la mascota añadida
    private fun actualizarUIConMascotaAñadida(mascota: Mascota) {
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
        borrarImageView.setImageResource(R.drawable.baseline_delete_forever_24) // Reemplaza 'baseline_delete_forever_24' con el nombre de tu icono de borrar
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

    // Método de la interfaz OnMascotaAddedListener
    override fun onMascotaAdded(mascota: Mascota) {
        mascotasAñadidasList.add(mascota)
        actualizarUIConMascotaAñadida(mascota)
    }
}