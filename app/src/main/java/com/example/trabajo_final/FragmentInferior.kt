import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.example.trabajo_final.R
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.trabajo_final.MainActivity
import com.example.trabajo_final.MisAnuncios
import com.example.trabajo_final.PerfilUsuario
import com.example.trabajo_final.PublicarAnuncio

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
    private var selectedImage: ImageView? = null
    private var selectedText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inferior, container, false)

        cardBuscar = view.findViewById(R.id.card_buscar)
        cardPublicar = view.findViewById(R.id.card_publicar)
        cardMisAnuncios = view.findViewById(R.id.card_tus_anuncios)
        cardMensajes = view.findViewById(R.id.card_mensajes)
        cardPerfil = view.findViewById(R.id.card_perfil)

        if (selectedCardId != 0) {
            val selectedCard = view.findViewById<CardView>(selectedCardId)
            val img: ImageView
            val txt: TextView
            val color = ContextCompat.getColor(requireContext(), R.color.texto)

            when (selectedCardId) {
                R.id.card_buscar -> {
                    img = selectedCard.findViewById(R.id.img_buscar)
                    txt = selectedCard.findViewById(R.id.txt_buscar)
                }
                R.id.card_publicar -> {
                    img = selectedCard.findViewById(R.id.img_publicar)
                    txt = selectedCard.findViewById(R.id.txt_publicar)
                }
                R.id.card_tus_anuncios -> {
                    img = selectedCard.findViewById(R.id.img_tus_anuncios)
                    txt = selectedCard.findViewById(R.id.txt_tus_anuncios)
                }
                R.id.card_mensajes -> {
                    img = selectedCard.findViewById(R.id.img_mensajes)
                    txt = selectedCard.findViewById(R.id.txt_mensajes)
                }
                R.id.card_perfil -> {
                    img = selectedCard.findViewById(R.id.img_perfil)
                    txt = selectedCard.findViewById(R.id.txt_perfil)
                }
                else -> {
                    return view
                }
            }

            img.setColorFilter(color)
            txt.setTextColor(color)
        }

        cardBuscar.setOnClickListener {
            selectedCard?.let {
                val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
                selectedImage?.setColorFilter(originalColor)
                selectedText?.setTextColor(originalColor)
            }

            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            val imgBuscar: ImageView = it.findViewById(R.id.img_buscar)
            val txtBuscar: TextView = it.findViewById(R.id.txt_buscar)
            val color = ContextCompat.getColor(requireContext(), R.color.texto)
            imgBuscar.setColorFilter(color)
            txtBuscar.setTextColor(color)

            selectedCard = it as CardView
            selectedImage = imgBuscar
            selectedText = txtBuscar

            if (FragmentInferior.actividadActual != "Buscar") {
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "Buscar"
                selectedCardId = R.id.card_buscar
            }
        }

        cardPublicar.setOnClickListener {
            selectedCard?.let {
                val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
                selectedImage?.setColorFilter(originalColor)
                selectedText?.setTextColor(originalColor)
            }

            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            val imgPublicar: ImageView? = it.findViewById(R.id.img_publicar)
            val txtPublicar: TextView? = it.findViewById(R.id.txt_publicar)
            val color = ContextCompat.getColor(requireContext(), R.color.texto)
            imgPublicar?.setColorFilter(color)
            txtPublicar?.setTextColor(color)

            selectedCard = it as CardView
            selectedImage = imgPublicar
            selectedText = txtPublicar

            if (FragmentInferior.actividadActual != "Publicar") {
                val intent = Intent(context, PublicarAnuncio::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "Publicar"
                selectedCardId = R.id.card_publicar
            }
        }

        cardMisAnuncios.setOnClickListener {
            selectedCard?.let {
                val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
                selectedImage?.setColorFilter(originalColor)
                selectedText?.setTextColor(originalColor)
            }

            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            val imgMisAnuncios: ImageView = it.findViewById(R.id.img_tus_anuncios)
            val txtMisAnuncios: TextView = it.findViewById(R.id.txt_tus_anuncios)
            val color = ContextCompat.getColor(requireContext(), R.color.texto)
            imgMisAnuncios.setColorFilter(color)
            txtMisAnuncios.setTextColor(color)

            selectedCard = it as CardView
            selectedImage = imgMisAnuncios
            selectedText = txtMisAnuncios

            if (FragmentInferior.actividadActual != "MisAnuncios") {
                val intent = Intent(context, MisAnuncios::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "MisAnuncios"
                selectedCardId = R.id.card_tus_anuncios
            }
        }

        cardMensajes.setOnClickListener {
            selectedCard?.let {
                val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
                selectedImage?.setColorFilter(originalColor)
                selectedText?.setTextColor(originalColor)
            }

            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            val imgMensajes: ImageView = it.findViewById(R.id.img_mensajes)
            val txtMensajes: TextView = it.findViewById(R.id.txt_mensajes)
            val color = ContextCompat.getColor(requireContext(), R.color.texto)
            imgMensajes.setColorFilter(color)
            txtMensajes.setTextColor(color)

            selectedCard = it as CardView
            selectedImage = imgMensajes
            selectedText = txtMensajes

            if (FragmentInferior.actividadActual != "Mensajes") {
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "Mensajes"
                selectedCardId = R.id.card_mensajes
            }
        }

        cardPerfil.setOnClickListener {
            selectedCard?.let {
                val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
                selectedImage?.setColorFilter(originalColor)
                selectedText?.setTextColor(originalColor)
            }

            val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulsar)
            it.startAnimation(pulseAnimation)

            val imgPerfil: ImageView = it.findViewById(R.id.img_perfil)
            val txtPerfil: TextView = it.findViewById(R.id.txt_perfil)
            val color = ContextCompat.getColor(requireContext(), R.color.texto)
            imgPerfil.setColorFilter(color)
            txtPerfil.setTextColor(color)

            selectedCard = it as CardView
            selectedImage = imgPerfil
            selectedText = txtPerfil

            if (FragmentInferior.actividadActual != "PerfilUsuario") {
                val intent = Intent(context, PerfilUsuario::class.java)
                startActivity(intent)
                FragmentInferior.actividadActual = "PerfilUsuario"
                selectedCardId = R.id.card_perfil
            }
        }

        if (actividadActual == "MisAnuncios") {
            val imgMisAnuncios: ImageView = cardMisAnuncios.findViewById(R.id.img_tus_anuncios)
            val txtMisAnuncios: TextView = cardMisAnuncios.findViewById(R.id.txt_tus_anuncios)
            val color = ContextCompat.getColor(requireContext(), R.color.texto)
            imgMisAnuncios.setColorFilter(color)
            txtMisAnuncios.setTextColor(color)
            selectedCard = cardMisAnuncios
            selectedImage = imgMisAnuncios
            selectedText = txtMisAnuncios
            selectedCardId = R.id.card_tus_anuncios

            // Restablece el color de la tarjeta "Publicar" a su color original
            val imgPublicar: ImageView = cardPublicar.findViewById(R.id.img_publicar)
            val txtPublicar: TextView = cardPublicar.findViewById(R.id.txt_publicar)
            val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
            imgPublicar.setColorFilter(originalColor)
            txtPublicar.setTextColor(originalColor)
        }

        return view
    }

}