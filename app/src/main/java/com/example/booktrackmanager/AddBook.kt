package com.example.booktrackmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddBook : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var simpleGrid: GridView
    private lateinit var names: ArrayList<String>
    private lateinit var log: ArrayList<Boolean>
    private lateinit var uid: String
    private lateinit var email: String
    private lateinit var fs: FirebaseFirestore
    private lateinit var customAdapter: Adapter
    private lateinit var sss: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        val searchView = findViewById<SearchView>(R.id.searchView)
        val i = intent
        email = i.getStringExtra("User").toString()

        findViewById<View>(R.id.menu).setOnClickListener {
            val navDrawer = findViewById<DrawerLayout>(R.id.my_drawer_layout)
            if (!navDrawer.isDrawerOpen(Gravity.LEFT)) navDrawer.openDrawer(Gravity.LEFT) else navDrawer.closeDrawer(
                Gravity.LEFT
            )
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            val intent = Intent(this@AddBook, AddingDetails::class.java)
            intent.putExtra("User", i.getStringExtra("User"))
            startActivity(intent)
            finish()
        }

        setNavigationDrawer()
        fs = FirebaseFirestore.getInstance()

        simpleGrid = findViewById(R.id.simpleGridView)
        names = ArrayList()
        log = ArrayList()
        customAdapter = Adapter(this@AddBook, names, log)

        fs.collection("users").whereEqualTo("Email", i.getStringExtra("User")).get()
            .addOnSuccessListener { q ->
                if (!q.isEmpty) {
                    for (d in q) {
                        uid = d.id
                    }
                    fs.collection("books").get().addOnSuccessListener { documentSnapshot ->
                        if (!documentSnapshot.isEmpty) {
                            for (d in documentSnapshot) {
                                names.add(d.getString("Title")!!)
                                log.add(d.getBoolean("Stats")!!)
                            }
                        }
                    }
                }
            }

        simpleGrid.setOnItemClickListener { parent, view, position, id ->
            val dialog = Dialog(this@AddBook)
            dialog.setContentView(R.layout.dialog)
            val tit = dialog.findViewById<TextView>(R.id.title)
            val aut = dialog.findViewById<TextView>(R.id.author)
            val pri = dialog.findViewById<TextView>(R.id.price)
            val buy = dialog.findViewById<TextView>(R.id.date)
            val e = dialog.findViewById<EditText>(R.id.text2)

            fs.collection("books").whereEqualTo("Title", names[position]).get()
                .addOnSuccessListener { q ->
                    for (d in q) {
                        tit.text = d.getString("Title")
                        aut.text = d.getString("Author")
                        pri.text = d.getString("Price")
                        buy.text = d.getString("Date")
                    }
                }

            fs.collection("page").document(uid).collection("book").document(names[position]).get()
                .addOnSuccessListener { d ->
                    if (d.exists()) {
                        e.setText(d.getString("page no"))
                    }
                }

            val userx = HashMap<String, Any>()
            val dialogButton = dialog.findViewById<Button>(R.id.OK)
            dialogButton.setOnClickListener {
                if (e.text.toString().isNotEmpty()) {
                    fs.collection("books").whereEqualTo("Title", names[position])
                        .whereEqualTo("Username", uid).get().addOnSuccessListener { q ->
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

                    val user1 = HashMap<String, Any>()
                    user1["bookid"] = sss
                    user1["userid"] = uid
                    val user = HashMap<String, Any>()
                    user["page no"] = e.text.toString()
                    fs.collection("Ongoingbooks").add(user1)
                    fs.collection("page").document(uid).collection("book").document(names[position])
                        .set(user)
                    dialog.dismiss()
                } else {
                    Toast.makeText(applicationContext, "Add A Number To Add", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            val dialogButto = dialog.findViewById<Button>(R.id.delete)
            dialogButto.text = "Completed"
            dialogButto.setOnClickListener {
                fs.collection("books").whereEqualTo("Title", names[position]).get()
                    .addOnSuccessListener { queryDocumentSnapshots ->
                        for (D in queryDocumentSnapshots) {
                            fs.collection("books").document(D.id).delete()
                            dialog.dismiss()
                            Toast.makeText(applicationContext, "Deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            dialog.show()
        }

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshLayou)
        swipeRefreshLayout.setOnRefreshListener {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            swipeRefreshLayout.isRefreshing = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                customAdapter = Adapter(this@AddBook, names, log)
                simpleGrid.adapter = customAdapter
                customAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun setNavigationDrawer() {
        drawerLayout = findViewById(R.id.my_drawer_layout)
        val navView = findViewById<NavigationView>(R.id.navigation)
        navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId
            when (itemId) {
                R.id.logout -> logout()
                R.id.wish -> startActivity(Intent(applicationContext, Wishlist::class.java))
                R.id.lend -> {
                    val i = Intent(this@AddBook, LendBook::class.java)
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c8 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "book")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c1 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "Comedy")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c2 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "Thriller")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c3 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "Horror")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c4 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "Biography")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c5 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "Cooking Book")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c6 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "Science Fiction")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.c7 -> {
                    val i = Intent(this@AddBook, catagory::class.java)
                    i.putExtra("s", "Romantic")
                    i.putExtra("User", email)
                    startActivity(i)
                }

                R.id.ongoin -> {
                    val i = Intent(this@AddBook, Ongoing::class.java)
                    i.putExtra("User", email)
                    startActivity(i)
                }
            }
            false
        })
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val i = Intent(this@AddBook, MainActivity::class.java)
        startActivity(i)
        finish()
    }
}