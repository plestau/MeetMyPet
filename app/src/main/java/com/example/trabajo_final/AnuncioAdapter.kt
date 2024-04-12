package com.example.trabajo_final

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AnuncioAdapter(private var listaAnuncios: List<Anuncio>) : RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder>() {
    private val database = FirebaseDatabase.getInstance()
    private val mascotasRef = database.getReference("app/mascotas")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnuncioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anuncio, parent, false)
        return AnuncioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnuncioViewHolder, position: Int) {
        val anuncio = listaAnuncios[position]
        holder.bind(anuncio)
    }

    override fun getItemCount(): Int {
        return listaAnuncios.size
    }

    fun updateData(newAnuncios: List<Anuncio>) {
        listaAnuncios = newAnuncios
        notifyDataSetChanged()
    }

    inner class AnuncioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTituloAnuncio: TextView = itemView.findViewById(R.id.tvtituloAnuncio)
        private val tvDescrpicionAnuncio: TextView = itemView.findViewById(R.id.tvDescripcionAnuncio)
        private val tvFechaAnuncio: TextView = itemView.findViewById(R.id.tvFechaAnuncio)
        private val tvHoraAnuncio: TextView = itemView.findViewById(R.id.tvHoraAnuncio)
        private val tvLugarAnuncio: TextView = itemView.findViewById(R.id.tvLugarAnuncio)
        private val tvNombreMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvNombreMascotaAnuncio)
        private val tvRazaMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvRazaMascotaAnuncio)
        private val tvEdadMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvEdadMascotaAnuncio)
        private val tvSexoMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvSexoMascotaAnuncio)

        fun bind(anuncio: Anuncio) {
            tvTituloAnuncio.text = anuncio.titulo
            tvDescrpicionAnuncio.text = anuncio.descripcion
            tvFechaAnuncio.text = anuncio.fecha
            tvHoraAnuncio.text = anuncio.hora
            tvLugarAnuncio.text = anuncio.lugar

            // Itera sobre la lista de mascotas en el anuncio
            anuncio.mascotas?.forEach { mascota ->
                // Recupera los detalles de la mascota
                mascotasRef.child(mascota.id!!).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val mascotaDetalles = snapshot.getValue(Mascota::class.java)
                        if (mascotaDetalles != null) {
                            tvNombreMascotaAnuncio.text = mascotaDetalles.nombre
                            tvRazaMascotaAnuncio.text = mascotaDetalles.raza
                            tvEdadMascotaAnuncio.text = mascotaDetalles.edad.toString()
                            tvSexoMascotaAnuncio.text = mascotaDetalles.sexo
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error al recuperar los detalles de la mascota: $error")
                    }
                })
            }
        }
    }
}