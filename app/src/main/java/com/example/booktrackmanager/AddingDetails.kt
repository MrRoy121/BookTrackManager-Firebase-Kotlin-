package com.example.booktrackmanager
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import java.util.*

class AddingDetails : AppCompatActivity() {

    private lateinit var email: String
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private lateinit var fs: FirebaseFirestore
    private lateinit var title: EditText
    private lateinit var author: EditText
    private lateinit var cat: TextView
    private lateinit var uid: String
    private lateinit var ca: String

    private val cata = arrayOf("  ", "Comedy", "Thriller", "Horror", "Biography", "Cooking Book", "Science Fiction", "Romantic")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_details)

        val i = intent
        val spnr = findViewById<Spinner>(R.id.spx)
        email = i.getStringExtra("User") ?: ""
        fs = FirebaseFirestore.getInstance()
        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)
        val date = findViewById<TextView>(R.id.Date)
        title = findViewById(R.id.tle)
        cat = findViewById(R.id.curr)
        author = findViewById(R.id.aut)

        fs.collection("users").whereEqualTo("Email", email).get().addOnSuccessListener { q ->
            if (!q.isEmpty) {
                for (d in q) {
                    uid = d.id
                    Log.e("Tah", uid)
                }
            }
        }

        val ad1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, cata)
        ad1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnr.adapter = ad1

        spnr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, view: View, position: Int, id: Long) {
                ca = when (position) {
                    1 -> "Comedy"
                    2 -> "Thriller"
                    3 -> "Horror"
                    4 -> "Biography"
                    5 -> "Cooking Book"
                    6 -> "Science Fiction"
                    7 -> "Romantic"
                    8 -> "All Books"
                    else -> ""
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }

        findViewById<Button>(R.id.add).setOnClickListener {
            if (title.text.isEmpty() || author.text.isEmpty() || ca.isEmpty() || date.text.isEmpty()) {
                Toast.makeText(this@AddingDetails, "All Fields Are Required!!", Toast.LENGTH_SHORT).show()
            } else {
                val refer = fs.collection("books")
                val user = hashMapOf(
                    "Username" to uid,
                    "Title" to title.text.toString(),
                    "Author" to author.text.toString(),
                    "Catagory" to ca,
                    "Stats" to false,
                    "Date" to date.text.toString()
                )
                refer.add(user).addOnSuccessListener {
                    Toast.makeText(applicationContext, "Added Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AddingDetails, AddBook::class.java)
                    intent.putExtra("User", email)
                    startActivity(intent)
                    finish()
                }
            }
        }

        date.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this@AddingDetails, { view, year, monthOfYear, dayOfMonth ->
                date.text = "$dayOfMonth-${monthOfYear + 1}-$year"
            }, mYear, mMonth, mDay)
            datePickerDialog.show()
        }
    }
}