package com.example.finalproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.INVISIBLE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.phone.SmsCodeAutofillClient
import com.google.android.gms.auth.api.phone.SmsCodeAutofillClient.PermissionState.DENIED
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class Options : AppCompatActivity() {
    lateinit var locationPerm: CheckBox
    lateinit var clearFav: ImageView
    lateinit var saveChange: ImageView
    lateinit var prefCity: EditText
    lateinit var prefKey: EditText
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    var checked: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_options)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        locationPerm = findViewById<CheckBox>(R.id.locationService)
        clearFav = findViewById<ImageView>(R.id.ClearFavorites)
        saveChange = findViewById<ImageView>(R.id.SaveChanges)
        prefCity = findViewById<EditText>(R.id.cityText)
        prefKey = findViewById<EditText>(R.id.keywordText)
        loadData()
        val db = FirebaseFirestore.getInstance()

        val sharedPreferences = getSharedPreferences("Shared_Ratings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        db.collection("Settings")
            .get()
            .addOnSuccessListener {documents ->
                val settings = documents.toObjects(UserSettings::class.java)
                for(setting in settings){
                    prefKey.setText(setting.preferredKeyword)
                    prefCity.setText(setting.preferredCity)
                }
            }
        clearFav.setOnClickListener{
            db.collection("Favorites")
                .get()
                .addOnSuccessListener {documents ->
                    for(document in documents){
                        document.reference.delete()
                    }
                }
        }
        saveChange.setOnClickListener{
            val setting = UserSettings(prefKey.text.toString(), prefCity.text.toString())
            editor.putString("preferredKeyword", prefKey.text.toString())
            db.collection("Settings").document("UserSettings").set(setting)
            editor.apply()
            finish()
        }
        if(checked){
            locationPerm.toggle()
            prefCity.visibility = View.GONE
        }
        //used geeksforgeeks https://www.geeksforgeeks.org/how-to-get-current-location-in-android/ to figure out gps code
        locationPerm.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked){
                prefCity.text.clear()
                prefCity.visibility = View.GONE
                editor.putBoolean("isChecked", true)
                if(isLocationPermissionGranted()){
                    locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    var locationByGps: Location


                    val gpsLocationListener: LocationListener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            locationByGps= location
                        }

                        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                    if(hasGps){
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            5000,
                            0F,
                            gpsLocationListener
                        )
                    }
                    val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val latitude = currentLocation?.latitude
                    val longitude = currentLocation?.longitude
                    Log.d("TAG", "$latitude and $longitude")
                    //used Geocoder API
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val address= geocoder.getFromLocation(latitude!!, longitude!!, 1, Geocoder.GeocodeListener {addresses ->
                        val cityName = addresses[0].locality
                        prefCity.setText(cityName)
                    })

                }

            }
            else{
                editor.putBoolean("isChecked", false)
                prefCity.visibility = View.VISIBLE
            }
            editor.apply()
        }
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    fun loadData(){
        val sharedPreferences = getSharedPreferences("Shared_Ratings", MODE_PRIVATE)
        checked = sharedPreferences.getBoolean("isChecked", false)
        Log.d("CheckTAG", "$checked")
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
            false
        } else {
            true
        }
    }
}