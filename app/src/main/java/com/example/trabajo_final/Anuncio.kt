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
    var usuarioPaseador: String? = null,
    var estado: String? = null,
    var tipoAnuncio: String? = null,
    var precio: Double? = null,
    var idmascota: List<String>? = null,
    var nombreMascota: List<String>? = null,
    var razaMascota: List<String>? = null,
    var edadMascota: List<Int>? = null,
    var valoracionMascota: List<Float>? = null,
    var imagenMascota: List<String>? = null,
    var estado_noti:Int? = null,
    var user_notificacion:String? = null,
): Parcelable, Comparable<Anuncio> {
    override fun compareTo(other: Anuncio): Int {
        return this.titulo!!.compareTo(other.titulo!!)
    }
}