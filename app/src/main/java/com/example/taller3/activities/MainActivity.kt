package com.example.taller3.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.databinding.ActivityMainBinding
import com.parse.ParseAnonymousUtils
import com.parse.ParseException
import com.parse.ParseUser


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val USER_CN = "Usuario"

    var usuario = ""
    var contraseña = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Autenticación automatica con parse-server
        loginParse()

        // Escucha el evento del botón de login
        // Autenticación con usuario y contraseña, con parse-server
        setupLoginButton()

        //Escucha el evento del boton de registro
        setupRegisterButton()


    }

    private fun loginParse(){
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("sessionToken", null)

        if (sessionToken != null) {
            // Iniciar sesión automáticamente con el token de sesión
            ParseUser.becomeInBackground(sessionToken) { user: ParseUser?, e: ParseException? ->
                if (user != null) {
                    val toast = Toast.makeText(
                        this,
                        "Token de usuario recuperado: ${user.username}",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()

                    user.put("estado","F")
                    user.saveInBackground { e ->
                        if (e == null) {
                            Log.i("ACT-ESTADO","El estado se setea en false")
                        } else {
                            // Manejar errores, por ejemplo, mostrar un mensaje al usuario
                        }
                    }

                    val intent = Intent (this, MapActivity::class.java)
                    startActivity(intent)
                } else {
                    val toast = Toast.makeText(
                        this,
                        "Error al iniciar sesión automáticamente",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    eliminarTokenDeSesion()
                }
            }
        }else{
            ParseAnonymousUtils.logIn { user, e ->
                if (e == null) {
                    // El inicio de sesión anónimo fue exitoso
                    val userId = user.objectId
                    // Realiza acciones con el usuario anónimo, si es necesario
                } else {
                    // El inicio de sesión anónimo falló
                    Log.e("Parse", "Error al iniciar sesión anónimamente: " + e.message)
                    Log.e("PARSE",e.stackTraceToString())
                }
                Log.d("PARSE","entra a anonimo")
            }

        }
    }

    private fun eliminarTokenDeSesion() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("sessionToken")
        editor.apply()

        ParseAnonymousUtils.logIn { user, e ->
            if (e == null) {
                // El inicio de sesión anónimo fue exitoso
                val userId = user.objectId
                // Realiza acciones con el usuario anónimo, si es necesario
            } else {
                // El inicio de sesión anónimo falló
                Log.e("Parse", "Error al iniciar sesión anónimamente: " + e.message)
                Log.e("PARSE",e.stackTraceToString())
            }
            Log.d("PARSE","entra a anonimo")
        }
    }

    private fun validateForm(): Boolean {
        val usuario = binding.inputUsuario.text.toString()
        val contrasena = binding.inputContrasena.text.toString()

        val todasLasCasillasTienenTexto = usuario.isNotEmpty() &&
                contrasena.isNotEmpty()

        return todasLasCasillasTienenTexto
    }

    private fun setupLoginButton() {
        binding.botonIniciarSesion.setOnClickListener {
            if (validateForm()) {
                val usuario = binding.inputUsuario.text.toString()
                val contrasena = binding.inputContrasena.text.toString()

                ParseUser.logInInBackground(usuario, contrasena) { user: ParseUser?, e: ParseException? ->
                    if (user != null) {
                        val sessionToken = user.sessionToken
                        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("sessionToken", sessionToken)
                        editor.apply()
                        Log.e("PARSE", "Login exitoso: $sessionToken")

                        val intent = Intent(this, MapActivity::class.java)
                        startActivity(intent)
                    } else {
                        val toast = Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }
            }
            else{
                val toast = Toast.makeText(this, "Campos incompletos", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    private fun setupRegisterButton(){
        binding.botonRegistrarse.setOnClickListener{
            val intent = Intent (this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }
}