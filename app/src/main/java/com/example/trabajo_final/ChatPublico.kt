package com.example.trabajo_final

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

class ChatPublico : AppCompatActivity() {
    private lateinit var adapter: ChatPublicoAdapter
    private lateinit var database: DatabaseReference
    private lateinit var txtMensaje: EditText
    private lateinit var btnEnviar: Button
    private val idUsuarioActual = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_publico)

        val fragmentInferior = FragmentInferior()
        supportFragmentManager.beginTransaction().add(R.id.fragment_inferior, fragmentInferior).commit()

        adapter = ChatPublicoAdapter(idUsuarioActual, findViewById(R.id.rvMensajes))
        val rvMensajes = findViewById<RecyclerView>(R.id.rvMensajes)
        rvMensajes.layoutManager = LinearLayoutManager(this)
        rvMensajes.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("app/chat_publico")

        txtMensaje = findViewById(R.id.txtMensaje)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            val mensaje = txtMensaje.text.toString()
            if (mensaje.isNotEmpty()) {
                enviarMensaje(mensaje)
                txtMensaje.text.clear()
            }
        }

        leerMensajes()
    }

    private fun enviarMensaje(contenido: String) {
        val id = database.push().key
        val fechaHora = Date()

        val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/$idUsuarioActual")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nombreEmisor = dataSnapshot.child("nombre").getValue(String::class.java) ?: ""
                val urlAvatar = dataSnapshot.child("profilePic").getValue(String::class.java) ?: ""
                val mensaje = MensajePublico(id!!, contenido, fechaHora, idUsuarioActual, 0, urlAvatar, nombreEmisor)
                database.child(id).setValue(mensaje)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun leerMensajes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                adapter.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val mensaje = postSnapshot.getValue(MensajePublico::class.java)
                    if (mensaje != null) {
                        adapter.addMensaje(mensaje)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
}