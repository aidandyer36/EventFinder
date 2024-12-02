package com.example.finalproject

data class UserData(
    val _embedded: Embed
)
data class Embed(
    val events: List<Event>
)
data class Event(
    val name: String? = null,
    val type: String? = null,
    val id: String? = null,
    val test: Boolean = false,
    val url: String? = null,
    val locale: String? = null,
    val images: List<images>? = null,
    val dates: Date? = null,
    val priceRanges: List<Prices>? = null,
    val _embedded: Emb? = null
)
data class images(
    val ratio: String? = null,
    val url: String? = null,
    val width: Int? = 0,
    val height: Int? = 0,
    val fallback: Boolean = false
)

data class Date(
    val start: Start? = null

)
data class Start(
    val localDate: String? = null,
    val localTime: String? = null,
)
data class Prices(
    val type: String? = null,
    val currency: String? = null,
    val min: Float? = 0.0F,
    val max: Float? = 0.0F
)

data class Emb(
    val venues: List<Venues>? = null
)

data class Venues(
    val name: String? = null,
    val city: City? = null,
    val state: State? = null,
    val address: Address? = null,
)

data class City(
    val name: String? = null
)
data class State(
    val name: String? = null
)
data class Address(
    val line1: String? = null,
    val line2: String? = null
)