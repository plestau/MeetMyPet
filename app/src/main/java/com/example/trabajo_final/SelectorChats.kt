package com.example.trabajo_final

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date

class SelectorChats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selector_chats)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fragmentInferior = FragmentInferior()
        supportFragmentManager.beginTransaction().add(R.id.fragment_inferior, fragmentInferior).commit()

        val btnPublicChat = findViewById<CardView>(R.id.cardViewPublicChat)
        val rvPrivateChats = findViewById<RecyclerView>(R.id.rv_private_chats)

        val privateChats = getPrivateChats()
        rvPrivateChats.layoutManager = LinearLayoutManager(this)
        rvPrivateChats.adapter = SelectorChatsPrivadosAdapter(privateChats)

        // Configurar el botón para ir al chat público
        btnPublicChat.setOnClickListener {
            val intent = Intent(this, ChatPublico::class.java)
            startActivity(intent)
        }

        // carga el último mensaje y la url de la imagen de usuario del chat público
        val database = FirebaseDatabase.getInstance().getReference("app/chat_publico")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val ultimoMensaje = dataSnapshot.children.lastOrNull()?.getValue(MensajePublico::class.java)
                findViewById<TextView>(R.id.tv_last_public_message).text = ultimoMensaje?.contenido
                findViewById<TextView>(R.id.tv_fecha_hora).text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ultimoMensaje?.fechaHora ?: Date())
                val urlAvatar = ultimoMensaje?.urlAvatar ?: ""
                val ivAvatar = findViewById<ImageView>(R.id.iv_avatar)
                Glide.with(this@SelectorChats).load(urlAvatar).transform(CircleCrop()).placeholder(Utilidades.animacion_carga(this@SelectorChats)).into(ivAvatar)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getPrivateChats(): List<SelectorChatsPrivados>{
        val chatsPrivados = mutableListOf<SelectorChatsPrivados>()
        val idUsuarioActual = FirebaseAuth.getInstance().currentUser!!.uid
        val sharedPref = getSharedPreferences("userRole", Context.MODE_PRIVATE)
        val userRol = sharedPref.getString("role", "user")
        val chatsPrivadosRef = FirebaseDatabase.getInstance().getReference("app/chats_privados")
        chatsPrivadosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatsPrivados.clear()
                for (chatSnapshot in dataSnapshot.children) {
                    val chatPrivado = chatSnapshot.children.lastOrNull()?.getValue(SelectorChatsPrivados::class.java)
                    if (chatPrivado != null && (chatPrivado.idsEmisorReceptor.contains(idUsuarioActual)) || userRol == "admin") {
                        if (!chatsPrivados.any { it.idsEmisorReceptor == chatPrivado!!.idsEmisorReceptor }) {
                            chatsPrivados.add(chatPrivado!!)
                        }
                    }
                }
                // Actualiza el adaptador del RecyclerView
                val rvPrivateChats = findViewById<RecyclerView>(R.id.rv_private_chats)
                rvPrivateChats.adapter = SelectorChatsPrivadosAdapter(chatsPrivados)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })

        return chatsPrivados
    }

    override fun onResume() {
        super.onResume()
        FragmentInferior.actividadActual = "Mensajes"
    }
}