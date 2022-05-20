package com.example.mapapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.collections.ArrayList

class HistoryActivity : AppCompatActivity() {
    lateinit var listView: ListView
    var arrayList: ArrayList<MyData> = ArrayList()
    var adapter: MyAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(findViewById(R.id.toolbar3))
        supportActionBar?.setTitle("History")
        getListUpdates()
    }

    private fun getListUpdates(){

        val pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val identifierInstallation  = pref.getString(getString(R.string.install_key), "0")

        val db = FirebaseFirestore.getInstance()


        //arrayList.add(MyData("prueba",
        //    "prueba",
        //    "prueba"))

        db.collection("bitacora")
            .whereEqualTo("userId", identifierInstallation)
            .orderBy("datetime", Query.Direction.DESCENDING).limit(7)
            .get()
            .addOnSuccessListener { querySnapshot ->
                listView = findViewById(R.id.listview)
                if (querySnapshot.documents.isNotEmpty()) {

                    querySnapshot.forEach { document ->
                        Log.d("TAG",document.get("total").toString())
                        val placeID = document.getString("placeId").toString()
                        val pos: Int = placeID.split("_")[2].toInt()

                        val place: String = resources.getStringArray(
                            if (placeID.contains("Dublin_2")) R.array.Dublin_2_array else R.array.Dublin_8_array
                            )[pos]

                        arrayList.add(MyData("Date: "+document.getDate("datetime")!!.toString(),
                            "Place: $place",
                            "Number of Visitors: "+document.get("total").toString()))
                    }
                    adapter = MyAdapter(this, arrayList)
                    listView.adapter = adapter
                }
            }




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater: MenuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.historymenu, menu)
        supportActionBar?.setTitle("History of Updates")

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result:Boolean = true
       // val intent = Intent(this, MainBasicActivity::class.java)

        when (item.itemId) {
            R.id.action_cagain ->
                //startActivity(intent)
            finish()
             //Log.d("TAG", " Testing menu item") // do whatever

            else -> result = super.onOptionsItemSelected(item)
        }
        return result
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

class MyAdapter(private val context: Context, private val arrayList: java.util.ArrayList<MyData>) : BaseAdapter() {
    private lateinit var dateField: TextView
    private lateinit var placeField: TextView
    private lateinit var totalField: TextView
    override fun getCount(): Int {
        return arrayList.size
    }
    override fun getItem(position: Int): Any {
        return position
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.history_list, parent, false)
        dateField = convertView.findViewById(R.id.date)
        placeField = convertView.findViewById(R.id.place)
        totalField = convertView.findViewById(R.id.total)
        dateField.text = " " + arrayList[position].date
        placeField.text = arrayList[position].place
        totalField.text = arrayList[position].total
        return convertView
    }
}

class MyData( var date:String, var place:String, var total:String)