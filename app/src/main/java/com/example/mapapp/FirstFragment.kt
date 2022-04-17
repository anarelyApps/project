package com.example.mapapp

//import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mapapp.databinding.FragmentFirstBinding
import com.google.firebase.firestore.FirebaseFirestore


import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

//import zoftino.com.firestore.R;


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val FIREBASE_CLOUD_FUNCTION_URL = "https://us-central1-geofences-345809.cloudfunctions.net/discount"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val spinner: Spinner = view.findViewById(R.id.spinner_area)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.areas_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
            spinner.setSelection(-1)
        }



        val spinnerx: Spinner = view.findViewById(R.id.spinner3)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinner.selectedItemPosition == 0)

                else {
                    var strItem = resources.getStringArray(R.array.areas_values_array)[spinner.selectedItemPosition]

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter.createFromResource(
                        requireContext(),
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

        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )
        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)

            }

        sendMessageToFcm(11.2)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sendMessageToFcm(orderAmount: Double) {
        val httpClient = OkHttpClient()
        val httpBuider : HttpUrl.Builder = FIREBASE_CLOUD_FUNCTION_URL.toHttpUrlOrNull()!!.newBuilder()

        httpBuider.addQueryParameter("orderAmt", "" + orderAmount )
        val request: Request = okhttp3.Request.Builder().url(httpBuider.build()).build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    println(response.body!!.string())
                }
            }
        })

    }
}