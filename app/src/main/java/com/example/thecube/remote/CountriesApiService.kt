package com.example.thecube.remote


import com.example.thecube.model.Country
import retrofit2.http.GET

interface CountriesApiService {
    @GET("all")
    suspend fun getCountries(): List<Country>
}
