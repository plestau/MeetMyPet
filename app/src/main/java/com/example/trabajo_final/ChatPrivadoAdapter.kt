package com.example.trabajo_final

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.trabajo_final.Java.HolderMensaje
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class ChatPrivadoAdapter(private val idUsuarioActual: String, private val recyclerView: RecyclerView) : RecyclerView.Adapter<HolderMensaje>() {
    var listMensaje: MutableList<MensajePrivado> = ArrayList()

    fun addMensaje(m: MensajePrivado) {
        listMensaje.add(m)
        notifyItemInserted(listMensaje.size)
        recyclerView.scrollToPosition(listMensaje.size - 1)
    }

    fun clear() {
        listMensaje.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMensaje {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje, parent, false)
        return HolderMensaje(v)
    }

    override fun onBindViewHolder(holder: HolderMensaje, position: Int) {
        val sharedPref = holder.itemView.context.getSharedPreferences("userRole", 0)
        val userRol = sharedPref.getString("role", "user")
        val mensaje = listMensaje[position]
        val layoutMensajeEnviado = holder.itemView.findViewById<LinearLayout>(R.id.layoutMensajeEnviado)
        val layoutMensajeRecibido = holder.itemView.findViewById<LinearLayout>(R.id.layoutMensajeRecibido)
        Log.d("mensajeEmisor",mensaje.contenido +" "+ mensaje.idEmisor)
        Log.d("idUsuarioActual", idUsuarioActual)
        if (mensaje.idEmisor == idUsuarioActual) {
            layoutMensajeEnviado.visibility = View.VISIBLE
            layoutMensajeRecibido.visibility = View.GONE
            // Verificar si el mensaje es una URL de imagen
            if (mensaje.contenido.startsWith("https://") || mensaje.contenido.startsWith("http://")) {
                // Es una URL de imagen, cargar la imagen
                holder.mensajeEnviado.visibility = View.GONE
                holder.fotoMensajeEnviado.visibility = View.VISIBLE
                Glide.with(holder.itemView.context).load(mensaje.contenido).override(500, 500).into(holder.fotoMensajeEnviado)
            } else {
                // No es una URL de imagen, mostrar el texto
                holder.mensajeEnviado.visibility = View.VISIBLE
                holder.fotoMensajeEnviado.visibility = View.GONE
                holder.mensajeEnviado.text = mensaje.contenido
            }
            holder.nombreEnviado.text = "Yo"
            holder.mensajeEnviado.text = mensaje.contenido
            holder.horaEnviado.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(mensaje.fechaHora)
            Glide.with(holder.itemView.context).load(mensaje.urlAvatar).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfilEnviado)
        } else {
            layoutMensajeEnviado.visibility = View.GONE
            layoutMensajeRecibido.visibility = View.VISIBLE
            if (mensaje.contenido.startsWith("https://") || mensaje.contenido.startsWith("http://")) {
                // Es una URL de imagen, cargar la imagen
                holder.mensajeRecibido.visibility = View.GONE
                holder.fotoMensajeRecibido.visibility = View.VISIBLE
                Glide.with(holder.itemView.context).load(mensaje.contenido).override(500, 500).into(holder.fotoMensajeRecibido)
            } else {
                // No es una URL de imagen, mostrar el texto
                holder.mensajeRecibido.visibility = View.VISIBLE
                holder.fotoMensajeRecibido.visibility = View.GONE
                holder.mensajeRecibido.text = mensaje.contenido
            }
            holder.nombreRecibido.text = mensaje.nombreEmisor
            holder.mensajeRecibido.text = mensaje.contenido
            holder.horaRecibido.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(mensaje.fechaHora)
            Glide.with(holder.itemView.context).load(mensaje.urlAvatar).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfilRecibido)
            holder.fotoMensajePerfilRecibido.setOnClickListener {
                val intent = Intent(holder.itemView.context, PerfilUsuario::class.java)
                intent.putExtra("USER_ID", mensaje.idEmisor)
                holder.itemView.context.startActivity(intent)
            }
        }

        if (userRol == "admin") {
            holder.itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Mensaje")
                builder.setMessage("¿Qué quieres hacer con este mensaje?")
                builder.setPositiveButton("Eliminar") { dialog, _ ->
                    val mensajeRef = FirebaseDatabase.getInstance().getReference("app/chats_privados/${mensaje.idsEmisorReceptor}/${mensaje.id}")
                    mensajeRef.child("estado_noti").setValue(Estado.ELIMINADO)
                    mensajeRef.child("user_notificacion").setValue(mensaje.idEmisor)
                }
                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
                true
            }
        }
    }

    override fun getItemCount () = listMensaje.size
}