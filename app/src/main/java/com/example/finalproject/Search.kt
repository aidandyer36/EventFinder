package com.example.finalproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

class Search : AppCompatActivity() {
    private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
    lateinit var keyword: String
    lateinit var city: String
    lateinit var apikey: String
    lateinit var ticketMasterAPI: TicketService
    lateinit var eventArray: ArrayList<Event>
    lateinit var keyw: EditText
    lateinit var cit: EditText
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: EventAdapter
    lateinit var noResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        keyw = findViewById<EditText>(R.id.KeywordText)
        cit = findViewById<EditText>(R.id.CityText)
        ticketMasterAPI = retrofit.create(TicketService::class.java)
        apikey = "WAndLqa9NqGqc1ZeMvQKXxnaLu0qurmG"
        eventArray = ArrayList<Event>(20)
        recyclerView = findViewById<RecyclerView>(R.id.EventList)
        adapter = EventAdapter(eventArray, this)
        noResults = findViewById<TextView>(R.id.noResults)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        noResults.visibility = INVISIBLE
        val autoSearch = intent.getBooleanExtra("autoSearch", false)


        if(autoSearch){
            val db = FirebaseFirestore.getInstance()
            db.collection("Settings")
                .get()
                .addOnSuccessListener(){documents ->
                    val settings = documents.toObjects(UserSettings::class.java)
                    for(setting in settings){
                        keyw.setText(setting.preferredKeyword)
                        cit.setText(setting.preferredCity)
                    }
                    search(findViewById(R.id.Search))
                }

        }

    }

    fun returnButton(view: View){
        finish()
    }

    fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun search(view: View){
            keyword = keyw.text.toString()
            city = cit.text.toString()
            if (keyword.isEmpty() || city.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Missing Query")
                    .setMessage("Please enter a keyword and a city")
                    .setPositiveButton("Ok", null)
                    .show()
            } else {
                keyw.hideKeyboard()
                cit.hideKeyboard()
                Log.d("TAG", keyword)
                Log.d("TAG", city)
                ticketMasterAPI.getEvents(keyword, city, "date,asc", apikey)
                    .enqueue(object : Callback<UserData?> {
                        override fun onResponse(
                            call: Call<UserData?>,
                            response: Response<UserData?>
                        ) {
                            Log.d("TAG", "onResponse: $response")
                            val body = response.body()
                            eventArray.clear()
                            if (body == null) {
                                recyclerView.visibility = INVISIBLE
                                noResults.visibility = VISIBLE
                                Log.w("TAG", "Valid response was not received")
                                return
                            } else {
                                if (body._embedded == null) {
                                    recyclerView.visibility = INVISIBLE
                                    noResults.visibility = VISIBLE
                                    Log.w("TAG", "Valid response was not received")
                                    return
                                } else {
                                    recyclerView.visibility = VISIBLE
                                    noResults.visibility = INVISIBLE
                                    eventArray.addAll(body._embedded.events)

                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }

                        override fun onFailure(call: Call<UserData?>, t: Throwable) {
                            Log.d("Response", "$t")
                        }
                    })
            }

    }
}