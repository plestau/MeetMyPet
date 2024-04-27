package com.example.trabajo_final.Java

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trabajo_final.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MensajesAdapter(c: Context) : RecyclerView.Adapter<HolderMensaje>() {
    private var listMensaje: MutableList<Mensaje> = ArrayList()
    private val c: Context

    init {
        listMensaje = listMensaje
        this.c = c
    }

    fun addMensaje(m: Mensaje) {
        listMensaje.add(m)
        notifyItemInserted(listMensaje.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMensaje {
        val v = LayoutInflater.from(c).inflate(R.layout.card_view_mensajes, parent, false)
        return HolderMensaje(v)
    }

    override fun onBindViewHolder(holder: HolderMensaje, position: Int) {
        holder.nombre.text = listMensaje[position].nombre
        holder.mensaje.text = listMensaje[position].mensaje
        holder.hora.text = listMensaje[position].hora
        if (listMensaje[position].type_mensaje == "2") {
            holder.fotoMensajeEnviado.visibility = View.VISIBLE
            holder.mensaje.visibility = View.VISIBLE
            Glide.with(c).load(listMensaje[position].urlFoto).into(holder.fotoMensajeEnviado)
            Log.d("url", listMensaje[position].urlFoto!!)
        } else if (listMensaje[position].type_mensaje == "1") {
            holder.fotoMensajeEnviado.visibility = View.GONE
            holder.mensaje.visibility = View.VISIBLE
        }
        if (listMensaje[position].fotoPerfil!!.isEmpty()) {
            holder.fotoMensajePerfil.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            Glide.with(c).load(listMensaje[position].fotoPerfil).into(holder.fotoMensajePerfil)
        }
        val databaseReference = FirebaseDatabase.getInstance().getReference("app/users/${listMensaje[position].nombre}/profilePic")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val profilePicUrl = dataSnapshot.getValue(String::class.java)
                if (profilePicUrl != null) {
                    Glide.with(c).load(profilePicUrl).into(holder.fotoMensajePerfil)
                } else {
                    holder.fotoMensajePerfil.setImageResource(R.mipmap.ic_launcher_round)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getItemCount(): Int {
        return listMensaje.size
    }
}
