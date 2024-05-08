package com.example.trabajo_final

import java.util.Date

data class SelectorChatsPrivados(
    val id: String = "",
    val idsEmisorReceptor: String = "",
    val contenido: String = "",
    val fechaHora: Date = Date(),
    val idAdaptador: Int = 0,
    val urlAvatar: String = "",
    val nombreEmisor: String = "",
    val tituloAnuncio: String = ""
)