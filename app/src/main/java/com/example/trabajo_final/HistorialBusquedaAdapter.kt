package com.example.trabajo_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialBusquedaAdapter(private var listaBusquedas: List<String>) : RecyclerView.Adapter<HistorialBusquedaAdapter.HistorialBusquedaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialBusquedaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        return HistorialBusquedaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialBusquedaViewHolder, position: Int) {
        val busqueda = listaBusquedas[position]
        holder.bind(busqueda)
    }

    override fun getItemCount(): Int {
        return listaBusquedas.size
    }

    inner class HistorialBusquedaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBusqueda: TextView = itemView.findViewById(R.id.tvHistorial)

        fun bind(busqueda: String) {
            val filtros = busqueda.split(", ")
            tvBusqueda.text = filtros.joinToString("\n")
        }
    }
}