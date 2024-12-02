package com.example.finalproject

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import org.w3c.dom.Text


class Favorites : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var eventlist: ArrayList<Event>
    lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorites)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        eventlist = ArrayList<Event>()
        recyclerView = findViewById<RecyclerView>(R.id.FavoriteEvents)
        loadData()
    }

    fun loadData() {
        val collection = db.collection("Favorites")
        collection.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("Oopsie", "Listen Failed", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val events = snapshots.toObjects(Event::class.java)
                eventlist.addAll(events)
                showData(eventlist)
            }
            else{
                recyclerView.visibility = INVISIBLE
            }

        }
    }

    fun showData(events: ArrayList<Event>){
        val adapter = EventAdapter(eventlist, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.visibility = VISIBLE
    }

    fun returnButton(view: View){
        finish()
    }

}