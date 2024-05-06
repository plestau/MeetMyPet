package com.example.trabajo_final

import java.util.Date

data class MensajePublico(
    val id: String = "",
    val contenido: String = "",
    val fechaHora: Date = Date(),
    val idEmisor: String = "",
    val idAdaptador: Int = 0,
    val urlAvatar: String = "",
    val nombreEmisor: String = ""
)