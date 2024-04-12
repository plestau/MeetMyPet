package com.example.trabajo_final

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FragmentVerMisMascotas : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView

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
        val mascotasRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${currentUser?.uid}/mascotas")
        recyclerView = view.findViewById(R.id.recyclerViewMisMascotas)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val mascotas = mutableListOf<Mascota>()
        val mascotaAdapter = MascotaAdapter(mascotas, parentFragmentManager)
        recyclerView.adapter = mascotaAdapter

        mascotaAdapter.setOnItemClickListener(object : MascotaAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val mascota = mascotas[position]
                val mascotasAñadidas = activity?.findViewById<View>(R.id.mascotasAñadidas) as TextView
                val mascotasAñadidasList = mascotasAñadidas.text.toString().split("\n").filter { it.isNotEmpty() }

                // Comprueba si la mascota ya ha sido añadida
                if ("mascotas añadidas: ${mascota.nombre}" in mascotasAñadidasList || mascota.nombre in mascotasAñadidasList) {
                    Toast.makeText(context, "Esta mascota ya ha sido añadida", Toast.LENGTH_SHORT).show()
                    return
                }

                val intent = Intent()
                intent.putExtra("mascota", mascota)
                activity?.setResult(Activity.RESULT_OK, intent)
                activity?.findViewById<ScrollView>(R.id.scrollView)?.visibility = View.VISIBLE
                parentFragmentManager.beginTransaction().hide(this@FragmentVerMisMascotas).commit()
                mascotasAñadidas.text = mascotasAñadidas.text.toString() + "\n" + mascota.nombre
            }
        })

        mascotasRef.addValueEventListener(object :
            ValueEventListener {
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
                Toast.makeText(context, "Error al cargar las mascotas", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val fragmentInferior = parentFragmentManager.findFragmentById(R.id.fragment_inferior)
        if (fragmentInferior != null) {
            parentFragmentManager.beginTransaction().show(fragmentInferior).commit()
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
}