package com.example.trabajo_final

import FragmentInferior
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.util.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

class ChatPrivado : AppCompatActivity() {
    private lateinit var adapter: ChatPrivadoAdapter
    private lateinit var database: DatabaseReference
    private lateinit var txtMensaje: EditText
    private lateinit var btnEnviar: Button
    private val idUsuarioActual = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_privado)

        val idUsuarioDueñoAnuncio = intent.getStringExtra("idUsuarioDueñoAnuncio") ?: ""
        val idChatPrivado = intent.getStringExtra("idChatPrivado") ?: ""
        val idUsuarioActual = idChatPrivado.split("@")[0]

        // Actualizar la vista con los datos del usuario receptor
        val tvNombreReceptor = findViewById<TextView>(R.id.nombreReceptor)
        val ivFotoPerfilReceptor = findViewById<ImageView>(R.id.fotoPerfilReceptor)
        val nombreReceptor = intent.getStringExtra("nombreReceptor") ?: ""
        val urlAvatar = intent.getStringExtra("urlAvatar") ?: ""
        tvNombreReceptor.text = nombreReceptor
        Glide.with(this@ChatPrivado).load(urlAvatar).transform(CircleCrop()).placeholder(Utilidades.animacion_carga(this@ChatPrivado)).into(ivFotoPerfilReceptor)

        // Verificar si el usuario actual es uno de los usuarios del chat
        if (idUsuarioActual == idUsuarioDueñoAnuncio || idUsuarioActual == idUsuarioActual) {
            val fragmentInferior = FragmentInferior()
            supportFragmentManager.beginTransaction().add(R.id.fragment_inferior, fragmentInferior).commit()

            adapter = ChatPrivadoAdapter(idUsuarioActual)
            val rvMensajes = findViewById<RecyclerView>(R.id.rvMensajes)
            rvMensajes.layoutManager = LinearLayoutManager(this)
            rvMensajes.adapter = adapter

            val idChatPrivado = intent.getStringExtra("idChatPrivado") ?: ""
            Log.d("ChatPrivado", "ID chat privado: $idChatPrivado")
            database = FirebaseDatabase.getInstance().getReference("app/chats_privados/$idChatPrivado")

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
        } else {
            Toast.makeText(this, "No tienes permiso para ver esta conversación", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun enviarMensaje(contenido: String) {
        val fechaHora = Date()

        val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/$idUsuarioActual")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nombreEmisor = dataSnapshot.child("nombre").getValue(String::class.java) ?: ""
                val urlAvatar = dataSnapshot.child("profilePic").getValue(String::class.java) ?: ""
                val idChatPrivado = intent.getStringExtra("idChatPrivado") ?: ""
                val tituloAnuncio = intent.getStringExtra("tituloAnuncio") ?: ""

                val fotoPerfilReceptor = findViewById<ImageView>(R.id.fotoPerfilReceptor)
                val nombreReceptor = findViewById<TextView>(R.id.nombreReceptor)
                nombreReceptor.text = nombreEmisor
                Glide.with(this@ChatPrivado).load(urlAvatar).transform(CircleCrop()).placeholder(Utilidades.animacion_carga(this@ChatPrivado)).into(fotoPerfilReceptor)

                // Verificar si ya existe un chat privado
                val chatPrivadoRef = FirebaseDatabase.getInstance().getReference("app/chats_privados/$idChatPrivado")
                chatPrivadoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(chatSnapshot: DataSnapshot) {
                        // Agregar el nuevo mensaje
                        val id = database.push().key
                        // comprueba con el idchatPrivado si el usuario actual es el dueño del anuncio o el usuario que ha enviado el mensaje
                        val idReceptor = if (idUsuarioActual == idChatPrivado.split("@")[0]) idChatPrivado.split("@")[1] else idChatPrivado.split("@")[0]
                        val mensaje = MensajePrivado(id!!, idChatPrivado, contenido, fechaHora, idUsuarioActual, 0, urlAvatar, nombreEmisor, tituloAnuncio, Estado.CREADO, idReceptor)
                        chatPrivadoRef.child(id).setValue(mensaje)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors.
                    }
                })
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
                    val mensaje = postSnapshot.getValue(MensajePrivado::class.java)
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