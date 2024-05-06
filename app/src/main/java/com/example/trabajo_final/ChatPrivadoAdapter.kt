package com.example.trabajo_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class ChatPrivadoAdapter(private val privateChats: List<ChatPrivado>) : RecyclerView.Adapter<ChatPrivadoAdapter.PrivateChatViewHolder>() {

    class PrivateChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvContenido: TextView = itemView.findViewById(R.id.tv_contenido)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tv_fecha_hora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_privado, parent, false)
        return PrivateChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrivateChatViewHolder, position: Int) {
        val privateChat = privateChats[position]
        Glide.with(holder.itemView.context).load(privateChat.urlAvatar).into(holder.ivAvatar)
        holder.tvContenido.text = privateChat.contenido
        holder.tvFechaHora.text = privateChat.fechaHora.toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(privateChat.fechaHora)
        holder.tvFechaHora.text = formattedDate
    }

    override fun getItemCount() = privateChats.size
}