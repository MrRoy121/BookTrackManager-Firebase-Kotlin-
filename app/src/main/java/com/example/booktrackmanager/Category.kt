package com.example.booktrackmanager
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Catagory : AppCompatActivity() {

    private lateinit var txt: TextView
    private lateinit var names: ArrayList<String>
    private lateinit var fs: FirebaseFirestore
    private lateinit var simpleGrid: GridView
    private lateinit var uid: String
    private lateinit var bid: String
    private lateinit var customAdapter: Adapter
    private lateinit var log: ArrayList<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catagory)

        val i = intent
        txt = findViewById(R.id.txt)
        txt.text = i.getStringExtra("s")
        fs = FirebaseFirestore.getInstance()

        log = ArrayList()
        simpleGrid = findViewById(R.id.simpleGridView)
        names = ArrayList()

        if (i.getStringExtra("s") == "book") {
            fs.collection("users").whereEqualTo("Email", i.getStringExtra("User")).get().addOnSuccessListener { q ->
                if (!q.isEmpty) {
                    for (d in q) {
                        uid = d.id
                    }
                    Log.e("Tahss", uid)
                    fs.collection("books").whereEqualTo("Username", uid).get().addOnSuccessListener { qs ->
                        if (!qs.isEmpty) {
                            for (d in qs) {
                                names.add(d.getString("Title").toString())
                                log.add(d.getBoolean("Stats") ?: false)
                                bid = d.id
                            }

                            customAdapter = Adapter(this@Catagory, names, log)
                            simpleGrid.adapter = customAdapter
                        }
                    }
                }
            }
        } else {
            fs.collection("users").whereEqualTo("Email", i.getStringExtra("User")).get().addOnSuccessListener { q ->
                if (!q.isEmpty) {
                    for (d in q) {
                        uid = d.id
                    }
                    Log.e("Tahss", uid)
                    fs.collection("books").whereEqualTo("Username", uid).whereEqualTo("Catagory", i.getStringExtra("s")).get().addOnSuccessListener { qs ->
                        if (!qs.isEmpty) {
                            for (d in qs) {
                                names.add(d.getString("Title").toString())
                                log.add(d.getBoolean("Stats") ?: false)
                                bid = d.id
                            }
                            Log.e("Tahss", names.toString())

                            customAdapter = Adapter(this@Catagory, names, log)
                            simpleGrid.adapter = customAdapter
                        }
                    }
                }
            }
        }

        simpleGrid.setOnItemClickListener { parent, view, position, id ->
            val dialog = Dialog(this@Catagory)
            dialog.setContentView(R.layout.dialog)
            val tit = dialog.findViewById<TextView>(R.id.title)
            val aut = dialog.findViewById<TextView>(R.id.author)
            val buy = dialog.findViewById<TextView>(R.id.date)
            val e = dialog.findViewById<EditText>(R.id.text2)

            fs.collection("books").whereEqualTo("Title", names[position]).get().addOnSuccessListener { q ->
                for (d in q) {
                    tit.text = d.getString("Title")
                    aut.text = d.getString("Author")
                    buy.text = d.getString("Date")
                }
            }

            fs.collection("page").document(uid).collection("book").document(names[position]).get().addOnSuccessListener { d ->
                if (d.exists()) {
                    e.setText(d.getString("page no"))
                }
            }

            val dialogButton = dialog.findViewById<Button>(R.id.OK)
            dialogButton.setOnClickListener {
                if (e.text.isNotEmpty()) {
                    val userx = HashMap<String, Any>()

                    fs.collection("books").whereEqualTo("Title", names[position]).whereEqualTo("Username", uid).get().addOnSuccessListener { qs ->
                        if (!qs.isEmpty) {
                            for (d in qs) {
                                bid = d.id
                                userx["Username"] = uid
                                userx["Title"] = d.getString("Title").toString()
                                userx["Author"] = d.getString("Author").toString()
                                userx["Catagory"] = d.getString("Catagory").toString()
                                userx["Stats"] = true
                                userx["Date"] = d.getString("Date").toString()
                            }

                            fs.collection("books").document(bid).set(userx)
                            val user1 = hashMapOf("bookid" to bid, "userid" to uid)
                            val user = hashMapOf("page no" to e.text.toString())
                            fs.collection("page").document(uid).collection("book").document(names[position]).set(user)
                            fs.collection("Ongoingbooks").add(user1)
                            dialog.dismiss()
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Add A Number To Add", Toast.LENGTH_SHORT).show()
                }
            }

            val dialogButto = dialog.findViewById<Button>(R.id.delete)
            dialogButto.setOnClickListener {
                dlt(position)
            }

            dialog.show()
        }
    }

    private fun dlt(position: Int) {
        val builder = AlertDialog.Builder(this@Catagory)
        builder.setMessage("Are you sure you want to delete it?")
            .setTitle("Alert !")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                fs.collection("books").whereEqualTo("Title", names[position]).get().addOnSuccessListener { queryDocumentSnapshots ->
                    for (D in queryDocumentSnapshots) {
                        fs.collection("books").document(D.id).delete()
                        dialog.dismiss()
                        Toast.makeText(applicationContext, "Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}