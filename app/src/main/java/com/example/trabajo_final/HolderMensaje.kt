package com.example.trabajo_final.Java

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trabajo_final.R

class HolderMensaje(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var nombreEnviado: TextView
    var mensajeEnviado: TextView
    var horaEnviado: TextView
    var fotoMensajePerfilEnviado: ImageView
    var fotoMensajeEnviado: ImageView

    var nombreRecibido: TextView
    var mensajeRecibido: TextView
    var horaRecibido: TextView
    var fotoMensajePerfilRecibido: ImageView
    var fotoMensajeRecibido: ImageView

    init {
        nombreEnviado = itemView.findViewById<View>(R.id.nombreMensajeEnviado) as TextView
        mensajeEnviado = itemView.findViewById<View>(R.id.mensajeMensajeEnviado) as TextView
        horaEnviado = itemView.findViewById<View>(R.id.horaMensajeEnviado) as TextView
        fotoMensajePerfilEnviado = itemView.findViewById<View>(R.id.fotoPerfilMensajeEnviado) as ImageView
        fotoMensajeEnviado = itemView.findViewById<View>(R.id.mensajeFotoEnviado) as ImageView

        nombreRecibido = itemView.findViewById<View>(R.id.nombreMensajeRecibido) as TextView
        mensajeRecibido = itemView.findViewById<View>(R.id.mensajeMensajeRecibido) as TextView
        horaRecibido = itemView.findViewById<View>(R.id.horaMensajeRecibido) as TextView
        fotoMensajePerfilRecibido = itemView.findViewById<View>(R.id.fotoPerfilMensajeRecibido) as ImageView
        fotoMensajeRecibido = itemView.findViewById<View>(R.id.mensajeFotoRecibido) as ImageView
    }
}