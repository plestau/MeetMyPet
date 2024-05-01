package com.example.trabajo_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FiltrosAnadidosAdapter(private var listaFiltros: MutableList<Pair<String, String>>) : RecyclerView.Adapter<FiltrosAnadidosAdapter.FiltrosAnadidosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiltrosAnadidosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filtro_en_buscador, parent, false)
        return FiltrosAnadidosViewHolder(view)
    }

    override fun onBindViewHolder(holder: FiltrosAnadidosViewHolder, position: Int) {
        val filtro = listaFiltros[position]
        holder.bind(filtro)
    }

    override fun getItemCount(): Int {
        return listaFiltros.size
    }

    inner class FiltrosAnadidosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFiltro: TextView = itemView.findViewById(R.id.tvFiltro)
        private val botonEliminar: Button = itemView.findViewById(R.id.botonEliminar)

        fun bind(filtro: Pair<String, String>) {
            tvFiltro.text = "${filtro.first}: ${filtro.second}"
            botonEliminar.setOnClickListener {
                listaFiltros.removeAt(adapterPosition)
                notifyDataSetChanged()
            }
        }
    }
}