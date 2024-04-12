package com.example.trabajo_final

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        suspend fun guardarProfilePic(sto_ref: StorageReference, id:String, imagen: Uri):String{
            lateinit var url_firebase: Uri

            url_firebase=sto_ref.child("app").child("usuarios").child(id)
                .putFile(imagen).await().storage.downloadUrl.await()

            return url_firebase.toString()
        }

        fun tostadaCorrutina(activity: AppCompatActivity, contexto: Context, texto:String){
            activity.runOnUiThread{
                Toast.makeText(
                    contexto,
                    texto,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun obtenerFechaActual():String{
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
            return sdf.format(Date())
        }
    }
}