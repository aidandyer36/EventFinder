package com.example.finalproject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketService {
    @GET("events.json")
    fun getEvents(@Query("keyword") keyword: String, @Query("city") city: String,
                  @Query("sort") sorting: String,
                  @Query("apikey") key: String): Call<UserData>
}