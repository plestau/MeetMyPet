package com.example.trabajo_final

import FragmentInferior
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
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
        rvPrivateChats.adapter = ChatPrivadoAdapter(privateChats)

        // Configurar el botón para ir al chat público
        btnPublicChat.setOnClickListener {
            val intent = Intent(this, ChatPublico::class.java)
            startActivity(intent)
        }

        // carga el último mensaje y la url de la imagen de usuario del chat público
        val database = FirebaseDatabase.getInstance().getReference("app/chat_publico")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val ultimoMensaje = dataSnapshot.children.lastOrNull()?.getValue(MensajePublico::class.java)
                Log.d("Mensaje final", "Ultimo mensaje: $ultimoMensaje")
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

    private fun getPrivateChats(): List<ChatPrivado> {
        // Implementa la lógica para obtener los chats privados
        // Este es solo un ejemplo y deberías reemplazarlo con tu propia lógica
        return listOf(
            ChatPrivado("id1", "emisor1@repeptor1", "Mensaje de prueba1", Date(), 1, "urlAvatar1", "nombreEmisor1"),
            ChatPrivado("id2", "emisor2@repeptor2", "Mensaje de prueba2", Date(), 2, "urlAvatar2", "nombreEmisor2"),
            ChatPrivado("id3", "emisor3@repeptor3", "Mensaje de prueba3", Date(), 3, "urlAvatar3", "nombreEmisor3")
        )
    }
}