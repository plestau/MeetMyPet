package com.example.trabajo_final

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

open class MascotaAdapter(var listaMascotas: List<Mascota>, val fragmentManager: FragmentManager) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = listaMascotas[position]
        holder.bind(mascota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mascota, parent, false)

        return MascotaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaMascotas.size
    }

    inner class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreMascota: TextView = itemView.findViewById(R.id.tvNombreMascotaAnuncio)
        val tvRazaMascota: TextView = itemView.findViewById(R.id.tvRazaMascotaAnuncio)
        val tvEdadMascota: TextView = itemView.findViewById(R.id.tvEdadMascota)
        val tvSexoMascota: TextView = itemView.findViewById(R.id.tvSexoMascota)
        val cbEsterilizadoMascota: CheckBox = itemView.findViewById(R.id.cbEsterilizadoMascota)
        val tvBiografiaMascota: TextView = itemView.findViewById(R.id.tvBiografiaMascota)
        val tvValoracionMascota: RatingBar = itemView.findViewById(R.id.rbValoracionMascota)
        val tvFotoMascota: ImageView = itemView.findViewById(R.id.ivFotoMascota)
        val ivEditarMascota: ImageView = itemView.findViewById(R.id.ivEditarMascota)
        val ivBorrarMascota: ImageView = itemView.findViewById(R.id.ivBorrarMascota)

        fun bind(mascota: Mascota) {
            tvNombreMascota.text = mascota.nombre
            tvRazaMascota.text = mascota.raza
            tvEdadMascota.text = "Tiene ${mascota.edad} años"
            tvSexoMascota.text = mascota.sexo
            cbEsterilizadoMascota.isChecked = mascota.esterilizado!!
            cbEsterilizadoMascota.isEnabled = false
            tvBiografiaMascota.text = mascota.biografia
            tvValoracionMascota.rating = mascota.valoracion!!
            val mascotaFotoUrl = mascota.foto
            if (mascotaFotoUrl != null && mascotaFotoUrl.isNotEmpty()) {
                Glide.with(this.itemView)
                    .load(mascotaFotoUrl)
                    .transform(CircleCrop())
                    .into(tvFotoMascota)
            }

            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition)
            }

            ivEditarMascota.setOnClickListener {
                val fragmentEditarMascota = FragmentEditarMascota()
                val args = Bundle()
                args.putParcelable("mascota", mascota)
                fragmentEditarMascota.arguments = args

                val fragmentManager = fragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                // Oculta todos los demás fragmentos
                fragmentManager.fragments.forEach { fragment ->
                    if (fragment.isVisible) {
                        fragmentTransaction.hide(fragment)
                    }
                }

                // Oculta todas las demás vistas
                (fragmentManager as? Activity)?.findViewById<ViewGroup>(android.R.id.content)?.forEach { view ->
                    if (view is ViewGroup) {
                        view.visibility = View.GONE
                    }
                }

                // Agrega el fragmento de edición de mascota
                fragmentTransaction.add(R.id.fragment_container, fragmentEditarMascota)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }

            ivBorrarMascota.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Confirmación")
                    .setMessage("¿Estás seguro de que quieres borrar esta mascota?")
                    .setPositiveButton("Sí") { _, _ ->
                        borrarMascota(mascota, itemView.context)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }
    private fun borrarMascota(mascota: Mascota, context: Context) {
        // si el atributo borrable de la mascota es falso, no se puede borrar
        if (!mascota.borrable!!) {
            Toast.makeText(
                context,
                "La mascota está en un anuncio y no se puede borrar",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            val mascotaRef = FirebaseDatabase.getInstance()
                .getReference("app/usuarios/${mascota.usuarioId}/mascotas/${mascota.id}")
            mascotaRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (mascota.foto != null && mascota.foto!!.isNotEmpty()) {
                        val storageRef =
                            FirebaseStorage.getInstance().getReferenceFromUrl(mascota.foto!!)
                        storageRef.delete().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Mascota borrada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al borrar la foto de la mascota",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Mascota borrada correctamente", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(context, "Error al borrar la mascota", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
