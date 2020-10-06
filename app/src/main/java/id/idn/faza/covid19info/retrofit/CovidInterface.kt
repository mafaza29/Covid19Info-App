package id.idn.faza.covid19info.retrofit

import id.idn.faza.covid19info.pojo.ResponseCountry
import id.idn.faza.covid19info.pojo.ResponseSummary
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Imam Fahrur Rofi on 30/07/2020.
 */
interface CovidInterface {

    // Path Url yang dituju adalah https://api.covid19api.com/summary,
    // karena base url-nya https://api.covid19api.com/
    // maka hanya perlu menulis summary saja
    @GET("summary")
    // response berupa data class dari pojo ResponseSummary
    suspend fun getSummary(): Response<ResponseSummary>

    //membuat path url untuk chart country
    //tujuan membuat url seperti ini
    //bagian indonesia bisa diganti dengan nama negara lai
    //maka kita perlu membuatnya  jadi variable dinamis
    //variable dinamis ditandai dengan linkungan kurung kurawal berisi nama variable{}
    @GET("dayone/country/{country_name}")

    //@path berfungsi untuk mengubah nilai variable menjadi nama negara yang diinputkan
    //response-nya berupa list dari ResponseCountry karena data JSON jenisnya list
    suspend fun getCountryData(@Path("country_name") country_name: String): Response<List<ResponseCountry>>
}
