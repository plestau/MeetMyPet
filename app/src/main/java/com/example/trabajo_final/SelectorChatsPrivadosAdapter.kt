package com.example.trabajo_final

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class SelectorChatsPrivadosAdapter(private val privateChats: List<SelectorChatsPrivados>) : RecyclerView.Adapter<SelectorChatsPrivadosAdapter.PrivateChatViewHolder>() {

    class PrivateChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvContenido: TextView = itemView.findViewById(R.id.tv_contenido)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tv_fecha_hora)
        val tvTituloAnuncio: TextView = itemView.findViewById(R.id.tv_titulo_anuncio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selector_chat_privado, parent, false)
        return PrivateChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrivateChatViewHolder, position: Int) {
        val privateChat = privateChats[position]
        holder.tvContenido.text = privateChat.contenido
        holder.tvFechaHora.text = privateChat.fechaHora.toString()
        holder.tvTituloAnuncio.text = privateChat.tituloAnuncio
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(privateChat.fechaHora)
        holder.tvFechaHora.text = formattedDate
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatPrivado::class.java)
            val idUsuarioDueñoAnuncio = privateChat.idsEmisorReceptor[0]
            val tituloAnuncio = privateChat.tituloAnuncio
            val idChatPrivado = privateChat.idsEmisorReceptor
            intent.putExtra("idUsuarioDueñoAnuncio", idUsuarioDueñoAnuncio)
            intent.putExtra("tituloAnuncio", tituloAnuncio)
            intent.putExtra("idChatPrivado", idChatPrivado)
            holder.itemView.context.startActivity(intent)
        }

        val lastMessageRef = FirebaseDatabase.getInstance().getReference("app/chats_privados/${privateChat.idsEmisorReceptor}")
        lastMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lastMessage = dataSnapshot.children.lastOrNull()?.getValue(MensajePrivado::class.java)
                if (lastMessage != null) {
                    val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${lastMessage.idEmisor}")
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val urlAvatar = dataSnapshot.child("profilePic").getValue(String::class.java) ?: ""
                            Glide.with(holder.itemView.context).load(urlAvatar).transform(CircleCrop()).placeholder(Utilidades.animacion_carga(holder.itemView.context)).into(holder.ivAvatar)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(holder.itemView.context, "Error al cargar la imagen de perfil", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(holder.itemView.context, "Error al cargar el último mensaje", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getItemCount() = privateChats.size
}