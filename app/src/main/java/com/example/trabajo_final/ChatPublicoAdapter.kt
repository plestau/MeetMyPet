package com.example.trabajo_final

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.trabajo_final.Java.HolderMensaje
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class ChatPublicoAdapter(private val idUsuarioActual: String) : RecyclerView.Adapter<HolderMensaje>() {
    private var listMensaje: MutableList<MensajePublico> = ArrayList()

    fun addMensaje(m: MensajePublico) {
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
        val sharedPref = holder.itemView.context.getSharedPreferences("userRole", 0)
        val userRol = sharedPref.getString("role", "user")
        val mensaje = listMensaje[position]
        holder.nombre.text = if (mensaje.idEmisor == idUsuarioActual) "Yo" else mensaje.nombreEmisor
        holder.mensaje.text = mensaje.contenido
        holder.hora.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(mensaje.fechaHora)
        Glide.with(holder.itemView.context).load(mensaje.urlAvatar).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfil)
        if (userRol == "admin") {
            holder.itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Mensaje")
                builder.setMessage("¿Qué quieres hacer con este mensaje?")
                builder.setPositiveButton("Eliminar") { dialog, _ ->
                    val mensajeRef = FirebaseDatabase.getInstance().getReference("app/chat_publico/${mensaje.id}")
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