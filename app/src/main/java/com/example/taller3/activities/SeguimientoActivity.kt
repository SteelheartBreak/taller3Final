package com.example.taller3.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.databinding.ActivitySeguimientoBinding
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling


class SeguimientoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySeguimientoBinding

    lateinit var parseLiveQueryClient: ParseLiveQueryClient
    lateinit var parseQuery: ParseQuery<ParseUser>
    lateinit var idSeguir :String
    var latitudSeguir : Double = 0.0
    var longitudSeguir : Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySeguimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idSeguir = intent.getStringExtra("objectID")
        println("Se ha iniciado el seguimiento a: "+idSeguir)

        initParseLiveQuery()
        setupSubscription()
    }

    private fun initParseLiveQuery() {
        parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
    }

    private fun setupSubscription() {

        parseQuery = ParseUser.getQuery().whereEqualTo("objectId", idSeguir)

        // Subscribirse a los cambios
        val subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery)

        // Reaccionar a los cambios en las columnas específicas
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE) { _, user ->
            user?.let {
                if (it.has("latitud") && it.has("longitud")) {
                    val latitud = it.getDouble("latitud")
                    val longitud = it.getDouble("longitud")
                    locationChanged(latitud, longitud)
                }
            }
        }
    }

    // Método que maneja los cambios en la ubicación
    private fun locationChanged(latitud: Double, longitud: Double) {
        // Actualizar la interfaz de usuario o lógica de la aplicación según sea necesario
        Log.i("LocationChange", "Ubicación actualizada: Latitud $latitud, Longitud $longitud")
    }
}