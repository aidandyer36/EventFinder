package com.example.finalproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser == null){
            startRegisterActivity()
        }
        else {
            val searchButton = findViewById<ImageView>(R.id.SearchButton)
            val db = FirebaseFirestore.getInstance()
            val quickSearch = findViewById<ImageView>(R.id.QuickSearch)
            val favorites = findViewById<ImageView>(R.id.FavoritesButton)
            val options = findViewById<ImageView>(R.id.OptionsButton)
            val logOut = findViewById<ImageView>(R.id.LogOutButton)
            val sharedPreferences = getSharedPreferences("Shared_Ratings", MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            searchButton.setOnClickListener {
                val myIntent = Intent(this, Search::class.java)
                startActivity(myIntent)
            }
            quickSearch.setOnClickListener{
                val myIntent = Intent(this, Search::class.java)
                myIntent.putExtra("autoSearch", true)
                startActivity(myIntent)
            }
            options.setOnClickListener {
                val myIntent = Intent(this, Options::class.java)
//            myIntent.putExtra("favList",favoriteList)
                startActivity(myIntent)
            }
            logOut.setOnClickListener{
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startRegisterActivity()
                        }
                    }
            }
            favorites.setOnClickListener(){
                val intent = Intent(this, Favorites::class.java)
                startActivity(intent)
            }
        }
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        // Make sure to call finish() to remove this activity from the backstack, otherwise the user
        // would be able to go back to the MainActivity
        finish()
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