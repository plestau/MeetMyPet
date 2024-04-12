package com.example.trabajo_final

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Anuncio(
    var id: String? = null,
    var descripcion: String? = null,
    var fecha: String? = null,
    var hora: String? = null,
    var lugar: String? = null,
    var titulo: String? = null,
    var usuarioDueño: String? = null,
    var mascotas: List<Mascota>? = null,
    var usuarioPaseador: String? = null,
    var estado: String? = null
): Parcelable