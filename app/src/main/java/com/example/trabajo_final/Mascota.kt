package com.example.trabajo_final

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mascota(
    var id: String? = null,
    var raza: String? = null,
    var nombre: String? = null,
    var foto: String? = null,
    var edad: Int? = null,
    var sexo: String? = null,
    var esterilizado: Boolean? = null,
    var biografia: String? = null,
    var valoraciones: List<Float>? = null,
    var usuarioId: String? = null,
    var borrable: Boolean = true,
    var estado_noti:Int? = null,
): Parcelable