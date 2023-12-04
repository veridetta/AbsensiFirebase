package com.example.absensi.activity.admin

import android.app.ProgressDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.absensi.R
import com.example.absensi.adapter.RekapAbsenAdapter
import com.example.absensi.adapter.SiswaAbsenAdapter
import com.example.absensi.helper.showSnack
import com.example.absensi.model.AbsenModel
import com.example.absensi.model.SiswaModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class ReportDetailActivity : AppCompatActivity() {
    lateinit var tvNamaKelas : TextView
    lateinit var tvTanggal : TextView
    private lateinit var dataAdapter: RekapAbsenAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var progressDialog: ProgressDialog

    private var kelasId = ""
    private var kelas = ""
    private var tanggal =""

    val TAG = "LOAD DATA Siswa Activity"
    private val dataList: MutableList<AbsenModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)
        initView()
        initIntent()
        initRc()
        initData()
        initCari()
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rcBarang)
        contentView = findViewById(R.id.contentView)
        searchLayout = findViewById(R.id.searchLayout)
        btnCari = findViewById(R.id.btnCari)
        tvNamaKelas = findViewById(R.id.tv_nama_kelas)
        tvTanggal = findViewById(R.id.tv_tanggal)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }

    private fun initIntent(){
        kelasId = intent.getStringExtra("kelasId").toString()
        kelas = intent.getStringExtra("kelas").toString()
        tanggal = intent.getStringExtra("tanggal").toString()
        tvNamaKelas.text = kelas
        tvTanggal.text = tanggal
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@ReportDetailActivity, 1)
            // set the custom adapter to the RecyclerView
            dataAdapter = RekapAbsenAdapter(
                dataList,
                this@ReportDetailActivity,
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
                val siswas = mutableListOf<AbsenModel>()
                for (document in result) {
                    //buat nampung model absen baru
                    var datanya = AbsenModel()
                    val siswa = document.toObject(SiswaModel::class.java)
                    datanya.siswa = siswa.nama
                    datanya.siswaId = siswa.uid
                    datanya.status="Pilih Status"
                    datanya.sudahAbsen="belum"
                    //ambil data collection path absensi where tanggal dan uid siswa
                    val resultAbsen = mFirestore.collection("absensi").
                    whereEqualTo("tanggal", tanggal).whereEqualTo("siswaId", siswa.uid).get().await()
                    for (documentAbsen in resultAbsen) {
                        val absen = documentAbsen.toObject(AbsenModel::class.java)
                        datanya.docId = documentAbsen.id
                        datanya.status = absen.status
                        datanya.sudahAbsen="sudah"
                        Log.d(TAG, "Datanya : ${documentAbsen.id} => ${documentAbsen.data}")
                    }
                    siswas.add(datanya)
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

}