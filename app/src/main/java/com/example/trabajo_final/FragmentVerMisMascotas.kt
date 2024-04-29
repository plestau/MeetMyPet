package com.example.trabajo_final

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentVerMisMascotas : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView

    interface OnMascotaAddedListener {
        fun onMascotaAdded(mascota: Mascota)
    }
    private var listener: OnMascotaAddedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_mis_mascotas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val database = FirebaseDatabase.getInstance()
        val sharedPref = activity?.getSharedPreferences("userRole", 0)
        val userRole = sharedPref?.getString("role", "usuario")
        recyclerView = view.findViewById(R.id.recyclerViewMisMascotas)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val mascotas = mutableListOf<Mascota>()
        val mascotaAdapter = MascotaAdapter(mascotas, parentFragmentManager)
        recyclerView.adapter = mascotaAdapter

        val fromPublicarAnuncio = arguments?.getBoolean("fromPublicarAnuncio", false) ?: false

        if (userRole == "admin") {
            val usersRef = database.getReference("app/usuarios")
            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newMascotas = mutableListOf<Mascota>()
                    dataSnapshot.children.forEach { user ->
                        user.child("mascotas").children.forEach { child ->
                            val mascota = child.getValue(Mascota::class.java)
                            if (mascota != null) {
                                newMascotas.add(mascota)
                            }
                        }
                    }
                    mascotas.clear()
                    mascotas.addAll(newMascotas)
                    mascotaAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Error al cargar las mascotas", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        } else {
            val mascotasRef = database.getReference("app/usuarios/${currentUser?.uid}/mascotas")
            mascotasRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newMascotas = mutableListOf<Mascota>()
                    dataSnapshot.children.forEach { child ->
                        val mascota = child.getValue(Mascota::class.java)
                        if (mascota != null) {
                            newMascotas.add(mascota)
                        }
                    }
                    mascotas.clear()
                    mascotas.addAll(newMascotas)
                    mascotaAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Error al cargar las mascotas", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Verificar si viene de PublicarAnuncio y habilitar la funcionalidad correspondiente
        if (fromPublicarAnuncio) {
            mascotaAdapter.setOnItemClickListener(object : MascotaAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val fromPublicarAnuncio = arguments?.getBoolean("fromPublicarAnuncio", false) ?: false
                    if (!fromPublicarAnuncio) {
                        return
                    }
                    val mascota = mascotas[position]

                    // Comprueba si la mascota ya ha sido añadida
                    val mascotasAñadidasLayout = activity?.findViewById<LinearLayout>(R.id.mascotasAñadidasLayout)
                    mascotasAñadidasLayout?.forEach { view ->
                        if (view is LinearLayout) {
                            val textView = view.getChildAt(0) as TextView
                            if (textView.text == mascota.nombre) {
                                Toast.makeText(context, "La mascota ya ha sido añadida", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }

                    AlertDialog.Builder(context)
                        .setTitle("Confirmación")
                        .setMessage("¿Seguro que quieres añadir a ${mascota.nombre}?")
                        .setPositiveButton("Sí") { _, _ ->
                            listener?.onMascotaAdded(mascota)
                            val intent = Intent()
                            intent.putExtra("mascota", mascota)
                            activity?.setResult(Activity.RESULT_OK, intent)
                            // Comprueba si la actividad actual es MisAnuncios o PublicarAnuncio antes de intentar mostrar la vista scrollView
                            if (activity is MisAnuncios || activity is PublicarAnuncio) {
                                activity?.findViewById<ScrollView>(R.id.scrollView)?.visibility = View.VISIBLE
                            }
                            parentFragmentManager.beginTransaction().hide(this@FragmentVerMisMascotas).commit()
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
                                // elimina a la mascota elegida del mascotasAñadidasLayout despues de confirmarlo
                                (activity as? PublicarAnuncio)?.mascotasAñadidasList?.removeAll { it.nombre == mascota.nombre }
                                mascotasAñadidasLayout?.removeView(mascotaLayout)
                                Log.d("MascotasAñadidasList", (activity as? PublicarAnuncio)?.mascotasAñadidasList.toString())
                            }

                            mascotaLayout.addView(borrarImageView)

                            // Añade el LinearLayout al layout de mascotas añadidas
                            val mascotasAñadidasLayout = activity?.findViewById<LinearLayout>(R.id.mascotasAñadidasLayout)
                            mascotasAñadidasLayout?.addView(mascotaLayout)

                            parentFragmentManager.popBackStack()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val fragmentInferior = parentFragmentManager.findFragmentById(R.id.fragment_inferior)
        if (fragmentInferior != null) {
            parentFragmentManager.beginTransaction().show(fragmentInferior).commit()
        }
        val fragmentSuperiorPerfil = parentFragmentManager.findFragmentById(R.id.fragment_superior_perfil)
        if (fragmentSuperiorPerfil != null) {
            parentFragmentManager.beginTransaction().show(fragmentSuperiorPerfil).commit()
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.findViewById<ScrollView>(R.id.scrollView)?.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        activity?.findViewById<ScrollView>(R.id.scrollView)?.visibility = View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMascotaAddedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnMascotaAddedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}