package com.example.apiintegrationv2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class inloggScreen : AppCompatActivity() {

    lateinit var logIn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inlogg_screen)
        val email = intent.getStringExtra("email")
        logIn = findViewById(R.id.createSaving)
        if (email != null) {
            fetchUserInfo(email)
        }

        val fm = supportFragmentManager
        fm.beginTransaction().add(R.id.showFragment, SavingsFragment::class.java, null)
            .commit()

        logIn.setOnClickListener{ v ->
            val intent = Intent(this, CreateSaving::class.java)
            intent.putExtra("email",email)
            startActivity(intent)
        }

        findViewById<Button>(R.id.loggOut).setOnClickListener{ v ->
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

    }

    private fun fetchUserInfo(email: String) {
        val db = Firebase.firestore
        val usersRef = db.collection("users")
        logIn = findViewById(R.id.createSaving)

        val query = usersRef.whereEqualTo("email", email)

        val listenerRegistration = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("fetchUserInfo", "Error fetching user info: ${exception.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val document = snapshot.documents[0]
                val amountGet = document.getString("amount")
                val newAmountGet = document.getLong("totalSaved")

                if (amountGet == newAmountGet.toString()) {
                    logIn.isInvisible = false
                } else {
                    logIn.isInvisible = true
                }
            }
        }
    }
}