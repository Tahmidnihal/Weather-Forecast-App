package com.example.weatherappproject.Login

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherappproject.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import isChecked

class SignUpActivity : AppCompatActivity() {

    private lateinit var userEmail: TextInputEditText
    private lateinit var username: TextInputEditText
    private lateinit var userFullName: TextInputEditText
    private lateinit var userPhone: TextInputEditText
    private lateinit var userPassword: TextInputEditText
    private lateinit var userConfirmPassword: TextInputEditText
    private lateinit var signUp: CardView
    private lateinit var reference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        reference = FirebaseDatabase.getInstance().getReference("WeatherUsers")
        findViews()

        signUp.setOnClickListener {
            signupFromUser()
        }
    }
    private fun findViews() {
        userEmail = findViewById(R.id.user_email)
        username = findViewById(R.id.user_name)
        userFullName = findViewById(R.id.user_full_name)
        userPhone = findViewById(R.id.user_phone)
        userPassword = findViewById(R.id.user_password)
        userConfirmPassword = findViewById(R.id.confirm_password)
        signUp = findViewById(R.id.signUp)
    }

    private fun signupFromUser() {
        val checked = isChecked()
        val hashString = HashString()

        val email = userEmail.text.toString()
        val userName = username.text.toString()
        val fullName = userFullName.text.toString()
        val phone = userPhone.text.toString()
        val password = userPassword.text.toString()
        val confirmPassword = userConfirmPassword.text.toString()

        if (!checked.validEmail(email) || email.isEmpty()) {
            userEmail.setBackgroundResource(R.drawable.error_background)
            return
        }

        if (!checked.validPassword(password) || password.isEmpty()) {
            userPassword.setBackgroundResource(R.drawable.error_background)
            return
        }

        if (password != confirmPassword) {
            userConfirmPassword.setBackgroundResource(R.drawable.error_background)
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_box, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        val hashedPassword = hashString.sha256(password)

        val userMap = hashMapOf(
            "email" to email,
            "userName" to userName,
            "fullName" to fullName,
            "phone" to phone,
            "password" to hashedPassword,
            "history" to "",
            "download" to "",
            "Photos" to "https://firebasestorage.googleapis.com/v0/b/clanguage-d17c6.appspot.com/o/profileImage%2Fuser%20(5).png?alt=media&token=692ab04f-fe90-4ed7-973c-48e54ae2dca8"
        )

        reference.child(reference.push().key!!).setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialog.dismiss()
                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                onBackPressed()
                finish()
            }
        }
    }
}