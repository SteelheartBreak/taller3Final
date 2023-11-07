package com.example.taller3.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.databinding.ActivityListUsersBinding
import com.parse.FindCallback
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser


class ListUsersActivity : AppCompatActivity() {
    val globalUsernamesList = mutableListOf<String>()

    private lateinit var binding: ActivityListUsersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadUsernames()

    }

    private fun loadUsernames() {
        val query: ParseQuery<ParseUser> = ParseUser.getQuery()
        query.findInBackground { users, e ->
            if (e == null) {
                // No hay error, proceder con la lista de usernames
                val usernames = users?.mapNotNull { it.username } ?: emptyList()
                // Ahora tienes una lista de usernames, puedes hacer lo que necesites con ella
                // Por ejemplo, podrías imprimir la lista
                println(usernames)
                globalUsernamesList.addAll(usernames)
            } else {
                // Manejar el error
                Log.e("loadUsernames", "Error: " + e.message)
            }
        }
    }


}
