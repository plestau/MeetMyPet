package com.example.trabajo_final

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    var id : String? = null,
    var email: String? = null,
    var nombre: String? = null,
    var password: String? = null,
    var tipo: String? = null,
    var imagen: String? = null,
    var biografia: String? = null,
    var n_telefono: String? = null,
    var valoraciones: List<Float>? = null,
    var mascotas: HashMap<String, Mascota>? = null,
    var fecha_registro: String? = null,
    var estado_noti:Int? = null,
    var user_notificacion:String? = null,
): Parcelable, Comparable<Usuario> {
    override fun compareTo(other: Usuario): Int {
        return this.email!!.compareTo(other.email!!)
    }
}