package com.example.trabajo_final

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class MensajePrivado(
    val id: String = "",
    val idsEmisorReceptor: String = "",
    val contenido: String = "",
    val fechaHora: Date = Date(),
    val idEmisor: String = "",
    val idAdaptador: Int = 0,
    var urlAvatar: String = "",
    val nombreEmisor: String = "",
    val tituloAnuncio: String = "",
    var estado_noti:Int? = null,
    var user_notificacion:String? = null
) : Parcelable