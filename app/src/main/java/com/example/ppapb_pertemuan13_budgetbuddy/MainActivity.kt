package com.example.ppapb_pertemuan13_budgetbuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import com.example.ppapb_pertemuan13_budgetbuddy.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var updateId = ""
    private val budgetCollectionRef = firestore.collection("budgets")
    private val budgetListLiveData : MutableLiveData<List<Budget>>
    by lazy {
        MutableLiveData<List<Budget>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnAdd.setOnClickListener{
                val nominal = edtNominal.text.toString()
                val description = edtDescription.text.toString()
                val date = edtDate.text.toString()

                val newBudget = Budget(
                    nominal = nominal,
                    description = description,
                    date = date)
                addBudget(newBudget)
            }

            btnUpdate.setOnClickListener {
                if (updateId != "") {
                    val nominal = edtNominal.text.toString()
                    val description = edtDescription.text.toString()
                    val date = edtDate.text.toString()

                    val updateBudget = Budget(
                        id = updateId,
                        nominal = nominal,
                        description = description,
                        date = date
                    )
                    updateBudget(updateBudget)
                    setEmptyField()
                    updateId = ""
                }
            }

            listView.setOnItemClickListener {
                    adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Budget
                updateId = item.id
                edtNominal.setText(item.nominal)
                edtDescription.setText(item.description)
                edtDate.setText(item.date)
            }

            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener {
                    adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Budget
                deleteBudget(item)
                true
            }
        }

        observeBudgets()
        getAllBudgets()
    }

    private fun getAllBudgets(){
        budgetCollectionRef.addSnapshotListener { snapshot, error ->
            if (error != null){
                Log.d("MainActivity", "error Listening for budget changes", error)
                return@addSnapshotListener
            }
            val budgets = arrayListOf<Budget>()
            snapshot?.forEach{
                documentReference ->
                budgets.add(
                        Budget(documentReference.id,
                            documentReference.get("nominal").toString(),
                            documentReference.get("description").toString(),
                            documentReference.get("date").toString()
                        )
                )
            }
            if (budgets != null) {
                budgetListLiveData.postValue(budgets)
            }
        }
    }

    private fun observeBudgets(){
        budgetListLiveData.observe(this){
            budgets ->
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                budgets.toMutableList())
            binding.listView.adapter = adapter
        }
    }

    private fun addBudget(budget: Budget){
        budgetCollectionRef.add(budget).addOnFailureListener{
            Log.d("MainActivity", "Error adding budget : ", it)
        }
    }

    private fun updateBudget(budget: Budget){
        budgetCollectionRef.document(updateId).set(budget)
            .addOnFailureListener{
                Log.d("MainActivity", "Error updating budget : ", it)
            }
    }

    private fun deleteBudget(budget: Budget){
        budgetCollectionRef.document(updateId).delete()
            .addOnFailureListener{
                Log.d("MainActivity", "Error deleting budget : ", it)
            }
    }

    private fun setEmptyField(){
        with(binding){
            edtNominal.setText("")
            edtDescription.setText("")
            edtDate.setText("")
        }
    }
}