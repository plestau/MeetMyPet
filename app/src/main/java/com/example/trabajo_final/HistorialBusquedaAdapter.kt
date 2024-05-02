package com.example.trabajo_final

import android.content.Intent
import android.util.Log
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
            val filtros = busqueda.split(" | ")
            tvBusqueda.text = filtros.joinToString("\n")

            itemView.setOnClickListener {
                val intent = Intent(it.context, ResultadosBusqueda::class.java)
                intent.putExtra("busqueda", busqueda)
                // Convertir los filtros a un array de pares y pasarlo como un extra en el intent
                val filtrosArray = filtros.map { filtro ->
                    val parts = filtro.split(": ")
                    Pair(parts[0], parts[1])
                }.toTypedArray()
                intent.putExtra("filtros", filtrosArray)
                it.context.startActivity(intent)
                // muestra en un log.d los filtros usados
                Log.d("HistorialBusquedaAdapter", "Filtros usados: $busqueda")
            }
        }
    }
}