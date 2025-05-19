package com.example.listit


import Adapter.GroceryAdapter
import Network.RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listit.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.GroceryItem
import model.RecipeRequest
import model.RecipeResponse
import retrofit2.Response


open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: GroceryAdapter
    private val groceryList = mutableListOf<GroceryItem>()
    private lateinit var firestore: FirebaseFirestore

    companion object{
        lateinit var auth: FirebaseAuth
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_grocery_list)
        val addButton = findViewById<Button>(R.id.add_button)
        val newItemInput = findViewById<EditText>(R.id.input_item)


        adapter = GroceryAdapter(groceryList) { position ->
            groceryList.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveListToFirestore()
        }


        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        addButton.setOnClickListener {
            val newItemText = newItemInput.text.toString().trim()
            if (newItemText.isNotEmpty()) {
                groceryList.add(GroceryItem(newItemText))
                adapter.notifyItemInserted(groceryList.size - 1)
                newItemInput.text.clear()
                saveListToFirestore()
            }
        }

        loadListFromFirestore()
        binding.profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }




        binding.generateButton.setOnClickListener {
            val checkedItemsInput = groceryList
                .filter { it.checked }
                .joinToString(separator = ", ") { it.name }

            // Create RecipeRequest with that dynamic input
            val request = RecipeRequest(type = "ingredients", input = checkedItemsInput)
            generateRecipeAi(request)
        }



    }
    private fun generateRecipeAi(request : RecipeRequest){
        RetrofitClient.instance.generateRecipe(request)
            .enqueue(object : retrofit2.Callback<RecipeResponse> {
                override fun onResponse(call: retrofit2.Call<RecipeResponse>, response: Response<RecipeResponse>) {
                    if (response.isSuccessful) {
                        val recipe = response.body()?.recipe
                        binding.titleTextView.text = recipe?.title ?: "No title"
                        binding.ingredientsTextView.text = recipe?.ingredients?.joinToString("\n") ?: "No ingredients"
                        binding.instructionsTextView.text = recipe?.instructions?.joinToString("\n") ?: "No instructions"
                        showToast("Congratz! Recipe generated successfully!")
                    }
                    else {
                        Log.e("API", "Gagal: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<RecipeResponse>, t: Throwable) {
                    showToast("Failed: ${t.message}")
                }
            })
    }
    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveListToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("Firestore", "User not logged in")
            return
        }

        firestore.collection("groceryLists")
            .document(userId)  // use dynamic user ID here
            .set(hashMapOf("items" to groceryList.map { mapOf("name" to it.name, "checked" to it.checked) }))
            .addOnSuccessListener {
                Log.d("Firestore", "Grocery list saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving grocery list", e)
            }
    }

    private fun loadListFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("Firestore", "User not logged in")
            return
        }

        firestore.collection("groceryLists")
            .document(userId)  // use dynamic user ID here
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val items = document.get("items") as? List<Map<String, Any>>
                    items?.let {
                        groceryList.clear()
                        groceryList.addAll(it.map {
                            GroceryItem(
                                it["name"] as String,
                                it["checked"] as? Boolean ?: false
                            )
                        })
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error loading grocery list", e)
            }
    }




}