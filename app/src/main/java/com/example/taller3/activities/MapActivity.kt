package com.example.taller3.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.taller3.R
import com.example.taller3.databinding.ActivityMapBinding
import com.example.taller3.model.LocationG
import com.example.taller3.model.getLugares
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay


class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private val permisosUbicacionRequestCode = 123 // Identificador único para la solicitud de permisos
    lateinit var map : MapView // Mapa
    lateinit var locationClient: FusedLocationProviderClient // Cliente de ubicación
    lateinit var locationRequest : LocationRequest // Solicitud de ubicación
    lateinit var locationCallback: LocationCallback // Callback de ubicación
    lateinit var lastLocation : Location // Última ubicación conocida
    lateinit var marker : Marker // Marcador
    lateinit var lugares : List<LocationG> // Base de datos
    lateinit var markers : List<Marker> // Lista de marcadores de los lugares
    val bundle = Bundle()
    var movimientoCamaraPrimeraVez = false
    lateinit var parseLiveQueryClient: ParseLiveQueryClient
    lateinit var parseQuery: ParseQuery<ParseUser>
    private val userStates = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallBack()
        val extras = intent.extras
        parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()

        // Verificar si ya se tienen permisos. si se tienen, se inicia la actualizacion de ubicacion.
        startLocationUpdates()

        // crear la base de datos
        lugares = getLugares()

        // configurar los botones
        setButtons()

        // configurar el mapa
        setMap()

        // establecer los marcadores
        setMarkers()

        // establecer los listeners de los marcadores
        setMarkerListeners()

        // mover la camara a Bogotá
        moveCamera(4.61, -74.07)

        binding.disponiblebtn.setOnClickListener {
            toggleUserState()
        }
        checkAndUpdateButtonState()

        //Obtenemos los estados iniciales de los usuarios
        fetchInitialUserStates()
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
        startLocationUpdates()
    }

    // funcion para configurar todos listeners de los botones
    fun setButtons(){
        //Escuchar en el boton de log out
        setUpLogOutButton()

        binding.listarbtn.setOnClickListener{
            val intent = Intent(this, ListUsersActivity::class.java)
            startActivity(intent)
        }




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
                    setMyLocationMarker()
                    var userParse = ParseUser.getCurrentUser()
                    userParse.put("latitud",lastLocation.latitude)
                    userParse.put("longitud",lastLocation.longitude)
                    userParse.saveInBackground { e ->
                        if (e == null) {
                            Log.d("PARSE","Localizacion actualizada a: "+lastLocation.latitude+","+lastLocation.longitude)
                        } else {
                            Log.d("PARSE","Error al actualizar Localizacion")
                        }
                    }

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

        if (!movimientoCamaraPrimeraVez){
            movimientoCamaraPrimeraVez=true
            moveCamera(lastLocation.latitude,lastLocation.longitude)
        }

    }

    // funcion que establece los listeners de los marcadores.
    fun setMarkerListeners(){
        for (marker in markers){
            marker.setOnMarkerClickListener { marker, mapView ->
                val lugar = lugares[markers.indexOf(marker)]
                val toast = Toast.makeText(this, lugar.name, Toast.LENGTH_SHORT) // in Activity
                toast.show()
                true
            }
        }
    }

    // funcion que establece los marcadores de los lugares
    fun setMarkers(){
        markers = listOf()
        for (lugar in lugares){
            val marker = Marker(map)
            marker.position = GeoPoint(lugar.latitud, lugar.longitude)
            marker.title = lugar.name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(marker)
            markers += marker
        }
        setMarkerListeners()
    }



    fun setUpLogOutButton(){
        binding.logOutbtn.setOnClickListener{
            ParseUser.logOut()

            // Borrar el token de las preferencias compartidas
            val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("sessionToken") // Elimina el token
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }



    private fun toggleUserState() {
        val currentUser = ParseUser.getCurrentUser()
        if (currentUser != null) {
            val estado = currentUser.getString("estado")
            if (estado == "F") {
                currentUser.put("estado", "T")
                currentUser.saveInBackground { e ->
                    if (e == null) {
                        updateButtonState("ENABLE")
                    } else {
                        // Manejar errores, por ejemplo, mostrar un mensaje al usuario
                    }
                }
            } else if (estado == "T") {
                currentUser.put("estado", "F")
                currentUser.saveInBackground { e ->
                    if (e == null) {
                        updateButtonState("DISABLE")
                    } else {
                        // Manejar errores, por ejemplo, mostrar un mensaje al usuario
                    }
                }
            }
        }
    }

    private fun checkAndUpdateButtonState() {
        val currentUser = ParseUser.getCurrentUser()
        currentUser?.fetchInBackground { obj: ParseObject?, e: ParseException? ->
            if (e == null) {
                val user = obj as ParseUser
                val estado = user.getString("estado")
                updateButtonState(if (estado == "F") "DISABLE" else "ENABLE")
            } else {
                // Manejar errores, por ejemplo, mostrar un mensaje al usuario
            }
        }
    }


    private fun updateButtonState(action: String) {
        when (action) {
            "ENABLE" -> {
                binding.disponiblebtn.text = "DISABLE"

            }
            "DISABLE" -> {
                binding.disponiblebtn.text = "ENABLE"
            }
                }
       }

    fun sendNotification(objectId: String, username: String) {
        println("Notificación")
        val channelId = "channel"
        val title = "actualización"
        val message = "El usuario $username ha cambiado su visibilidad"

        // Crear un NotificationManager
        val notificationManager = baseContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Verificar si el dispositivo tiene Android Oreo (API 26) o superior, ya que se requieren canales de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Canal de notificaciones", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Crear un intent para abrir la actividad deseada al hacer clic en la notificación
        val intent = Intent(baseContext, SeguimientoActivity::class.java)
        intent.putExtra("objectID", objectId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Esto evita la creación de múltiples instancias de la actividad
        val pendingIntent = PendingIntent.getActivity(baseContext, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Crear una notificación utilizando NotificationCompat.Builder
        val builder = NotificationCompat.Builder(baseContext, channelId)
            .setSmallIcon(R.drawable.logo_app) // Icono de la notificación (debe existir en tu proyecto)
            .setContentTitle(title) // Título de la notificación
            .setContentText(message) // Contenido de la notificación
            .setContentIntent(pendingIntent) // Asignar el intent a la notificación
            .setAutoCancel(true)

        // Mostrar la notificación
        notificationManager.notify(1, builder.build()) // El valor 1 es un identificador único para la notificación
    }

    private fun setupSubscription() {
        parseQuery = ParseUser.getQuery()

        // Subscribirse a los cambios
        val subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery)

        // Reaccionar a los cambios en la columna 'estado'
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE) { _, user ->
            user?.let {
                val userId = it.objectId
                val nuevoEstado = it.getString("estado") ?: ""
                val estadoAnterior = userStates[userId] ?: "F" // Asumimos "F" como valor predeterminado si es desconocido

                // Comprobar si el estado ha cambiado de "F" a "T"
                if (estadoAnterior == "F" && nuevoEstado == "T") {
                    // Aquí actualizas el estado en tu HashMap
                    userStates[userId] = nuevoEstado

                    // Aquí llamas a la función que maneja este cambio específico, pasando el usuario
                    handleUserStateChange(it)
                }

                if (estadoAnterior == "T" && nuevoEstado == "F"){
                    userStates[userId] = nuevoEstado
                }
            }
        }
    }
    private fun handleUserStateChange(user: ParseUser) {
        sendNotification(user.objectId.toString(), user.username)
        // Manejar el cambio de estado del usuario aquí
        // Por ejemplo, actualizar la interfaz de usuario o realizar otras acciones en respuesta al cambio
        Log.i("UserStateChange", "El usuario ${user.objectId} ha cambiado su estado a 'T'")
    }
    private fun fetchInitialUserStates() {
        val query = ParseUser.getQuery()
        query.findInBackground { users, e ->
            if (e == null) {
                // No hay errores, poblar el HashMap
                for (user in users) {
                    val userId = user.objectId
                    val estado = user.getString("estado") ?: "F" // Asumir "F" si es nulo
                    userStates[userId] = estado
                }

                println("Estado inicial usuarios: $userStates")

                // Después de poblar el HashMap, configurar la suscripción a actualizaciones en vivo
                setupSubscription()

            } else {
                // Manejar el error
                Log.e("fetchInitialUserStates", "Error: " + e.message)
            }
        }
    }

}