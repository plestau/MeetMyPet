package com.example.trabajo_final

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.trabajo_final.Java.HolderMensaje
import java.text.SimpleDateFormat
import java.util.Locale

class ChatPrivadoAdapter(private val idUsuarioActual: String) : RecyclerView.Adapter<HolderMensaje>() {
    private var listMensaje: MutableList<MensajePrivado> = ArrayList()

    fun addMensaje(m: MensajePrivado) {
        listMensaje.add(m)
        notifyItemInserted(listMensaje.size)
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
        val mensaje = listMensaje[position]
        holder.nombre.text = mensaje.nombreEmisor
        holder.mensaje.text = mensaje.contenido
        holder.hora.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(mensaje.fechaHora)
        Glide.with(holder.itemView.context).load(mensaje.urlAvatar).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfil)
    }

    override fun getItemCount () = listMensaje.size
}