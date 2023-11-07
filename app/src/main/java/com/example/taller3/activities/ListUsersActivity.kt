package com.example.taller3.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.databinding.ActivityListUsersBinding
import com.google.firebase.storage.FirebaseStorage
import com.parse.ParseQuery
import com.parse.ParseUser


class ListUsersActivity : AppCompatActivity() {
    val usernameList = mutableListOf<String>()

    private lateinit var binding: ActivityListUsersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)


        updateUsers()



    }



    fun updateUsers() {
        // Crear una lista vacía para almacenar los nombres de usuario


        // Obtener el usuario actual
        val currentUser = ParseUser.getCurrentUser()

        // Obtener la consulta de ParseUser
        val query = ParseUser.getQuery()

        // Excluir al usuario actual
        if (currentUser != null) {
            query.whereNotEqualTo("objectId", currentUser.objectId)
        }

        // Filtrar solo los usuarios cuyo estado es "T"
        query.whereEqualTo("estado", "T")

        // Ejecutar la consulta en segundo plano
        query.findInBackground { userObjects, e ->
            if (e == null) {
                // Si no hay error, procesar la lista de usuarios
                userObjects?.forEach { user ->
                    // Asumiendo que el campo para el nombre de usuario es 'username'
                    user.objectId?.let { username ->
                        // Añadir el nombre de usuario a la lista si no es nulo
                        usernameList.add(username)
                    }
                }


                // Aquí puedes usar usernameList con los nombres de usuario
                println("Lista de nombres de usuario con estado 'T': $usernameList")

                downloadUserImages(usernameList) { uris ->
                    // Aquí tienes tu lista de URIs
                    for (uri in uris) {
                        println("URI descargada: $uri")
                    }
                    // Puedes hacer algo con la lista de URIs aquí
                }
            } else {
                // Manejar el error
                println("Error al buscar usuarios: " + e.localizedMessage)
            }
        }
    }



    fun downloadUserImages(usernames: List<String>, completion: (List<Uri>) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        // Crear una lista vacía para almacenar las URIs
        val imageUris = mutableListOf<Uri>()

        // Variable de conteo para saber cuándo hemos terminado
        var downloadCount = 0

        // Itera sobre la lista de nombres de usuario
        for (username in usernames) {
            // Reemplaza cualquier caracter que no sea adecuado para un nombre de archivo.
            val sanitizedUsername = username

            // Obtiene la referencia de la imagen para el usuario actual
            val imageRef = storage.reference.child("images/$sanitizedUsername.png")

            // Obtiene la URL de descarga
            imageRef.downloadUrl
                .addOnSuccessListener { uri->
                    // Agrega la URI a la lista
                    imageUris.add(uri)

                    // Incrementa el conteo de descargas
                    downloadCount++

                    // Si hemos recibido todas las URIs, pasamos la lista a la función de completado
                    if (downloadCount == usernames.size) {
                        completion(imageUris)
                    }
                }
                .addOnFailureListener { exception->
                    // Incrementa el conteo de descargas incluso en caso de fallo
                    downloadCount++

                    // Maneja el caso de error aquí, tal vez quieras registrar los errores o añadir un valor nulo a la lista
                    println("Error al descargar la imagen de $sanitizedUsername: ${exception.message}")

                    // Aún en caso de fallo, debemos verificar si hemos terminado
                    if (downloadCount == usernames.size) {
                        completion(imageUris)
                    }
                }
        }
    }
}