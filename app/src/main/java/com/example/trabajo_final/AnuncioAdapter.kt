package com.example.trabajo_final

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AnuncioAdapter(private var listaAnuncios: List<Anuncio>, val fragmentManager: FragmentManager, private val activityName: String) : RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder>() {
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
        private val tvTipoAnuncio: TextView = itemView.findViewById(R.id.tvTipoAnuncio)
        private val tvPrecioAnuncio: TextView = itemView.findViewById(R.id.tvPrecioAnuncio)
        private val tvNombreMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvNombreMascotaAnuncio)
        private val tvRazaMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvRazaMascotaAnuncio)
        private val tvEdadMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvEdadMascotaAnuncio)
        private val tvValoracionMascotaAnuncio = itemView.findViewById<TextView>(R.id.tvValoracionMascotaAnuncio)
        private val rvImagenMascotaAnuncio = itemView.findViewById<RecyclerView>(R.id.rvImagenesMascotaAnuncio)
        private val ivEditarAnuncio: ImageView = itemView.findViewById(R.id.ivEditarAnuncio)
        private val btnIniciarChat: ImageView = itemView.findViewById(R.id.btnIniciarChat)
        private val btnApuntarse: ImageView = itemView.findViewById(R.id.btnApuntarse)
        private val ivAprobar: ImageView = itemView.findViewById(R.id.ivAprobar)
        private val ivDenegar: ImageView = itemView.findViewById(R.id.ivDenegar)
        private val ivTerminar: ImageView = itemView.findViewById(R.id.ivTerminar)
        private var tvNombrePaseador: TextView = itemView.findViewById(R.id.tvNombrePaseadorAnuncio)
        private var ivValorar: ImageView = itemView.findViewById(R.id.ivValorar)
        private val ivPerfilPaseador: ImageView = itemView.findViewById(R.id.ivPerfilPaseador)
        private val llPaseadorAnuncio: LinearLayout = itemView.findViewById(R.id.llPaseadorAnuncio)
        private var nombrePaseador: String = ""
        val sharedPref = itemView.context.getSharedPreferences("userRole", Context.MODE_PRIVATE)
        val userRol = sharedPref.getString("role", "user")

        fun bind(anuncio: Anuncio) {
            tvTituloAnuncio.text = anuncio.titulo
            tvTituloAnuncio.text = anuncio.titulo?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString()}
            tvDescrpicionAnuncio.text = anuncio.descripcion
            tvFechaAnuncio.text = anuncio.fecha
            tvHoraAnuncio.text = anuncio.hora
            tvLugarAnuncio.text = anuncio.lugar
            tvLugarAnuncio.text = anuncio.lugar?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString()}
            tvTipoAnuncio.text = anuncio.tipoAnuncio
            tvPrecioAnuncio.text = anuncio.precio?.let { String.format("%.1f", it) } + " €"
            tvNombreMascotaAnuncio.text = anuncio.nombreMascota?.joinToString()
            tvNombreMascotaAnuncio.text = anuncio.nombreMascota?.joinToString()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString()}
            tvRazaMascotaAnuncio.text = anuncio.razaMascota?.joinToString()
            tvEdadMascotaAnuncio.text = anuncio.edadMascota?.joinToString() + " años"
            tvValoracionMascotaAnuncio.text = anuncio.valoracionMascota?.joinToString()
            val color: Int = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                ContextCompat.getColor(itemView.context, R.color.white)
            } else {
                ContextCompat.getColor(itemView.context, R.color.black)
            }
            // carga la imagen de la mascota o las imagenes de las mascotas en caso de que haya mas de una
            rvImagenMascotaAnuncio.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            rvImagenMascotaAnuncio.adapter = ImagenMascotaAdapter(anuncio.imagenMascota!!)

            ivEditarAnuncio.setColorFilter(color)
            btnApuntarse.setColorFilter(color)
            btnIniciarChat.setColorFilter(color)
            tvValoracionMascotaAnuncio.setTextColor(color)
            ivValorar.setColorFilter(color)
            ivPerfilPaseador.setColorFilter(color)
            ivTerminar.setColorFilter(color)
            ivAprobar.setColorFilter(color)
            ivDenegar.setColorFilter(color)

            when (activityName) {
                "MisAnuncios" -> {
                    // si el usuario es dueño del anuncio
                    if (anuncio.usuarioDueño == FirebaseAuth.getInstance().currentUser?.uid) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val nombrePaseador = obtenerNombrePaseador(anuncio.usuarioPaseador).await()
                            withContext(Dispatchers.Main) {
                                if (anuncio.estado == "creado") {
                                    ivEditarAnuncio.visibility = View.VISIBLE
                                    ivAprobar.visibility = View.GONE
                                    ivDenegar.visibility = View.GONE
                                    llPaseadorAnuncio.visibility = View.GONE
                                    ivTerminar.visibility = View.GONE
                                } else if (anuncio.estado == "reservado") {
                                    ivEditarAnuncio.visibility = View.GONE
                                    ivAprobar.visibility = View.VISIBLE
                                    ivDenegar.visibility = View.VISIBLE
                                    llPaseadorAnuncio.visibility = View.VISIBLE
                                    tvNombrePaseador.text = nombrePaseador
                                    btnApuntarse.visibility = View.GONE
                                } else if (anuncio.estado == "En curso") {
                                    ivEditarAnuncio.visibility = View.GONE
                                    ivAprobar.visibility = View.GONE
                                    ivDenegar.visibility = View.GONE
                                    llPaseadorAnuncio.visibility = View.VISIBLE
                                    tvNombrePaseador.text = nombrePaseador
                                    ivTerminar.visibility = View.VISIBLE
                                } else if (anuncio.estado == "terminado") {
                                    ivEditarAnuncio.visibility = View.GONE
                                    ivAprobar.visibility = View.GONE
                                    ivDenegar.visibility = View.GONE
                                    llPaseadorAnuncio.visibility = View.VISIBLE
                                    tvNombrePaseador.text = nombrePaseador
                                    ivValorar.visibility = View.GONE
                                } else if (anuncio.estado == "valorado") {
                                    ivEditarAnuncio.visibility = View.GONE
                                    ivAprobar.visibility = View.GONE
                                    ivDenegar.visibility = View.GONE
                                    llPaseadorAnuncio.visibility = View.VISIBLE
                                    tvNombrePaseador.text = nombrePaseador
                                    ivValorar.visibility = View.GONE
                                } else {
                                    ivEditarAnuncio.visibility = View.GONE
                                    ivAprobar.visibility = View.GONE
                                    ivDenegar.visibility = View.GONE
                                    llPaseadorAnuncio.visibility = View.GONE
                                    ivTerminar.visibility = View.GONE
                                }
                            }
                        }
                        btnIniciarChat.visibility = View.GONE
                        btnApuntarse.visibility = View.GONE
                    } else {
                        if (anuncio.estado == "creado") {
                            ivEditarAnuncio.visibility = View.GONE
                            btnIniciarChat.visibility = View.VISIBLE
                            btnApuntarse.visibility = View.VISIBLE
                        } else if (anuncio.estado == "reservado") {
                            ivEditarAnuncio.visibility = View.GONE
                            btnIniciarChat.visibility = View.VISIBLE
                            btnApuntarse.visibility = View.GONE
                        } else if (anuncio.estado == "terminado") {
                            ivEditarAnuncio.visibility = View.GONE
                            btnIniciarChat.visibility = View.VISIBLE
                            btnApuntarse.visibility = View.GONE
                            ivValorar.visibility = View.VISIBLE
                        } else {
                            ivEditarAnuncio.visibility = View.GONE
                            btnIniciarChat.visibility = View.VISIBLE
                            btnApuntarse.visibility = View.GONE
                            ivValorar.visibility = View.GONE
                        }
                        if (userRol == "admin") {
                            ivEditarAnuncio.visibility = View.VISIBLE
                        }
                    }
                }
                "ResultadosBusqueda" -> {
                    if (anuncio.estado == "creado") {
                        ivEditarAnuncio.visibility = View.GONE
                        btnIniciarChat.visibility = View.VISIBLE
                        btnApuntarse.visibility = View.VISIBLE
                    } else if (anuncio.estado == "En curso") {
                        ivEditarAnuncio.visibility = View.GONE
                        btnIniciarChat.visibility = View.VISIBLE
                        btnApuntarse.visibility = View.GONE
                    } else if (anuncio.estado == "terminado") {
                        ivEditarAnuncio.visibility = View.GONE
                        btnIniciarChat.visibility = View.VISIBLE
                        btnApuntarse.visibility = View.GONE
                        ivValorar.visibility = View.VISIBLE
                    } else if( anuncio.estado == "valorado"){
                        ivEditarAnuncio.visibility = View.GONE
                        btnIniciarChat.visibility = View.VISIBLE
                        btnApuntarse.visibility = View.GONE
                        ivValorar.visibility = View.GONE
                    } else {
                        ivEditarAnuncio.visibility = View.GONE
                        btnIniciarChat.visibility = View.VISIBLE
                        btnApuntarse.visibility = View.GONE
                    }
                }
            }
            ivEditarAnuncio.setOnClickListener {
                val fragmentEditarAnuncio = FragmentEditarAnuncio()
                val args = Bundle()
                args.putParcelable("anuncio", anuncio)
                fragmentEditarAnuncio.arguments = args

                val fragmentManager = fragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                // oculta el linear layout de la actividad MisAnuncios
                val activity = itemView.context as AppCompatActivity
                activity.findViewById<View>(R.id.llMisAnuncios).visibility = View.GONE

                // edita el valor de user_notification en FirebaseDatabase
                val androidId = Settings.Secure.getString(itemView.context.contentResolver, Settings.Secure.ANDROID_ID)
                val userNotificationRef = database.getReference("app/usuarios/${FirebaseAuth.getInstance().currentUser?.uid}/user_notification")
                userNotificationRef.setValue(androidId)

                // Agrega el fragmento de edición de anuncio encima del fragmento existente
                fragmentTransaction.add(R.id.fragment_container, fragmentEditarAnuncio)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
            btnApuntarse.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && currentUser.uid != anuncio.usuarioDueño) {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Apuntarse al anuncio")
                        .setMessage("¿Estás seguro de que quieres apuntarte a este anuncio?")
                        .setPositiveButton("Sí") { _, _ ->
                            anuncio.estado = "reservado"
                            anuncio.estado_noti = Estado.RESERVADO
                            anuncio.usuarioPaseador = currentUser.uid
                            database.getReference("app/anuncios/${anuncio.id}").setValue(anuncio)
                            btnApuntarse.visibility = View.GONE
                            Toast.makeText(itemView.context, "Te has apuntado al anuncio", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("No") { _, _ -> }
                        .show()
                } else{
                    Toast.makeText(itemView.context, "No puedes apuntarte a tu propio anuncio", Toast.LENGTH_SHORT).show()
                }
            }
            ivAprobar.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Aprobar paseador")
                    .setMessage("¿Estás seguro de que quieres aprobar a $nombrePaseador como paseador de tu mascota?")
                    .setPositiveButton("Sí") { _, _ ->
                        anuncio.estado = "En curso"
                        anuncio.estado_noti = Estado.EnCurso
                        database.getReference("app/anuncios/${anuncio.id}").setValue(anuncio)
                        ivAprobar.visibility = View.GONE
                        ivDenegar.visibility = View.GONE
                        Toast.makeText(itemView.context, "Has aprobado a $nombrePaseador como paseador de tu mascota", Toast.LENGTH_SHORT).show()
                        ivTerminar.visibility = View.VISIBLE
                    }
                        .setNegativeButton("No") { _, _ -> }.show()
            }
            ivDenegar.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Denegar paseador")
                    .setMessage("¿Estás seguro de que quieres denegar a $nombrePaseador como paseador de tu mascota?")
                    .setPositiveButton("Sí") { _, _ ->
                        anuncio.estado = "creado"
                        anuncio.estado_noti = Estado.CREADO
                        anuncio.user_notificacion = ""
                        database.getReference("app/anuncios/${anuncio.id}").setValue(anuncio)
                        database.getReference("app/anuncios/${anuncio.id}/user_notification").setValue("")
                        database.getReference("app/anuncios/${anuncio.id}/estado_noti").setValue(Estado.CREADO)
                        ivAprobar.visibility = View.GONE
                        ivDenegar.visibility = View.GONE
                        llPaseadorAnuncio.visibility = View.GONE
                        Toast.makeText(itemView.context, "Has denegado a $nombrePaseador como paseador de tu mascota", Toast.LENGTH_SHORT).show()
                    }
                        .setNegativeButton("No") { _, _ ->
                            Toast.makeText(itemView.context, "No has denegado a $nombrePaseador como paseador de tu mascota", Toast.LENGTH_SHORT).show()
                        }
                        .show()
            }
            ivTerminar.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Terminar anuncio")
                    .setMessage("¿Estás seguro de que quieres terminar este anuncio?")
                    .setPositiveButton("Sí") { _, _ ->
                        anuncio.estado = "terminado"
                        database.getReference("app/anuncios/${anuncio.id}").setValue(anuncio)
                        ivTerminar.visibility = View.GONE
                        Toast.makeText(itemView.context, "Has terminado el anuncio", Toast.LENGTH_SHORT).show()

                        // Crea un nuevo AlertDialog para la valoración del paseador
                        val valoracionDialogView = LayoutInflater.from(itemView.context).inflate(R.layout.fragment_valorar_paseador, null)
                        val valoracionDialog = AlertDialog.Builder(itemView.context)
                            .setView(valoracionDialogView)
                            .setTitle("Valorar al paseador")
                            .setPositiveButton("Enviar") { _, _ ->
                                val ratingBarPaseador = valoracionDialogView.findViewById<RatingBar>(R.id.ratingBarValorarPaseador)
                                val valoracionPaseador = ratingBarPaseador.rating
                                // añade la valoracion a la lista de valoraciones del usuario paseador
                                agregarValoracionUsuario(anuncio.usuarioPaseador!!, valoracionPaseador)
                            }
                            .setNegativeButton("No, gracias") { _, _ ->
                                Toast.makeText(itemView.context, "No has enviado ninguna valoración", Toast.LENGTH_SHORT).show()
                            }
                            .create()

                        valoracionDialog.show()
                    }.show()
            }
            ivValorar.setOnClickListener {
                ivValorar.visibility = View.GONE
                // Crea un nuevo AlertDialog para la valoración del dueño y la mascota
                val valoracionDialogView = LayoutInflater.from(itemView.context).inflate(R.layout.fragment_valorar_dueno, null)
                val valoracionDialog = AlertDialog.Builder(itemView.context)
                    .setView(valoracionDialogView)
                    .setTitle("Valorar al dueño y a la mascota")
                    .setPositiveButton("Enviar") { _, _ ->
                        anuncio.estado = "valorado"
                        database.getReference("app/anuncios/${anuncio.id}").setValue(anuncio)
                        // Valora al dueño y a la mascota y añade los valores de los ratingBars a "valoraciones"
                        val ratingBarDueno = valoracionDialogView.findViewById<RatingBar>(R.id.ratingBarValorarDueno)
                        val ratingBarMascota = valoracionDialogView.findViewById<RatingBar>(R.id.ratingBarValorarMascota)
                        val valoracionDueno = ratingBarDueno.rating
                        val valoracionMascota = ratingBarMascota.rating
                        // añade la valoracion a la lista de valoraciones del usuario dueño
                        agregarValoracionUsuario(anuncio.usuarioDueño!!, valoracionDueno)
                        // añade la valoracion a la lista de valoraciones de la mascota
                        agregarValoracionMascota(anuncio.usuarioDueño!!, anuncio.idmascota!!, valoracionMascota)
                    }
                    .setNegativeButton("No, gracias") { _, _ ->
                        Toast.makeText(itemView.context, "No has enviado ninguna valoración", Toast.LENGTH_SHORT).show()
                    }
                    .create()

                valoracionDialog.show()
            }
            ivPerfilPaseador.setOnClickListener {
                val intent = Intent(it.context, PerfilUsuario::class.java)
                intent.putExtra("USER_ID", anuncio.usuarioPaseador)
                it.context.startActivity(intent)
            }
            btnIniciarChat.setOnClickListener {
                val idUsuarioActual = FirebaseAuth.getInstance().currentUser!!.uid
                val idUsuarioDueñoAnuncio = anuncio.usuarioDueño
                val tituloAnuncio = anuncio.titulo

                // Crear el ID del chat a partir de los ID de los usuarios
                val idChatPrivado = "$idUsuarioActual@$idUsuarioDueñoAnuncio"

                val intent = Intent(it.context, ChatPrivado::class.java)
                intent.putExtra("idUsuarioDueñoAnuncio", idUsuarioDueñoAnuncio)
                intent.putExtra("tituloAnuncio", tituloAnuncio)
                intent.putExtra("idChatPrivado", idChatPrivado)
                it.context.startActivity(intent)
            }
        }
        fun obtenerNombrePaseador(idUsuarioPaseador: String?): Task<String> {
            // busca en firabse el nombre del usuario paseador a partir de su id
            val userRef = database.getReference("app/usuarios/$idUsuarioPaseador/nombre")
            return userRef.get().addOnSuccessListener {
                nombrePaseador = it.value.toString()
            }.continueWith {
                nombrePaseador
            }
        }
        fun agregarValoracionUsuario(idUsuario: String, nuevaValoracion: Float) {
            val userRef = FirebaseDatabase.getInstance().getReference("app/usuarios/$idUsuario")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usuario = dataSnapshot.getValue(Usuario::class.java)
                    val valoraciones = usuario?.valoraciones?.toMutableList() ?: mutableListOf()
                    valoraciones.add(nuevaValoracion)
                    userRef.child("valoraciones").setValue(valoraciones)
                    userRef.child("user_notificacion").setValue(usuario!!.id)
                    userRef.child("estado_noti").setValue(Estado.VALORADO)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

        fun agregarValoracionMascota(idUsuario: String, idMascota: List<String>, nuevaValoracion: Float) {
            val mascotaRef = FirebaseDatabase.getInstance().getReference("app/usuarios/$idUsuario/mascotas")
            mascotaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (mascotaSnapshot in dataSnapshot.children) {
                        val mascota = mascotaSnapshot.getValue(Mascota::class.java)
                        if (mascota?.id in idMascota) {
                            val valoraciones = mascota?.valoraciones?.toMutableList() ?: mutableListOf()
                            valoraciones.add(nuevaValoracion)
                            mascotaRef.child(mascota?.id!!).child("valoraciones").setValue(valoraciones)
                            mascotaRef.child(mascota?.id!!).child("user_notificacion").setValue(idUsuario)
                            mascotaRef.child(mascota?.id!!).child("estado_noti").setValue(Estado.VALORADO)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(itemView.context, "Error al añadir la valoración a la mascota", Toast.LENGTH_SHORT).show()
                }
            })

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
                    .placeholder(Utilidades.animacion_carga(itemView.context))
                    .transform(CircleCrop())
                    .into(ivImagenMascota)
                    .apply { Utilidades.opcionesGlide(itemView.context) }
            }
        }
    }
}