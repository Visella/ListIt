package Network

import model.RecipeRequest
import model.RecipeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST



interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/generate-recipe")
    fun generateRecipe(@Body request: RecipeRequest): Call<RecipeResponse>
}
