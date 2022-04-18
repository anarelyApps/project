package com.example.mapapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mapapp.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }


        //********************* generate id of user and register the key on firestone *************
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val identifierInstallation  = sharedPref.getString(getString(R.string.install_key), "0")

        Log.d("TAG", "Document: $identifierInstallation" )

        if(identifierInstallation=="0"){
            val c = Calendar.getInstance()

            val db = FirebaseFirestore.getInstance()
            val user = hashMapOf(
                "datetime" to c.time
            )
            // Add a new document with a generated ID
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    with (sharedPref.edit()) {
                        putString(getString(R.string.install_key), documentReference.id)
                        apply()
                    }

                    getActualLocation(documentReference.id)
                    Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)

                }


        }

        getActualLocation(identifierInstallation)

    }

private fun getActualLocation(id: String?) {

    //************************** get location Longitude/Latitude ****************************
    var fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    val task = fusedLocationProviderClient.lastLocation

    if (ActivityCompat
            .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat
            .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        return
    }
    Log.d("TAG","successful")

    task.addOnSuccessListener {
        if (it != null){

            //****************** generate/update document about the visit *******************
            val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val idInst  = pref.getString(getString(R.string.install_key), "0")
            val idvisit = pref.getString(getString(R.string.visit_key), "0")

            val cal = Calendar.getInstance()
            val db = FirebaseFirestore.getInstance()
            var locationVisitor = hashMapOf( "Latitude" to it.latitude,"Longitude" to it.longitude)
            val visitor = hashMapOf(
                "userid" to idInst,
                "location" to locationVisitor,
                "poid" to "",
                "placeid" to "",
                "datetime" to cal.time
            )

            if(idvisit=="0") {
                db.collection("visitors")
                    .add(visitor)
                    .addOnSuccessListener { documentReference ->

                        with(pref.edit()) {
                            putString(
                                getString(com.example.mapapp.R.string.visit_key),
                                documentReference.id
                            )
                            apply()
                        }
                        Log.d("TAG", "document in visitor: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)

                    }
            } else{
                db.collection("visitors").document(idvisit.toString()).set(visitor)
            }
            Log.d("TAG","latitude: ${it.latitude}, longitude: ${it.longitude}") // tvLongitude is a TextView
        }
        else
            Log.d("TAG","it is null")
    }
}

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}