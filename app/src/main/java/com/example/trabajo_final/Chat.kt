package com.example.trabajo_final

import FragmentInferior
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trabajo_final.Java.Mensaje
import com.example.trabajo_final.Java.MensajesAdapter
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class Chat : AppCompatActivity() {
    private companion object {
        private const val PHOTO_SEND = 1
        private const val PHOTO_PERFIL = 2
        val hora = java.text.SimpleDateFormat("HH:mm").format(java.util.Date())
        val ampm = java.text.SimpleDateFormat("a").format(java.util.Date())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val fragmentInferior = FragmentInferior()
        supportFragmentManager.beginTransaction().add(R.id.fragment_inferior, fragmentInferior)
            .commit()

        val fotoPerfil = findViewById<ImageView>(R.id.fotoPerfil)
        val nombre = findViewById<TextView>(R.id.nombre)
        val rvMensajes = findViewById<RecyclerView>(R.id.rvMensajes)
        val txtMensaje = findViewById<EditText>(R.id.txtMensaje)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val imagenButton = findViewById<ImageButton>(R.id.imageBtn)
        val adapterMensajes = MensajesAdapter(this)
        val linearLayoutManager = LinearLayoutManager(this)
        val firebaseReference = FirebaseDatabase.getInstance()
        val databaseReference = firebaseReference.getReference("app/chat")
        val firebaseStorage = FirebaseStorage.getInstance()

        rvMensajes.setLayoutManager(linearLayoutManager)
        rvMensajes.setAdapter(adapterMensajes)

        btnEnviar.setOnClickListener {
            databaseReference.push().setValue(
                Mensaje(
                    txtMensaje.getText().toString(),
                    nombre.getText().toString(),
                    "",
                    "1",
                    "$hora $ampm"
                )
            )
            txtMensaje.setText("")
        }

        fotoPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/jpeg")
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PHOTO_PERFIL)
        }

        imagenButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/jpeg")
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PHOTO_SEND)
        }

        adapterMensajes.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                setScrollBar()
            }
        })

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val m = snapshot.getValue(Mensaje::class.java)
                adapterMensajes.addMensaje(m!!)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setScrollBar() {
        val rvMensajes = findViewById<RecyclerView>(R.id.rvMensajes)
        rvMensajes.scrollToPosition(rvMensajes.getAdapter()!!.getItemCount() - 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_SEND && resultCode == RESULT_OK) {
            val nombre = findViewById<TextView>(R.id.nombre)
            val u = data!!.data
            val StorageReference = FirebaseStorage.getInstance().getReference("app/imagenesChat")
            val fotoReferencia = StorageReference.child(u!!.lastPathSegment!!)
            val databaseReference = FirebaseDatabase.getInstance().getReference("app/chat")
            fotoReferencia.putFile(u).addOnSuccessListener {
                fotoReferencia.downloadUrl.addOnSuccessListener {
                    databaseReference.push().setValue(
                        Mensaje(
                            "Te ha enviado una foto",
                            nombre.getText().toString(),
                            u.toString(),
                            it.toString(),
                            "2",
                            "$hora $ampm"
                        )
                    )
                }
            }
        } else if (requestCode == PHOTO_PERFIL && resultCode == RESULT_OK) {
            val nombre = findViewById<TextView>(R.id.nombre)
            val u = data!!.data
            val StorageReference = FirebaseStorage.getInstance().getReference("app/fotosPerfil")
            val fotoReferencia = StorageReference.child(u!!.lastPathSegment!!)
            val databaseReference = FirebaseDatabase.getInstance().getReference("app/chat")
            fotoReferencia.putFile(u).addOnSuccessListener {
                fotoReferencia.downloadUrl.addOnSuccessListener {
                    databaseReference.push().setValue(
                        Mensaje(
                            "${nombre.getText().toString()} ha actualizado su foto de perfil",
                            nombre.getText().toString(),
                            u.toString(),
                            it.toString(),
                            "2",
                            "$hora $ampm"
                        )
                    )
                }
            }
        }
    }
}