package com.example.trabajo_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class MascotaEnAnuncioAdapter(
    val mascotas: MutableList<Mascota>,
    private val onMascotaRemoved: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaEnAnuncioAdapter.MascotaViewHolder>() {

    inner class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mascotaNombreTextView: TextView = itemView.findViewById(R.id.mascotaNombreTextView)
        val borrarMascotaImageView: ImageView = itemView.findViewById(R.id.borrarMascotaImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mascota_en_anuncio, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = mascotas[position]
        holder.mascotaNombreTextView.text = mascota.nombre
        holder.mascotaNombreTextView.text = holder.mascotaNombreTextView.text.toString().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString()}
        holder.borrarMascotaImageView.setOnClickListener {
            // si hay solo una mascota en el anuncio, no se puede borrar
            if (mascotas.size == 1) {
                Toast.makeText(holder.itemView.context, "No puedes eliminar la última mascota del anuncio", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Confirmación")
                    .setMessage("¿Estás seguro de que quieres eliminar a ${mascota.nombre} del anuncio?")
                    .setPositiveButton("Sí") { _, _ ->
                        onMascotaRemoved(mascota)
                        notifyItemRemoved(position)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = mascotas.size
}