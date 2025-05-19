package com.example.listit


import Network.RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listit.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import model.RecipeRequest
import model.RecipeResponse
import retrofit2.Response
import kotlin.reflect.typeOf


open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object{
        lateinit var auth: FirebaseAuth
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.main)

        binding.profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val request = RecipeRequest(type = "ingredients", input = "telur, susu, tepung")

        RetrofitClient.instance.generateRecipe(request)
            .enqueue(object : retrofit2.Callback<RecipeResponse> {
                override fun onResponse(call: retrofit2.Call<RecipeResponse>, response: Response<RecipeResponse>) {
                    if (response.isSuccessful) {
                        val recipe = response.body()?.recipe
                        binding.titleTextView.text = recipe?.title ?: "No title"
                        binding.ingredientsTextView.text = recipe?.ingredients?.joinToString("\n") ?: "No ingredients"
                        binding.instructionsTextView.text = recipe?.instructions?.joinToString("\n") ?: "No instructions"
                        Toast.makeText(this@MainActivity, "Congratz! Recipe generated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Log.e("API", "Gagal: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<RecipeResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })


    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



}