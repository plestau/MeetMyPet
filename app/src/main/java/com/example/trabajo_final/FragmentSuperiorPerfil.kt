package com.example.trabajo_final

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.trabajo_final.R
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.trabajo_final.FragmentCambiarPassword
import com.example.trabajo_final.FragmentEditarUser
import com.example.trabajo_final.FragmentPreferenciasCuenta

class FragmentSuperiorPerfil : Fragment() {

    private lateinit var cardInfoPersonal: CardView
    private lateinit var cardCuenta: CardView
    private var selectedCard: CardView? = null
    private var selectedText: TextView? = null
    private var selectedLine: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superior_perfil, container, false)

        cardInfoPersonal = view.findViewById(R.id.card_info_personal)
        cardCuenta = view.findViewById(R.id.card_cuenta)

        val lineInfoPersonal: View = view.findViewById(R.id.line_info_personal)
        val lineCuenta: View = view.findViewById(R.id.line_cuenta)
        val txtCuenta: TextView = view.findViewById(R.id.txt_cuenta)

        val txtInfoPersonal: TextView = view.findViewById(R.id.txt_info_personal)
        val color = ContextCompat.getColor(requireContext(), R.color.texto)
        val bold = Typeface.DEFAULT_BOLD
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val colorActual = if (isNightMode) ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.texto)
        val colorSeleccionado = if (isNightMode) ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.texto)

        txtInfoPersonal.setTextColor(colorSeleccionado)
        lineInfoPersonal.setBackgroundColor(colorSeleccionado)
        txtInfoPersonal.setTypeface(bold)

        selectedCard = cardInfoPersonal
        selectedText = txtInfoPersonal
        selectedLine = lineInfoPersonal

        cardInfoPersonal.setOnClickListener {
            val fragment = parentFragmentManager.findFragmentById(R.id.fragment_container)
            val originalColor = if (isNightMode) ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.black)
            selectedText?.setTextColor(originalColor)
            selectedLine?.setBackgroundColor(Color.TRANSPARENT)
            selectedText?.setTypeface(Typeface.DEFAULT)

            txtInfoPersonal.setTextColor(colorSeleccionado)
            lineInfoPersonal.setBackgroundColor(colorSeleccionado)
            txtInfoPersonal.setTypeface(bold)

            selectedCard = it as CardView
            selectedText = txtInfoPersonal
            selectedLine = lineInfoPersonal

            if (fragment is FragmentPreferenciasCuenta) {
                parentFragmentManager.beginTransaction().remove(fragment).commit()
                activity?.findViewById<View>(R.id.scrollView)?.visibility = View.VISIBLE
            }

        }

        cardCuenta.setOnClickListener {
            val originalColor = if (isNightMode) ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.black)
            selectedText?.setTextColor(originalColor)
            selectedLine?.setBackgroundColor(Color.TRANSPARENT)
            selectedText?.setTypeface(Typeface.DEFAULT)

            txtCuenta.setTextColor(colorSeleccionado)
            lineCuenta.setBackgroundColor(colorSeleccionado)
            txtCuenta.setTypeface(bold)

            // Actualizar la tarjeta seleccionada
            selectedCard = it as CardView
            selectedText = txtCuenta
            selectedLine = lineCuenta

            if (selectedCard == cardCuenta) {
                val fragmentCuenta = FragmentPreferenciasCuenta()
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentCuenta).commit()
            }

            activity?.findViewById<View>(R.id.scrollView)?.visibility = View.GONE
        }

        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            val currentFragment = parentFragmentManager.findFragmentById(R.id.fragment_container)

            if (currentFragment is FragmentPreferenciasCuenta || currentFragment is FragmentEditarUser || currentFragment is FragmentCambiarPassword) {
                val originalColor = ContextCompat.getColor(requireContext(), R.color.black)
                selectedText?.setTextColor(originalColor)
                selectedLine?.setBackgroundColor(Color.TRANSPARENT)
                selectedText?.setTypeface(Typeface.DEFAULT)

                val color = ContextCompat.getColor(requireContext(), R.color.texto)
                val bold = Typeface.DEFAULT_BOLD
                val txtInfoPersonal: TextView = requireView().findViewById(R.id.txt_info_personal)
                val lineInfoPersonal: View = requireView().findViewById(R.id.line_info_personal)

                txtInfoPersonal.setTextColor(color)
                lineInfoPersonal.setBackgroundColor(color)
                txtInfoPersonal.setTypeface(bold)

                selectedCard = cardInfoPersonal
                selectedText = txtInfoPersonal
                selectedLine = lineInfoPersonal
            }
        }
    }
}