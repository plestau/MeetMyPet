package com.example.trabajo_final

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.Date

class Utilidades {
    companion object{
        fun obtenerListaUsuarios(db_ref: DatabaseReference): MutableList<Usuario> {
            val lista_usuarios: MutableList<Usuario> = mutableListOf()

            db_ref.child("app").child("usuarios").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista_usuarios.clear()
                    for (usuario in snapshot.children) {
                        val pojo_usuario = usuario.getValue(Usuario::class.java)
                        lista_usuarios.add(pojo_usuario!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("error", error.toString())
                }
            })
            return lista_usuarios
        }

        fun existeUsuario(lista_usuarios:MutableList<Usuario>, email:String):Boolean{
            var existe = false
            for (usuario in lista_usuarios){
                if (usuario.email == email){
                    existe = true
                    break
                }
            }
            return existe
        }

        fun obtenerFechaActual():String{
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
            return sdf.format(Date())
        }

        fun animacion_carga(contexto: Context): CircularProgressDrawable {
            val animacion = CircularProgressDrawable(contexto)
            animacion.strokeWidth = 5f
            animacion.centerRadius = 30f
            animacion.start()
            return animacion
        }

        fun opcionesGlide(context: Context): RequestOptions {
            val options = RequestOptions()
                .placeholder(animacion_carga(context))
                .fallback(R.drawable.error_404)
                .error(R.drawable.error_404)
            return options
        }
    }
}