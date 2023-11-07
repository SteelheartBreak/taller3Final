package com.example.taller3.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.taller3.databinding.ActivityRegistroBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.parse.ParseACL
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseUser

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding

    var usuario = ""
    var nombre = ""
    var contrasena = ""
    var confirmarContrasena = ""
    var apellido =""
    var numIdentificacion =""
    // latitud y longitud de la ubicación actual
    var latitud = 0.0
    var longitud = 0.0
    lateinit var lastLocation : Location // Última ubicación conocida
    private val permisosUbicacionRequestCode = 123 // Identificador único para la solicitud de permisos



    lateinit var locationClient: FusedLocationProviderClient // Cliente de ubicación
    lateinit var locationRequest : LocationRequest // Solicitud de ubicación
    lateinit var locationCallback: LocationCallback // Callback de ubicación

    val TAG = "GREETING_APP"
    val USER_CN = "Usuario"
    private var isImageSelected = false

    private lateinit var uriUpload : Uri


    val getContentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            loadImage(it!!)
        }
    )

    lateinit var storageRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallBack()

        // Verificar si ya se tienen permisos. si se tienen, se inicia la actualizacion de ubicacion.
        startLocationUpdates()

        setupRegisterButton()

        binding.imageView12.setOnClickListener{
            getContentGallery.launch("image/*")
        }

        binding.backButtonR.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setupRegisterButton() {
        binding.botonRegistrarseR.setOnClickListener {
            if (validateForm()) {
                guardarUsuario()
            } else {
                val toast = Toast.makeText(this, "Información Inválida", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }


    private fun guardarUsuario() {
        var userRegistro = ParseUser()
        userRegistro.username = binding.inputCorreoR.text.toString()
        userRegistro.setPassword(binding.inputContraseAR.text.toString())

        nombre = binding.inputNombreR.text.toString()
        userRegistro.put("nombre", nombre)

        apellido = binding.inputApellidoR.text.toString()
        userRegistro.put("apellido", apellido)

        numIdentificacion = binding.inputIdentificacionR.text.toString()
        userRegistro.put("numIdentificacion", numIdentificacion)

        userRegistro.put("estado","F")

        userRegistro.put("latitud",latitud)
        userRegistro.put("longitud",longitud)

        val acl = ParseACL()
        acl.publicReadAccess = true
        userRegistro.acl=acl

        userRegistro.signUpInBackground { e: ParseException? ->
            if (e == null) {
                // Registro exitoso, guarda el token de sesión en SharedPreferences
                val sessionToken = userRegistro.sessionToken
                val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("sessionToken", sessionToken)
                editor.apply()
                Log.e("PARSE", "Registro exitoso: $sessionToken")
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
            } else {
                val errorMessage = e.message
                Log.e("PARSE", "Error durante el registro: $errorMessage", e)
                // También puedes imprimir el código de error para tener más información
                Log.e("PARSE", "Código de error: ${e.code}")
            }

        }

        uploadFirebaseImage(uriUpload)

    }

    private fun validateForm(): Boolean {

        val nombre = binding.inputNombreR.text.toString()
        val apellido = binding.inputApellidoR.text.toString()
        val correo = binding.inputCorreoR.text.toString()
        val numIdentificacion = binding.inputIdentificacionR.text.toString()
        val contrasena = binding.inputContraseAR.text.toString()
        val confirmarContrasena = binding.inputContraseAConfirmaRr.text.toString()


        val todasLasCasillasTienenTexto =
        nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() && numIdentificacion.isNotEmpty() && contrasena.isNotEmpty() && confirmarContrasena.isNotEmpty()

        val emailValido = isValidEmail(correo)

        val contrasenasCoinciden = contrasena == confirmarContrasena

        return todasLasCasillasTienenTexto && contrasenasCoinciden && emailValido && isImageSelected
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
        return email.matches(emailRegex)
    }

    fun loadImage(uri : Uri){
        val imageStream = getContentResolver().openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        binding.imageView12.setImageBitmap(bitmap)
        isImageSelected = true
        uriUpload=uri
    }

    fun uploadFirebaseImage(uriUpload: Uri) {
        // Obtén una referencia al lugar donde las fotos serán guardadas
        val currentUser = ParseUser.getCurrentUser()
        val objectId = currentUser?.objectId
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("images/${objectId}.png")

        // Inicia la carga del archivo
        storageRef.putFile(uriUpload)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                // La carga fue exitosa, aquí puedes obtener, por ejemplo, la URL de la imagen
                val downloadUrl = taskSnapshot.metadata?.reference?.downloadUrl
                downloadUrl?.addOnSuccessListener { uri ->
                    println("Imagen cargada con éxito. URL: $uri")
                }
            }
            .addOnFailureListener { exception: Exception ->
                // La carga falló, maneja el error
                println("Error al cargar la imagen: ${exception.message}")
            }
    }

    fun createLocationRequest() : LocationRequest{
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .build()
        return locationRequest
    }

    fun createLocationCallBack() : LocationCallback{
        val locationCallback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if(result!=null){
                    lastLocation = result.lastLocation!!
                    latitud = lastLocation.latitude
                    longitud = lastLocation.longitude
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

    // Función para solicitar permisos de ubicacion al usuario
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            permisosUbicacionRequestCode
        )
    }


}