package dev.tricht.gamesense.com.steelseries

import dev.tricht.gamesense.com.steelseries.model.Event
import dev.tricht.gamesense.com.steelseries.model.EventRegistration
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiClient {
    @POST("/bind_game_event")
    fun addEvent(@Body body: EventRegistration): Call<Any>
    @POST("/game_event")
    fun sendEvent(@Body body: Event): Call<Any>
}