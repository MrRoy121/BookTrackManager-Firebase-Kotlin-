package com.example.booktrackmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Ongoing : AppCompatActivity() {

    private lateinit var simpleGrid: GridView
    private lateinit var names: ArrayList<String>
    private lateinit var log: ArrayList<Boolean>
    private lateinit var uid: String
    private lateinit var fs: FirebaseFirestore
    private lateinit var customAdapter: Adapter
    private lateinit var sss: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing)

        fs = FirebaseFirestore.getInstance()
        val i = intent
        simpleGrid = findViewById(R.id.simpleGridView)
        names = ArrayList()
        log = ArrayList()
        customAdapter = Adapter(this@Ongoing, names, log)

        fs.collection("users").whereEqualTo("Email", i.getStringExtra("User")).get()
            .addOnSuccessListener { q ->
                if (!q.isEmpty) {
                    for (d in q) {
                        uid = d.id
                    }
                    fs.collection("Ongoingbooks").whereEqualTo("userid", uid).get()
                        .addOnSuccessListener { qs ->
                            if (!qs.isEmpty) {
                                for (d in qs) {
                                    fs.collection("books").document(d.getString("bookid")!!).get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            if (documentSnapshot.exists()) {
                                                names.add(documentSnapshot.getString("Title")!!)
                                                log.add(documentSnapshot.getBoolean("Stats")!!)
                                                customAdapter = Adapter(this@Ongoing, names, log)
                                                simpleGrid.adapter = customAdapter
                                            }
                                        }
                                }
                            }
                        }
                }
            }

        simpleGrid.setOnItemClickListener { parent, view, position, id ->
            val dialog = Dialog(this@Ongoing)
            dialog.setContentView(R.layout.dialog2)
            val e = dialog.findViewById<EditText>(R.id.text2)

            fs.collection("page").document(uid).collection("book").document(names[position]).get()
                .addOnSuccessListener { d ->
                    if (d.exists()) {
                        e.setText(d.getString("page no"))
                    }
                }

            val userx = HashMap<String, Any>()
            val dialogButton = dialog.findViewById<Button>(R.id.OK)
            dialogButton.setOnClickListener {
                if (e.text.isNotEmpty()) {
                    fs.collection("books").whereEqualTo("Title", names[position]).whereEqualTo("Username", uid).get()
                        .addOnSuccessListener { q ->
                            for (d in q) {
                                sss = d.id
                                userx["Username"] = uid
                                userx["Title"] = d.getString("Title")!!
                                userx["Author"] = d.getString("Author")!!
                                userx["Catagory"] = d.getString("Catagory")!!
                                userx["Stats"] = true
                                userx["Date"] = d.getString("Date")!!
                                userx["Price"] = d.getString("Price")!!
                            }
                            fs.collection("books").document(sss).set(userx)
                        }

                    val user = HashMap<String, Any>()
                    user["page no"] = e.text.toString()
                    fs.collection("page").document(uid).collection("book").document(names[position]).set(user)
                    dialog.dismiss()
                } else {
                    Toast.makeText(applicationContext, "Add A Number To Add", Toast.LENGTH_SHORT).show()
                }
            }

            val dialogButto = dialog.findViewById<Button>(R.id.delete)
            dialogButto.text = "Completed"
            dialogButto.setOnClickListener {
                fs.collection("books").whereEqualTo("Title", names[position]).whereEqualTo("Username", uid).get()
                    .addOnSuccessListener { queryDocumentSnapshots ->
                        for (D in queryDocumentSnapshots) {
                            fs.collection("Ongoingbooks").whereEqualTo("bookid", D.id).get()
                                .addOnSuccessListener { qs ->
                                    for (ds in qs) {
                                        fs.collection("Ongoingbooks").document(ds.id).delete()
                                    }
                                    dialog.dismiss()
                                    Toast.makeText(applicationContext, "Book Completed!", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
            dialog.show()
        }
    }
}