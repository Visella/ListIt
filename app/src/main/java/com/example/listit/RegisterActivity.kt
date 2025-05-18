package com.example.listit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.listit.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.signupButton.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            handleSignUp(name, email, password)
        }

        binding.goToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun handleSignUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            showToast("All fields must be filled.")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email format.")
        } else if (password.length < 6) {
            showToast("Password must be at least 6 characters.")
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        showToast("Signup successful!")
                        clearFields()
                        val firebaseUser = auth.currentUser
                        if(firebaseUser != null){
                            val userID = firebaseUser.uid
                            documentReference  = firestore.collection("users").document(userID);
                            val userData = hashMapOf<String,Any>()
                            userData["name"] = name
                            userData["email"] = email
                            documentReference.set(userData).addOnSuccessListener(this) { task ->
                                Log.d("RegisterActivity", "User data added to Firestore")
                            }
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        task.exception?.let {
                            Log.e("RegisterActivity", "Registration failed", it)
                            showToast("Registration failed: ${it.message}")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("RegisterActivity", "Error during registration", exception)
                    showToast("Error: ${exception.message}")
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearFields() {
        binding.inputName.text.clear()
        binding.inputEmail.text.clear()
        binding.inputPassword.text.clear()
    }
}
