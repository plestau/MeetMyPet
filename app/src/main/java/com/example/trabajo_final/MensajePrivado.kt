package com.example.trabajo_final

import java.util.Date

data class MensajePrivado(
    val id: String = "",
    val idsEmisorReceptor: String = "",
    val contenido: String = "",
    val fechaHora: Date = Date(),
    val idEmisor: String = "",
    val idAdaptador: Int = 0,
    val urlAvatar: String = "",
    val nombreEmisor: String = "",
    val tituloAnuncio: String = ""
)