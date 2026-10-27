package com.example.weatherappproject.Login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.weatherappproject.History.HistoryPage
import com.example.weatherappproject.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SignInActivity : AppCompatActivity() {

    private lateinit var signIn: CardView
    private lateinit var signup: TextView
    private lateinit var email: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var userRef: DatabaseReference
    private var loginFace = false
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        userRef = FirebaseDatabase.getInstance().getReference("WeatherUsers")

        findIds()
        setClickListeners()

        signIn.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty()) {
                email.setBackgroundResource(R.drawable.error_background)
            }
            if (passwordText.isEmpty()) {
                password.setBackgroundResource(R.drawable.error_background)
            }
            if (emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                loginUser(emailText, passwordText)
            }
        }
    }

    private fun findIds() {
        signIn = findViewById(R.id.sing_in)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        signup = findViewById(R.id.signUp)
    }

    private fun setClickListeners() {
        signup.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
    }

    private fun loginUser(userEmail: String, userPassword: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_box, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        val hashedInputPass = HashString().sha256(userPassword)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            var found = false
            override fun onDataChange(snapshot: DataSnapshot) {
                val editor = sharedPreferences.edit()

                for (userSnap in snapshot.children) {
                    val dbEmail = userSnap.child("email").getValue(String::class.java)
                    val dbPass = userSnap.child("password").getValue(String::class.java)

                    if (userEmail == dbEmail && hashedInputPass == dbPass) {
                        val key = userSnap.key
                        loginFace = true

                        editor.putString("profileKey", key)
                        editor.putBoolean("profilePermission", loginFace)
                        editor.apply()

                        found = true
                        break
                    }
                }

                dialog.dismiss()

                if (found) {
                    Toast.makeText(this@SignInActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignInActivity,HistoryPage::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    Toast.makeText(this@SignInActivity, "Invalid User", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toast.makeText(this@SignInActivity, "Server problem: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        }
}