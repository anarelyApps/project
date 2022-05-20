package com.example.mapapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
       // setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setTitle("Information about place")

        val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val idplace  = pref.getString(getString(R.string.place_key), "0")

        val db = FirebaseFirestore.getInstance()

        val txt1:TextView = this.findViewById(R.id.textView8)
        val txt2:TextView = this.findViewById(R.id.textView9)
        val txt3:TextView = this.findViewById(R.id.textView11)
        val txt4:TextView = this.findViewById(R.id.textView12)
        val txt5:TextView = this.findViewById(R.id.textView10)
        val txt6:TextView = this.findViewById(R.id.textView13)

        Log.d("TAG","info")
        db.collection("info")
            .whereEqualTo("placeId", idplace)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("TAG","info inside")
/*
                if(querySnapshot.documents.isNotEmpty()) {

                    querySnapshot.forEach { document ->
                        txt1.text = document.data["name"].toString()
                        txt2.text = document.data["address"].toString()
                        txt3.text = document.data["amenities"].toString()
                        txt4.text = document.data["facilities"].toString()
                        txt5.text = document.data["area"].toString()
                        txt6.text = document.data["openingHours"].toString()
                    }
                }*/
            }
            .addOnCompleteListener { snapshot ->

              //fun onComplete(@NonNull task: Task<DocumentSnapshot> ) {
                 //   Log.d("TAG","info complete inside")
                    if(snapshot.isSuccessful()){
                        if(snapshot.getResult().documents.isNotEmpty()) {
                            var doc: DocumentSnapshot = snapshot.getResult().documents.get(0)

                            txt1.text = doc.getString("name").toString()
                            txt2.text = doc.getString("address").toString()
                            txt3.text = doc.getString("amenities").toString()
                            txt4.text = doc.getString("facilities").toString()
                            txt5.text = doc.getString("area").toString()
                            txt6.text = doc.getString("openingHours").toString()
                        }
                    }

                  //  Log.w("TAG", "Error getting documents $txt1.text")
               // }
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents $exception")
            }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater: MenuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.infomenu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result:Boolean = true

        when (item.itemId) {
            R.id.action_cagain ->
                showMainActivity()
            else -> result = super.onOptionsItemSelected(item)
        }
        return result
    }

    private fun showMainActivity(){

        val intent = Intent(this, MainBasicActivity::class.java)

        startActivity(intent)

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