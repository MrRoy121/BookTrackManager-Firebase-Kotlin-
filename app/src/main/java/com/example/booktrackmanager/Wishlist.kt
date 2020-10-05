package com.example.booktrackmanager


import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Wishlist : AppCompatActivity() {

    private lateinit var fs: FirebaseFirestore
    private lateinit var mobileArray: ArrayList<String>
    private lateinit var uid: String
    private lateinit var wi: EditText
    private lateinit var add: Button
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        fs = FirebaseFirestore.getInstance()
        val listView: ListView = findViewById(R.id.mobile_list)

        mobileArray = ArrayList()
        val user = FirebaseAuth.getInstance().currentUser?.uid
        if (user != null) {
            fs.collection("users").whereEqualTo("userID", user).get().addOnSuccessListener { q ->
                if (!q.isEmpty) {
                    for (d in q) {
                        uid = d.id
                    }
                    fs.collection("wishlist").whereEqualTo("username", uid).get().addOnSuccessListener { q ->
                        if (!q.isEmpty) {
                            for (d in q) {
                                mobileArray.add(d.id)
                                Log.e("Tahss", d.id)
                            }
                            adapter = ArrayAdapter(this@Wishlist, R.layout.listview, mobileArray)
                            listView.adapter = adapter
                        }
                    }
                }
            }
        }

        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.refreshLayou)
        swipeRefreshLayout.setOnRefreshListener {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            swipeRefreshLayout.isRefreshing = false
        }

        wi = findViewById(R.id.wi)
        add = findViewById(R.id.add)
        add.setOnClickListener {
            if (wi.text.isEmpty()) {
                Toast.makeText(this@Wishlist, "Please Provide The Title", Toast.LENGTH_SHORT).show()
            } else {
                val user = hashMapOf("username" to uid)
                fs.collection("wishlist").document(wi.text.toString()).set(user).addOnSuccessListener {
                    Toast.makeText(this@Wishlist, "Added", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}