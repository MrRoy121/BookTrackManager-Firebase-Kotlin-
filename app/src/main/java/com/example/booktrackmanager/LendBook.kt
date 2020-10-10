package com.example.booktrackmanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class LendBook : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var listView: ListView
    private lateinit var sport: ArrayList<String>
    private lateinit var sports: ArrayList<String>
    private lateinit var fs: FirebaseFirestore
    private lateinit var uid: String

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lend_book)

        val i = intent
        fs = FirebaseFirestore.getInstance()
        listView = findViewById(R.id.simpleGridView)
        button = findViewById(R.id.button)
        sports = ArrayList()
        sport = ArrayList()

        val itemDataList = ArrayList<Map<String, Any>>()
        fs.collection("users").whereEqualTo("Email", i.getStringExtra("User")).get().addOnSuccessListener { q ->
            if (!q.isEmpty) {
                for (d in q) {
                    uid = d.id
                }
                fs.collection("Lendedbooks").whereEqualTo("UserId", uid).get().addOnSuccessListener { queryDocumentSnapshots ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        for (d in queryDocumentSnapshots) {
                            sports.add(d.getString("Bookname").toString())
                            sport.add(d.getString("From").toString())
                        }
                        val titleLen = sports.size
                        for (i in 0 until titleLen) {
                            val listItemMap = HashMap<String, Any>()
                            listItemMap["Bookname"] = "Bookname : ${sports[i]}"
                            listItemMap["To"] = "To : ${sport[i]}"
                            itemDataList.add(listItemMap)
                        }
                        val adapter = SimpleAdapter(
                            this@LendBook, itemDataList, android.R.layout.simple_list_item_2,
                            arrayOf("Bookname", "To"), intArrayOf(android.R.id.text1, android.R.id.text2)
                        )

                        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
                        listView.adapter = adapter
                    }
                }
            }
        }

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayou)
        swipeRefreshLayout.setOnRefreshListener {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            swipeRefreshLayout.isRefreshing = false
        }

        button.setOnClickListener {
            val alert = AlertDialog.Builder(this@LendBook)
            val input = EditText(this@LendBook)
            val input2 = EditText(this@LendBook)
            alert.setTitle("Lend Book")
            val D = alert.create()
            D.window?.setBackgroundDrawable(ColorDrawable(R.color.yellow))
            alert.setMessage("Give Details For Lending Book")
            input.hint = "Book Name"
            input2.hint = "To"
            input.setPadding(20, 20, 20, 20)
            input2.setPadding(20, 20, 20, 20)
            val lay = LinearLayout(this)
            lay.orientation = LinearLayout.VERTICAL
            lay.addView(input)
            lay.addView(input2)
            alert.setView(lay)
            alert.setPositiveButton("Ok") { dialog, whichButton ->
                if (input.text.isNotEmpty() && input2.text.isNotEmpty()) {
                    val use = HashMap<String, Any>()
                    use["Bookname"] = input.text.toString()
                    use["From"] = input2.text.toString()
                    use["UserId"] = uid
                    fs.collection("Lendedbooks").add(use).addOnSuccessListener {
                        Toast.makeText(this@LendBook, "Added", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LendBook, "All Fields Are Required.", Toast.LENGTH_SHORT).show()
                }
            }

            alert.setNegativeButton("Cancel") { dialog, whichButton ->
                dialog.cancel()
            }
            alert.show()
        }
    }
}