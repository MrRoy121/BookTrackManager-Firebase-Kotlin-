package com.example.booktrackmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var mobile: EditText
    private lateinit var password: EditText
    private lateinit var conpassword: EditText
    private lateinit var register: Button
    private lateinit var fauth: FirebaseAuth
    private lateinit var fstore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        email = findViewById(R.id.email)
        mobile = findViewById(R.id.mobile)
        conpassword = findViewById(R.id.confirmpassword)
        fauth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()
        register = findViewById(R.id.signupbtn)

        findViewById<Button>(R.id.login).setOnClickListener {
            startActivity(Intent(applicationContext, SignUp::class.java))
        }

        register.setOnClickListener {
            when {
                username.text.isEmpty() || password.text.isEmpty() || email.text.isEmpty() || mobile.text.isEmpty() || conpassword.text.isEmpty() -> {
                    Toast.makeText(this@SignUp, "All Fields Are Required!!", Toast.LENGTH_SHORT).show()
                }
                password.text.toString() != conpassword.text.toString() -> {
                    Toast.makeText(
                        this@SignUp,
                        "Password & Confirm Password Must be Same. ${password.text}\n${conpassword.text}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    fauth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                        .addOnSuccessListener(OnSuccessListener<AuthResult> {
                            val userid = fauth.currentUser?.uid
                            val refer = fstore.collection("users").document(username.text.toString())
                            val user = hashMapOf(
                                "Email" to email.text.toString(),
                                "username" to username.text.toString(),
                                "Mobile" to mobile.text.toString(),
                                "userID" to userid,
                                "Password" to password.text.toString()
                            )
                            refer.set(user).addOnSuccessListener {
                                Toast.makeText(applicationContext, "Register Successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            }
                        }).addOnFailureListener(OnFailureListener { e ->
                            Toast.makeText(applicationContext, "Register unsuccessful \n ${e.message}", Toast.LENGTH_SHORT).show()
                        })
                }
            }
        }
    }
}