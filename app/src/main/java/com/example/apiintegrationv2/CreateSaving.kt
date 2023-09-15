package com.example.apiintegrationv2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class CreateSaving : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var amount: EditText
    private lateinit var date: CalendarView
    private lateinit var info: TextView
    private var totalMonth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_saving)

        name = findViewById(R.id.NameSaving)
        amount = findViewById(R.id.AmountSaving)
        date = findViewById(R.id.whenSaving)
        info = findViewById(R.id.info)

        val today = Calendar.getInstance()
        val selectedDate = Calendar.getInstance()
        date.setOnDateChangeListener { _, year, month, dayOfMonth ->

            selectedDate.set(year, month, dayOfMonth)
            totalMonth = monthsDifference(today, selectedDate)

            val num: Int = amountPerMonth(amount.text, totalMonth)
            if (!num.equals(null)) {
                info.text = "Du måste spara $num i månaden"
            }
        }

        var email = intent.getStringExtra("email")
        val db = Firebase.firestore

        findViewById<Button>(R.id.create).setOnClickListener {v ->

            val user = hashMapOf(
                "email" to email.toString(),
                "savingsName" to name.text.toString(),
                "amount" to amount.text.toString(),
                "startDate" to today.time,
                "endDate" to selectedDate.time,
                "totalSaved" to 0
            )
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val userId = document.id
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d("tag", "Användaruppgifter uppdaterades framgångsrikt")
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Fel vid uppdatering av användaruppgifter", e)
                            }
                    } else {
                        db.collection("users")
                            .add(user)
                            .addOnSuccessListener { documentReference ->
                                Log.d("jag", "Användare lades till med ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Fel vid tillägg av användare", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Fel vid hämtning av användarinformation", e)
                }


            val intent = Intent(this,inloggScreen::class.java)
            intent.putExtra("email",email)
            startActivity(intent)
        }


        findViewById<Button>(R.id.loggOut2).setOnClickListener{ v ->
            Firebase.auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    fun monthsDifference(startDate: Calendar, endDate: Calendar): Int {
        val diffYear = endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR)
        val diffMonth = endDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH)
        return diffYear * 12 + diffMonth
    }

    fun amountPerMonth(amount: Editable, month: Int): Int {
        val amountText = amount.toString()
        val amountValue = amountText.toInt()
        val difference = amountValue / month
        return difference
    }
}