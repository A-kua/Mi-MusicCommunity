package fan.akua.exam.api

import fan.akua.exam.Constants
import fan.akua.exam.data.ApiResponse
import fan.akua.exam.data.Page
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MusicService {

    @GET("music/homePage")
    suspend fun getHomePage(
        @Header("Content-Type") contentType: String = "application/json",
        @Query("current") current: Int,
        @Query("size") size: Int = 5
    ): ApiResponse<Page>

    companion object {
        private var service: MusicService? = null

        fun getApi(): MusicService {
            if (null == service) {

                val client = OkHttpClient.Builder()
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                service = retrofit.create(MusicService::class.java)
            }

            return service!!
        }
    }
}