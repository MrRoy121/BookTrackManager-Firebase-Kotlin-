package com.example.booktrackmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var fauth: FirebaseAuth
    private lateinit var fstore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fstore = FirebaseFirestore.getInstance()
        fauth = FirebaseAuth.getInstance()
        val username = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val regbtn = findViewById<Button>(R.id.loginbtn)

        if (fauth.currentUser != null) {
            val currentUser = fauth.currentUser
            val intent = Intent(this@MainActivity, AddBook::class.java)
            fstore.collection("users").whereEqualTo("userID", currentUser!!.uid).get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    for (d in queryDocumentSnapshots) {
                        intent.putExtra("User", d.getString("Email"))
                        startActivity(intent)
                        finish()
                    }
                }
        }

        findViewById<Button>(R.id.regi).setOnClickListener {
            startActivity(Intent(applicationContext, SignUp::class.java))
        }

        regbtn.setOnClickListener {
            val email1 = username.text.toString()
            val password1 = password.text.toString()
            if (username.text.isEmpty() && password.text.isEmpty()) {
                Toast.makeText(this@MainActivity, "All Fields Are Required!!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                fauth.signInWithEmailAndPassword(email1, password1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@MainActivity,
                                "Logged In Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@MainActivity, AddBook::class.java)
                            intent.putExtra("User", email1)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, "Login Failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }
    }
}