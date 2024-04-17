package com.example.trabajo_final

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
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
        private val tvRazaMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvRazaMascotaAnuncio)
        private val tvValoracionMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvValoracionMascotaAnuncio)
        private val rvImagenMascotaAnuncio = itemView.findViewById<RecyclerView>(R.id.rvImagenesMascotaAnuncio)

        fun bind(anuncio: Anuncio) {
            tvTituloAnuncio.text = anuncio.titulo
            tvDescrpicionAnuncio.text = anuncio.descripcion
            tvFechaAnuncio.text = anuncio.fecha
            tvHoraAnuncio.text = anuncio.hora
            tvLugarAnuncio.text = anuncio.lugar
            tvNombreMascotaAnuncio.text = anuncio.nombreMascota?.joinToString()
            tvRazaMascotaAnuncio.text = anuncio.razaMascota?.joinToString()
            tvValoracionMascotaAnuncio.text = anuncio.valoracionMascota?.joinToString()
            // carga la imagen de la mascota o las imagenes de las mascotas en caso de que haya mas de una
            rvImagenMascotaAnuncio.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            rvImagenMascotaAnuncio.adapter = ImagenMascotaAdapter(anuncio.imagenMascota!!)
        }
    }
}

class ImagenMascotaAdapter(private var listaImagenes: List<String>) : RecyclerView.Adapter<ImagenMascotaAdapter.ImagenMascotaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagenMascotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_imagen_mascota, parent, false)
        return ImagenMascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagenMascotaViewHolder, position: Int) {
        val imagen = listaImagenes[position]
        holder.bind(imagen)
    }

    override fun getItemCount(): Int {
        return listaImagenes.size
    }

    inner class ImagenMascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImagenMascota: ImageView = itemView.findViewById(R.id.ivFotoMascotaAnuncio)
        fun bind(imagen: String?) {
            if (imagen != null) {
                Glide.with(itemView)
                    .load(imagen)
                    .centerCrop()
                    .transform(CircleCrop())
                    .into(ivImagenMascota)
            } else {
                Log.d(TAG, "No se encontró la imagen de la mascota")
            }
        }
    }
}