package com.example.taller3.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3.databinding.ActivitySeguimientoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


class SeguimientoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySeguimientoBinding
    private val permisosUbicacionRequestCode = 123 // Identificador único para la solicitud de permisos
    lateinit var map : MapView // Mapa
    lateinit var locationClient: FusedLocationProviderClient // Cliente de ubicación
    lateinit var locationRequest : LocationRequest // Solicitud de ubicación
    lateinit var locationCallback: LocationCallback // Callback de ubicación
    lateinit var lastLocation : Location // Última ubicación conocida
    var lastLocationSeguir = Location("") // Última ubicación conocida de la persona a seguir
    lateinit var marker : Marker // Marcador
    lateinit var markerSeguir : Marker // Marcador de la persona a seguir
    private val RADIUS_OF_EARTH_KM = 6371.0


    lateinit var parseLiveQueryClient: ParseLiveQueryClient
    lateinit var parseQuery: ParseQuery<ParseUser>
    var idSeguir =""
    var latitudSeguir : Double = 0.0
    var longitudSeguir : Double = 0.0

    var movimientoCamaraPrimeraVez = false


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



        idSeguir = intent.getStringExtra("objectID").toString()


        initParseLiveQuery()
        setupSubscription()
    }

    private fun initParseLiveQuery() {
        parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
    }

    private fun setupSubscription() {

        parseQuery = ParseUser.getQuery().whereEqualTo("objectId", idSeguir)
        println("Se ha iniciado el seguimiento a: "+idSeguir)

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
        //verifica que los valores de latitud y longitud sean validos
        if ((latitud != 0.0 || longitud != 0.0)&&(lastLocation!=null)) {
            // guarda la latitud y longitud de la persona a seguir en lastLocationSeguir
            lastLocationSeguir.latitude = latitud
            lastLocationSeguir.longitude = longitud
            runOnUiThread {
                setSeguirLocationMarker()
                moveCamera(lastLocationSeguir.latitude,lastLocationSeguir.longitude)

                val distancia = distance(lastLocation.latitude, lastLocation.longitude, lastLocationSeguir.latitude, lastLocationSeguir.longitude)
                binding.DistanciaTxtView.text = "Distancia: $distancia km"
                Log.i("LocationChange", "Ubicación actualizada: Latitud $latitud, Longitud $longitud")
            }
        }
        if(latitud == null || longitud == null){
            Toast.makeText(this, "El usuario no ha compartido su ubicación", Toast.LENGTH_SHORT).show()
        }
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
                    setLocationMarker()
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
        runOnUiThread {
            val geoPoint = GeoPoint(latitude, longitude)
            map.controller.animateTo(geoPoint)
            map.controller.setZoom(18.0)
        }
    }

    //funcion que establece el marcador en la ubicacion actual
    fun setLocationMarker(){
        if(this::marker.isInitialized){
            map.overlays.remove(marker)
        }
        marker = Marker(map)
        marker.position = GeoPoint(lastLocation.latitude, lastLocation.longitude)
        marker.title = "Ubicación actual"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)
    }


    //funcion que establece el marcador en la ubicacion actual (SEGUIR)
    fun setSeguirLocationMarker(){
        if(this::markerSeguir.isInitialized){
            map.overlays.remove(markerSeguir)
        }
        markerSeguir = Marker(map)
        markerSeguir.position = GeoPoint(lastLocationSeguir.latitude, lastLocationSeguir.longitude)
        markerSeguir.title = "Ubicación a seguir"
        markerSeguir.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(markerSeguir)
    }

    fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                * sin(lngDistance / 2) * sin(lngDistance / 2)))
        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
        val result: Double = RADIUS_OF_EARTH_KM * c
        println("RESULT:"+(result * 100.0).roundToInt() / 100.0)
        return (result * 100.0).roundToInt() / 100.0
    }

}