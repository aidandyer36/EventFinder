package com.example.finalproject
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat


class EventAdapter (private val eventsList: ArrayList<Event>, val mContext: Context) : RecyclerView.Adapter<EventAdapter.MyViewHolder>() {


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    inner class MyViewHolder (itemView: View): RecyclerView.ViewHolder (itemView) {
        // This class will represent a single row in our recyclerView list
        // This class also allows caching views and reuse them
        // Each MyViewHolder object keeps a reference to 3 view items in our row_item.xml file
        val name = itemView.findViewById<TextView>(R.id.EventName)
        val ven = itemView.findViewById<TextView>(R.id.Venue)
        val location = itemView.findViewById<TextView>(R.id.Location)
        val profileImage = itemView.findViewById<ImageView>(R.id.imageView)
        val date = itemView.findViewById<TextView>(R.id.Date)
        val price = itemView.findViewById<TextView>(R.id.Price)
        val ticketButton = itemView.findViewById<Button>(R.id.seeTickets)
        val favoriteB = itemView.findViewById<ImageView>(R.id.Favorites)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate a layout from our XML (row_item.XML) and return the holder
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.eventlist, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dec = DecimalFormat("#,###,##0.00")
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val currentItem = eventsList[position]
        val sharedPreferences = mContext.getSharedPreferences("Shared_Ratings",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        var isFavorite: Boolean
        holder.name.text = currentItem.name
        holder.ven.text = "${currentItem._embedded?.venues?.get(0)?.name}, ${currentItem._embedded?.venues?.get(0)?.city?.name}"
        holder.location.text = "${currentItem._embedded?.venues?.get(0)?.address?.line1}, " +
                "${currentItem._embedded?.venues?.get(0)?.city?.name}," +
                "${currentItem._embedded?.venues?.get(0)?.state?.name}"
        if(currentItem.dates?.start?.localDate != null && currentItem.dates?.start.localTime != null){
            holder.date.text = formatDates(currentItem.dates?.start.localDate, currentItem.dates?.start.localTime)
        }
        else{
            holder.date.visibility = INVISIBLE
        }

        if(currentItem.priceRanges != null){
            var currency = "${currentItem.priceRanges[0].currency}"
            if(currency == "USD"){
                currency = "$"
            }
            holder.price.text = "$currency${dec.format(currentItem.priceRanges[0].min?.toDouble())} - $currency${dec.format(currentItem.priceRanges[0].max?.toDouble())} "
        }
        else{
            holder.price.visibility = INVISIBLE
        }

        holder.ticketButton.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.url))


            mContext.startActivity(browserIntent)
        }
        val highestQualityImage = currentItem.images!!.maxByOrNull {
            it.width!!.toInt() * it.height!!.toInt()
        }

        // Get the context for glide
        val context = holder.itemView.context

        // Load the image from the url using Glide library
        Glide.with(context)
            .load(highestQualityImage?.url)
            .placeholder(R.drawable.ic_launcher_foreground) // In case the image is not loaded show this placeholder image
            .into(holder.profileImage)
        val db = FirebaseFirestore.getInstance()
        db.collection("Favorites")
            .whereEqualTo("name", currentItem.name)
            .whereEqualTo("dates", currentItem.dates)
            .get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty){
                    for(document in documents){
                        holder.favoriteB.setImageResource(R.drawable.ic_action_on)
                    }
                }

            }
        holder.favoriteB.setOnClickListener{
            Log.d("ButtonTest", "did we make it")
            db.collection("Favorites")
                .whereEqualTo("name", currentItem.name)
                .whereEqualTo("dates", currentItem.dates)
                .get()
                .addOnSuccessListener { documents ->
                        if(!documents.isEmpty) {
                            Log.d("ButtonTest", "we")
                            for (document in documents) {
                                Log.d("ButtonTest", "did")
                                Log.d("ButtonTest", "$document")
                                document.reference.delete()
                                holder.favoriteB.setImageResource(R.drawable.ic_action_name)
                            }
                        }
                    else{
                        Log.d("ButtonTest", "oops")
                        db.collection("Favorites").document("${currentItem.name} ${currentItem.dates?.start?.localDate}").set(currentItem)
                        holder.favoriteB.setImageResource(R.drawable.ic_action_on)
                    }
                }
        }

    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    fun formatDates(date: String, time: String): String{
        val dateList = date.split("-")
        val year = dateList[0]
        val month = dateList[1]
        val day = dateList[2]
        val timeList = time.split(":")
        var hour = timeList[0].toInt()
        var timing = "AM"
        if(hour > 12){
            hour = hour - 12
            timing = "PM"
        }
        val min = timeList[1]
        val formatted = "$month/$day/$year @ $hour:$min $timing"
        Log.d("TAG", "$hour")
        return formatted
    }
}