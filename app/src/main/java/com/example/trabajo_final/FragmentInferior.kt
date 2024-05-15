package com.example.trabajo_final

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.trabajo_final.R
import com.example.trabajo_final.Buscador
import com.example.trabajo_final.MisAnuncios
import com.example.trabajo_final.PerfilUsuario
import com.example.trabajo_final.PublicarAnuncio
import com.example.trabajo_final.SelectorChats

class FragmentInferior : Fragment() {
    companion object {
        var actividadActual: String = ""
        var selectedCardId: Int = 0
    }

    private lateinit var cardBuscar: CardView
    private lateinit var cardPublicar: CardView
    private lateinit var cardMisAnuncios: CardView
    private lateinit var cardMensajes: CardView
    private lateinit var cardPerfil: CardView
    private var selectedCard: CardView? = null
    private lateinit var txt_tus_anuncios: TextView
    private lateinit var iconoBuscar: ImageView
    private lateinit var textoBuscar: TextView
    private lateinit var iconoPublicar: ImageView
    private lateinit var textoPublicar: TextView
    private lateinit var iconoMisAnuncios: ImageView
    private lateinit var textoMisAnuncios: TextView
    private lateinit var iconoMensajes: ImageView
    private lateinit var textoMensajes: TextView
    private lateinit var iconoPerfil: ImageView
    private lateinit var textoPerfil: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inferior, container, false)
        val sharedPref = activity?.getSharedPreferences("userRole", 0)
        val userRol = sharedPref?.getString("role", "user")

        cardBuscar = view.findViewById(R.id.card_buscar)
        cardPublicar = view.findViewById(R.id.card_publicar)
        cardMisAnuncios = view.findViewById(R.id.card_tus_anuncios)
        cardMensajes = view.findViewById(R.id.card_mensajes)
        cardPerfil = view.findViewById(R.id.card_perfil)
        txt_tus_anuncios = view.findViewById(R.id.txt_tus_anuncios)
        iconoBuscar = view.findViewById(R.id.img_buscar)
        textoBuscar = view.findViewById(R.id.txt_buscar)
        iconoPublicar = view.findViewById(R.id.img_publicar)
        textoPublicar = view.findViewById(R.id.txt_publicar)
        iconoMisAnuncios = view.findViewById(R.id.img_tus_anuncios)
        textoMisAnuncios = view.findViewById(R.id.txt_tus_anuncios)
        iconoMensajes = view.findViewById(R.id.img_mensajes)
        textoMensajes = view.findViewById(R.id.txt_mensajes)
        iconoPerfil = view.findViewById(R.id.img_perfil)
        textoPerfil = view.findViewById(R.id.txt_perfil)

        if (userRol == "admin") {
            txt_tus_anuncios.text = "Anuncios en app"
            txt_tus_anuncios.textSize = 8F
        }

        cardBuscar.setOnClickListener {
            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            selectedCard = it as CardView

            if (FragmentInferior.actividadActual != "Buscar") {
                val intent = Intent(context, Buscador::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "Buscar"
                selectedCardId = R.id.card_buscar
                actualizarColor()
            }
        }

        cardPublicar.setOnClickListener {
            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            selectedCard = it as CardView

            if (FragmentInferior.actividadActual != "Publicar") {
                val intent = Intent(context, PublicarAnuncio::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "Publicar"
                selectedCardId = R.id.card_publicar
                actualizarColor()
            }
        }

        cardMisAnuncios.setOnClickListener {
            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            selectedCard = it as CardView

            if (FragmentInferior.actividadActual != "MisAnuncios") {
                val intent = Intent(context, MisAnuncios::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "MisAnuncios"
                selectedCardId = R.id.card_tus_anuncios
                actualizarColor()
            }
        }

        cardMensajes.setOnClickListener {
            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            selectedCard = it as CardView

            if (FragmentInferior.actividadActual != "Mensajes") {
                val intent = Intent(context, SelectorChats::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "Mensajes"
                selectedCardId = R.id.card_mensajes
                actualizarColor()
            }
        }

        cardPerfil.setOnClickListener {
            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            selectedCard = it as CardView

            if (FragmentInferior.actividadActual != "PerfilUsuario") {
                val intent = Intent(context, PerfilUsuario::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "PerfilUsuario"
                selectedCardId = R.id.card_perfil
                actualizarColor()
            }
        }
        actualizarColor()

        return view
    }
    private fun actualizarColor() {
        val colorAzul = ContextCompat.getColor(requireContext(), R.color.texto)
        val colorNegro = ContextCompat.getColor(requireContext(), R.color.black)
        val colorBlanco = ContextCompat.getColor(requireContext(), R.color.white)

        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val colorActual = if (isNightMode) colorBlanco else colorNegro
        val colorSeleccionado = if (isNightMode) colorAzul else colorAzul

        textoBuscar.setTextColor(if (FragmentInferior.actividadActual == "Buscar") colorSeleccionado else colorActual)
        iconoBuscar.setColorFilter(if (FragmentInferior.actividadActual == "Buscar") colorSeleccionado else colorActual)

        textoPublicar.setTextColor(if (FragmentInferior.actividadActual == "Publicar") colorSeleccionado else colorActual)
        iconoPublicar.setColorFilter(if (FragmentInferior.actividadActual == "Publicar") colorSeleccionado else colorActual)

        textoMisAnuncios.setTextColor(if (FragmentInferior.actividadActual == "MisAnuncios") colorSeleccionado else colorActual)
        iconoMisAnuncios.setColorFilter(if (FragmentInferior.actividadActual == "MisAnuncios") colorSeleccionado else colorActual)

        textoMensajes.setTextColor(if (FragmentInferior.actividadActual == "Mensajes") colorSeleccionado else colorActual)
        iconoMensajes.setColorFilter(if (FragmentInferior.actividadActual == "Mensajes") colorSeleccionado else colorActual)

        textoPerfil.setTextColor(if (FragmentInferior.actividadActual == "PerfilUsuario") colorSeleccionado else colorActual)
        iconoPerfil.setColorFilter(if (FragmentInferior.actividadActual == "PerfilUsuario") colorSeleccionado else colorActual)
    }
    override fun onResume() {
        super.onResume()
        actualizarColor()
    }
}