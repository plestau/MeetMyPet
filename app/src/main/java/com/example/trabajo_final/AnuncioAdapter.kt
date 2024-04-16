package com.example.trabajo_final

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AnuncioAdapter(private var listaAnuncios: List<Anuncio>) : RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder>() {
    private val database = FirebaseDatabase.getInstance()

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
        private val tvValoracionMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvValoracionMascotaAnuncio)
        private val ivImagenMascotaAnuncio = itemView.findViewById<ImageView>(R.id.ivFotoMascotaAnuncio)

        fun bind(anuncio: Anuncio) {
            tvTituloAnuncio.text = anuncio.titulo
            tvDescrpicionAnuncio.text = anuncio.descripcion
            tvFechaAnuncio.text = anuncio.fecha
            tvHoraAnuncio.text = anuncio.hora
            tvLugarAnuncio.text = anuncio.lugar
            tvNombreMascotaAnuncio.text = anuncio.nombreMascota?.joinToString()
            tvValoracionMascotaAnuncio.text = anuncio.valoracionMascota?.joinToString()
            anuncio.imagenMascota?.let { imagenes ->
                if (imagenes.isNotEmpty()) {
                    Glide.with(itemView)
                        .load(imagenes[0])
                        .centerCrop()
                        .into(ivImagenMascotaAnuncio)
                }
            }
        }
    }
}