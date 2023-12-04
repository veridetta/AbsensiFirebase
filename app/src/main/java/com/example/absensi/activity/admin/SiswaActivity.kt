package com.example.absensi.activity.admin

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
import com.example.absensi.adapter.SiswaAdapter
import com.example.absensi.model.KelasModel
import com.example.absensi.model.SiswaModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SiswaActivity : AppCompatActivity() {
    private lateinit var dataAdapter: SiswaAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var btnAdd: CardView
    private lateinit var btnAbsen: CardView
    private lateinit var progressDialog: ProgressDialog

    private var kelasId = ""
    private var kelas = ""
    private var jumlahSiswa = ""

    val TAG = "LOAD DATA Siswa Activity"
    private val dataList: MutableList<SiswaModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_siswa)
        initView()
        initIntent()
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
        btnAbsen = findViewById(R.id.btnAbsensi)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }

    private fun initIntent(){
        kelasId = intent.getStringExtra("docId").toString()
        kelas = intent.getStringExtra("nama").toString()
        jumlahSiswa = intent.getStringExtra("jumlahSiswa").toString()
        Log.d(TAG, "initIntent Siswa: $kelasId $kelas $jumlahSiswa")
    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@SiswaActivity, 1)
            // set the custom adapter to the RecyclerView
            dataAdapter = SiswaAdapter(
                dataList,
                this@SiswaActivity,
                { data -> editData(data) }
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
                val result = mFirestore.collection("siswa").whereEqualTo("kelasId", kelasId).get().await()
                val siswas = mutableListOf<SiswaModel>()
                for (document in result) {
                    val siswa = document.toObject(SiswaModel::class.java)
                    val docId = document.id
                    siswa.docId = docId
                    siswas.add(siswa)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    dataList.addAll(siswas)
                    dataAdapter.filteredDataList.addAll(siswas)
                    dataAdapter.notifyDataSetChanged()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }

    private fun editData(data: SiswaModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, AddSiswaActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("docId", data.docId)
        intent.putExtra("uid", data.uid)
        intent.putExtra("nama", data.nama)
        intent.putExtra("kelasId", data.kelasId)
        intent.putExtra("kelas", data.kelas)
        intent.putExtra("jumlahSiswa", jumlahSiswa)
        startActivity(intent)
    }

    private fun initClick(){
        btnAdd.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AddSiswaActivity::class.java)
            intent.putExtra("type", "tambah")
            intent.putExtra("kelasId", kelasId)
            intent.putExtra("kelas", kelas)
            intent.putExtra("jumlahSiswa", jumlahSiswa)
            startActivity(intent)
        }
        btnAbsen.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AbsenActivity::class.java)
            intent.putExtra("kelasId", kelasId)
            intent.putExtra("kelas", kelas)
            intent.putExtra("jumlahSiswa", jumlahSiswa)
            startActivity(intent)
        }
    }
}