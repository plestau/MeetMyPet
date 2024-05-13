import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.example.trabajo_final.R
import androidx.cardview.widget.CardView
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
            }
        }

        return view
    }
}