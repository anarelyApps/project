package com.example.mapapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater: MenuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.secondarymenu, menu)
        supportActionBar?.setTitle("Mapping place")

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result:Boolean = true
        //val intent = Intent(this, MainBasicActivity::class.java)
        when (item.itemId) {
            R.id.action_cagain ->
                finish()
               // startActivity(intent)
            R.id.info ->
                showInfoActivity()
            R.id.update ->
                updateData()

            else -> result = super.onOptionsItemSelected(item)
        }
        return result
    }

    private fun updateData(){
         Log.d("TAG", " Testing updateData function")
        getActualLocation()

        mMap.clear()
        addmarkers()


    }

    private fun showInfoActivity(){
        val intent = Intent(this, InfoActivity::class.java)
        //setResult(0)
        startActivity(intent)
        //this.finish()
    }

    private fun getActualLocation() {

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
                val idvisit = pref.getString("var_id_visit", "0")

                val cal = Calendar.getInstance()
                val db = FirebaseFirestore.getInstance()
                var locationVisitor = hashMapOf( "Latitude" to it.latitude,"Longitude" to it.longitude)
                val visitor = hashMapOf(
                    "userid" to idInst,
                    "location" to locationVisitor,
                   // "poid" to "",
                   // "placeid" to "",
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
                    db.collection("visitors").document(idvisit.toString()).update("location", locationVisitor,
                        "datetime", cal.time)
                }
                Log.d("TAG","latitude: ${it.latitude}, longitude: ${it.longitude}") // tvLongitude is a TextView
            }
            else
                Log.d("TAG","it is null")
        }
    }

    private fun addmarkers(){
        val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val place  = pref.getString(getString(R.string.place_key), "0")


        val db = FirebaseFirestore.getInstance()

        db.collection("Management")
            .whereEqualTo("placeId", place)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var tot:Int = 0
                val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val zoom  = sharedPref.getString(getString(R.string.zoom_key), "0")!!.toFloat()
                if(querySnapshot.documents.isNotEmpty()) {
                    querySnapshot.forEach { document ->
                        val geo: GeoPoint? = document.getGeoPoint("location")

                        // Add a marker
                        // val location = LatLng(53.33518760381965, -6.261063009622788)
                        val location: LatLng = LatLng(geo!!.latitude.toDouble(), geo!!.longitude.toDouble())
                        val total: String = document.data["total"].toString()
                        val title: String = document.getString("poid").toString()
                        val isMoved: Boolean? = document.getBoolean("isPosition")

                        if (isMoved == true && querySnapshot.documents.size == 1) {
                            mMap.addMarker(MarkerOptions().position(location).title("$title").snippet("Visitors: $total")).showInfoWindow()
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                        }
                        else {
                            mMap.addMarker(MarkerOptions().position(location).title("$title").snippet("Visitors: $total"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                        }
                        tot += (document.data["total"] as Long).toInt()

                        Log.d("TAG", "Read document with ID ${document.getGeoPoint("location")}")
                    }

                    mMap.setMinZoomPreference(zoom)
                    addUpdates(tot)
                }

            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents $exception")
            }
    }

    private fun addUpdates(total:Int){
        val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val idInst  = pref.getString(getString(R.string.install_key), "0")
        val idplace  = pref.getString(getString(R.string.place_key), "0")


        val c = Calendar.getInstance()

        val db = FirebaseFirestore.getInstance()
        val bitacora = hashMapOf(
            "datetime" to c.time,
            "placeId" to idplace,
            "total" to total,
            "userId" to idInst
        )
        // Add a new document with a generated ID
        db.collection("bitacora")
            .add(bitacora)
            .addOnSuccessListener { documentReference ->

                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)

            }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val zoom  = pref.getString(getString(R.string.zoom_key), "0")

        mMap = googleMap
        mMap.setMinZoomPreference(zoom!!.toFloat())
        mMap.getUiSettings().setMapToolbarEnabled(true);

        addmarkers()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        this.finish()
    }
}