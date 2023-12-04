package com.example.absensi.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.absensi.R
import com.example.absensi.activity.LoginActivity
import com.example.absensi.adapter.KelasAdapter
import com.example.absensi.model.KelasModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {
    private lateinit var dataAdapter: KelasAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var btnLogout: CardView
    private lateinit var btnAdd: CardView
    private lateinit var btnReport: CardView
    private lateinit var progressDialog: ProgressDialog

    val TAG = "LOAD DATA AdminActvy"
    private val dataList: MutableList<KelasModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        initView()
        initRc()
        initData()
        initCari()
        initClick()
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rcBarang)
        contentView = findViewById(R.id.contentView)
        searchLayout = findViewById(R.id.searchLayout)
        btnCari = findViewById(R.id.btnCari)
        btnAdd = findViewById(R.id.btnAdd)
        btnLogout = findViewById(R.id.btnLogout)
        btnReport = findViewById(R.id.btnReport)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@AdminActivity, 1)
            // set the custom adapter to the RecyclerView
            dataAdapter = KelasAdapter(
                dataList,
                this@AdminActivity,
                { barang -> editData(barang) },
                { barang -> listData(barang) }
            )
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = dataAdapter
    }
    private fun initCari(){
        dataAdapter.filter("")
        btnCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dataAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("kelas").get().await()
                val plants = mutableListOf<KelasModel>()
                for (document in result) {
                    val plant = document.toObject(KelasModel::class.java)
                    val docId = document.id
                    plant.docId = docId
                    plants.add(plant)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    dataList.addAll(plants)
                    dataAdapter.filteredDataList.addAll(plants)
                    dataAdapter.notifyDataSetChanged()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }

    private fun editData(data: KelasModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("docId", data.docId)
        intent.putExtra("uid", data.uid)
        intent.putExtra("nama", data.nama)
        intent.putExtra("jumlahSiswa", data.jumlahSiswa)

        startActivity(intent)
    }
    private fun listData(data: KelasModel) {
        // Dialog konfirmasi
        //intent ke homeActivity fragment add
        val intent = Intent(this, SiswaActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("docId", data.docId)
        intent.putExtra("uid", data.uid)
        intent.putExtra("nama", data.nama)
        intent.putExtra("jumlahSiswa", data.jumlahSiswa.toString())
        Log.d(TAG, "initIntent Admin: ${data.jumlahSiswa}")
        startActivity(intent)
    }

    private fun initClick(){
        btnLogout.setOnClickListener {
            // Hapus shared preferences
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Arahkan ke MainActivity dengan membersihkan stack aktivitas
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        btnAdd.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("type", "tambah")
            startActivity(intent)
        }
        btnReport.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }
    }
}