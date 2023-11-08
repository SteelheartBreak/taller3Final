package com.example.taller3.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3.R
import com.example.taller3.databinding.ActivitySeguimientoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import android.util.Log
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling


class SeguimientoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySeguimientoBinding
    private val permisosUbicacionRequestCode = 123 // Identificador único para la solicitud de permisos
    lateinit var map : MapView // Mapa
    lateinit var locationClient: FusedLocationProviderClient // Cliente de ubicación
    lateinit var locationRequest : LocationRequest // Solicitud de ubicación
    lateinit var locationCallback: LocationCallback // Callback de ubicación
    lateinit var lastLocation : Location // Última ubicación conocida
    lateinit var marker : Marker // Marcador

    lateinit var parseLiveQueryClient: ParseLiveQueryClient
    lateinit var parseQuery: ParseQuery<ParseUser>
    var idSeguir =""
    var latitudSeguir : Double = 0.0
    var longitudSeguir : Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySeguimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallBack()

        // Verificar si ya se tienen permisos. si se tienen, se inicia la actualizacion de ubicacion.
        startLocationUpdates()

        // configurar el mapa
        setMap()

        // mover la camara a la ubicacion actual
        setMyLocationMarker();

        idSeguir = intent.getStringExtra("objectID").toString()
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

    // metodo onPause
    override fun onPause() {
        super.onPause()
        locationClient.removeLocationUpdates(locationCallback)
        map.onPause()
    }

    // metodo onResume
    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    // funcion para establecer el mapa en la actividad
    fun setMap(){
        Configuration.getInstance().load(this,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
        map = binding.osmMap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
    }


    // Función para verificar si se tienen los permisos necesarios
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Función para solicitar permisos de ubicacion al usuario
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            permisosUbicacionRequestCode
        )
    }

    // Manejar la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permisosUbicacionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
                // imrpimir un toast de que se aceptaron los permisos
                Toast.makeText(this, "Permisos aceptados", Toast.LENGTH_SHORT).show()
            } else {
                // imrpimir un toast de que se rechazaron los permisos
                Toast.makeText(this, "Permisos rechazados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createLocationRequest() : LocationRequest{
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(3000)
            .build()
        return locationRequest
    }

    fun createLocationCallBack() : LocationCallback{
        val locationCallback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if(result!=null){
                    lastLocation = result.lastLocation!!
                }
            }
        }
        return locationCallback
    }

    fun startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        }else{
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    // funcion que mueve la camara, dadas la latitud y longitud.
    fun moveCamera(latitude: Double, longitude: Double){
        val geoPoint = GeoPoint(latitude, longitude)
        map.controller.animateTo(geoPoint)
        map.controller.setZoom(18.0)
    }

    //funcion que establece el marcador en la ubicacion actual (JAVERIANA)
    fun setMyLocationMarker(){
        if(this::marker.isInitialized){
            map.overlays.remove(marker)
        }
        marker = Marker(map)
        marker.position = GeoPoint(lastLocation.latitude, lastLocation.longitude)
        marker.title = "Mi ubicación"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)

    }

}