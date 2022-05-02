package com.example.mapapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainBasicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_basic)
        setSupportActionBar(findViewById(R.id.toolbar2))
        supportActionBar?.setTitle("Choosing place")

        val btnNext = findViewById(R.id.button_first) as Button
        btnNext.setOnClickListener {

            val spinner1: Spinner = this.findViewById(R.id.spinner_area)
            val spinner2: Spinner = this.findViewById(R.id.spinner3)

            if (spinner1.selectedItemPosition==0 || spinner1.selectedItemPosition==0){
              Toast.makeText(this,"Select area and place, please",Toast.LENGTH_SHORT).show()
            }
            else if (spinner1.selectedItemPosition==0 ){
                Toast.makeText(this,"Select area, please",Toast.LENGTH_SHORT).show()
            }
            else if (spinner2.selectedItemPosition==0){
                Toast.makeText(this,"Select place, please",Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this, MapsActivity::class.java)
                val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val strItem: String = resources.getStringArray(R.array.areas_values_array)[spinner1.selectedItemPosition]



                with (sharedPref.edit()) {
                    putString(
                        getString(com.example.mapapp.R.string.place_key),
                        strItem.replace("array",spinner2.selectedItemPosition.toString())
                    )
                    apply()
                }
                startActivity(intent)
            }
        }


        val spinner: Spinner = this.findViewById(R.id.spinner_area)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            baseContext,
            R.array.areas_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
            spinner.setSelection(-1)
        }



        val spinnerx: Spinner = this.findViewById(R.id.spinner3)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinner.selectedItemPosition == 0)

                else {
                    var strItem = resources.getStringArray(R.array.areas_values_array)[spinner.selectedItemPosition]

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter.createFromResource(
                        baseContext,
                        if (strItem == "Dublin_2_array") R.array.Dublin_2_array else R.array.Dublin_8_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        spinnerx.adapter = adapter
                    }
                }

            }

        }



        //********************* generate id of user and register the key on firestore *************
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
                val idvisit = pref.getString("var_id_visit", "0")

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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater: MenuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.mainmenu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result:Boolean = true
        val intent = Intent(this, HistoryActivity::class.java)
        when (item.itemId) {
            R.id.action_search ->
               // Log.d("TAG", " Testing menu item") // do whatever
                startActivity(intent)
            else -> result = super.onOptionsItemSelected(item)
        }
        return result
    }
}