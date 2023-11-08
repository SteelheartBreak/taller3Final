package com.example.taller3.adapters

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.taller3.R
import com.example.taller3.model.Usuario
import com.bumptech.glide.Glide
import com.example.taller3.activities.RegistroActivity
import com.example.taller3.activities.SeguimientoActivity
import kotlinx.coroutines.CoroutineScope

class UsersAdapter(context: Context, userList: List<Usuario>) :
    ArrayAdapter<Usuario>(context, 0, userList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView

        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.user_row, parent, false)
        }

        val usuario = getItem(position)

        val nombreTextView = listItemView?.findViewById<TextView>(R.id.contactName)
        val fotoImageView = listItemView?.findViewById<ImageView>(R.id.fotoP)
        val followButton = listItemView?.findViewById<Button>(R.id.btnFollow)

        nombreTextView?.text = usuario?.usuario
        val imageUrl = usuario?.urlImagen

        // Carga la imagen usando la URI de la foto (puedes usar una biblioteca como Glide o Picasso)
        // Ejemplo ficticio, debes implementar la carga de la imagen desde la URI
        if (fotoImageView != null) {
            Glide.with(context).load(imageUrl).into(fotoImageView)
        }

        // Configura el OnClickListener para el botón
        followButton?.setOnClickListener {
            // Inicia DetailActivity y pasa el objectID como un extra
            val intent = Intent(context, SeguimientoActivity::class.java)
            intent.putExtra("objectID", usuario?.objectID)
            context.startActivity(intent)
        }

        return listItemView!!
    }
}