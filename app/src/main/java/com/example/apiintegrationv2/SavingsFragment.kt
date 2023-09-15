package com.example.apiintegrationv2

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SavingsFragment : Fragment() {

    private lateinit var name: TextView
    private lateinit var newAmount: TextView
    private lateinit var addMoney: EditText
    private lateinit var endDate: TextView
    private lateinit var progBarr: ProgressBar
    private lateinit var button: Button
    private var currentTotalSaved: Int = 0
    private var newTotalSaved: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_savings, container, false)
        val email = (activity as inloggScreen).intent.getStringExtra("email")
        val db = Firebase.firestore
        (activity as inloggScreen).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        name = view.findViewById(R.id.getName)
        newAmount = view.findViewById(R.id.newAmount)
        addMoney = view.findViewById(R.id.addMoney)
        progBarr = view.findViewById(R.id.progressBar)
        button = view.findViewById(R.id.money)
        endDate = view.findViewById(R.id.endDate)

        if (email != null) {
            fetchUserInfo(email)
        }
        button.setOnClickListener(View.OnClickListener {
            val amountString = addMoney.text.toString()
            val amount = amountString.toLongOrNull()

            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val userId = document.id
                        currentTotalSaved = (document.getLong("totalSaved") ?: 0).toInt()
                        newTotalSaved = (currentTotalSaved + (amount ?: 0)).toInt()

                        val user = hashMapOf(
                            "totalSaved" to newTotalSaved
                        )

                        db.collection("users").document(userId)
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d(ContentValues.TAG, "totalSaved uppdaterades framgångsrikt")
                                updateProgressBar(newTotalSaved)
                                if (email != null) {
                                    fetchUserInfo(email)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w(ContentValues.TAG, "Fel vid uppdatering av totalSaved", e)
                            }
                    } else {
                        Log.w(ContentValues.TAG, "Användaren hittades inte")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Fel vid hämtning av användare", exception)
                }
            addMoney.text.clear()
        })
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun fetchUserInfo(email: String) {
        val db = Firebase.firestore
        val usersRef = db.collection("users")
        usersRef.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val nameGet = document.getString("savingsName")
                    val newAmountGet = document.getLong("totalSaved")
                    val endDateGet = document.getDate("endDate")
                    val toAmountGet = document.getString("amount")

                    if (toAmountGet != null) {
                        progBarr.max = toAmountGet.toInt()
                    }
                    if (newAmountGet != null) {
                        progBarr.progress = newAmountGet.toInt()
                    }
                    name.text = "$nameGet"
                    newAmount.text = "Du har sparat \n" +
                            "$newAmountGet kr tills du når $toAmountGet"
                    endDate.text = "Du ska nå ditt mål \n" +
                            "$endDateGet"
                } else {
                    name.text = "Du har inga sparmål!\n" +
                            "Skapa ett nedan"
                    progBarr.isInvisible = true
                    addMoney.isInvisible = true
                    button.isInvisible = true
                }
            }
            .addOnFailureListener { exception ->
                Log.e("fail","misslyckas")
            }
    }

    private fun updateProgressBar(Amount: Int){
        if (Amount != null) {
            progBarr.progress = Amount.toInt()
        }
    }

}