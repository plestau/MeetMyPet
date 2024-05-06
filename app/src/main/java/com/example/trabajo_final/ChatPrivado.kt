package com.example.trabajo_final

import java.util.Date

data class ChatPrivado(
    val id: String,
    val idsEmisorReceptor: String,
    val contenido: String,
    val fechaHora: Date,
    val idAdaptador: Int,
    val urlAvatar: String,
    val nombreEmisor: String
)