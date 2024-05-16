package com.example.trabajo_final

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.trabajo_final.Java.HolderMensaje
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
        val layoutMensajeEnviado = holder.itemView.findViewById<LinearLayout>(R.id.layoutMensajeEnviado)
        val layoutMensajeRecibido = holder.itemView.findViewById<LinearLayout>(R.id.layoutMensajeRecibido)
        val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/${mensaje.idEmisor}")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val updatedProfilePicUrl = dataSnapshot.child("profilePic").getValue(String::class.java)
                // Actualiza la foto de perfil del usuario en el mensaje
                if (mensaje.idEmisor == idUsuarioActual) {
                    Glide.with(holder.itemView.context).load(updatedProfilePicUrl).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfilEnviado)
                } else {
                    Glide.with(holder.itemView.context).load(updatedProfilePicUrl).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfilRecibido)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ChatPublicoAdapter", "loadPost:onCancelled", databaseError.toException())
            }
        })

        if (mensaje.idEmisor == idUsuarioActual) {
            layoutMensajeEnviado.visibility = View.VISIBLE
            layoutMensajeRecibido.visibility = View.GONE
            holder.nombreEnviado.text = "Yo"
            holder.mensajeEnviado.text = mensaje.contenido
            holder.horaEnviado.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(mensaje.fechaHora)
            Glide.with(holder.itemView.context).load(mensaje.urlAvatar).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfilEnviado)
        } else {
            layoutMensajeEnviado.visibility = View.GONE
            layoutMensajeRecibido.visibility = View.VISIBLE
            holder.nombreRecibido.text = mensaje.nombreEmisor
            holder.mensajeRecibido.text = mensaje.contenido
            holder.horaRecibido.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(mensaje.fechaHora)
            Glide.with(holder.itemView.context).load(mensaje.urlAvatar).placeholder(Utilidades.animacion_carga(holder.itemView.context)).transform(CircleCrop()).into(holder.fotoMensajePerfilRecibido)
        }

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