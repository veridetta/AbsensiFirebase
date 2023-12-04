package com.example.absensi.activity.admin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.absensi.R
import com.example.absensi.adapter.SiswaAbsenAdapter
import com.example.absensi.adapter.SiswaAdapter
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

class AbsenDetailActivity : AppCompatActivity() {
    lateinit var tvNamaKelas : TextView
    lateinit var tvTanggal : TextView
    lateinit var btnSelesai : Button
    private lateinit var dataAdapter: SiswaAbsenAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var progressDialog: ProgressDialog

    private var kelasId = ""
    private var kelas = ""
    private var tanggal =""
    private var jumlahSiswa = 0
    val TAG = "LOAD DATA Siswa Activity"
    private val dataList: MutableList<AbsenModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen_detail)
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
        tvNamaKelas = findViewById(R.id.tv_nama_kelas)
        tvTanggal = findViewById(R.id.tv_tanggal)
        btnSelesai = findViewById(R.id.btnSelesai)
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
            layoutManager = GridLayoutManager(this@AbsenDetailActivity, 1)
            // set the custom adapter to the RecyclerView
            dataAdapter = SiswaAbsenAdapter(
                dataList,
                this@AbsenDetailActivity,
                { data -> listener(data) }
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
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
    @RequiresApi(Build.VERSION_CODES.O)
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
                    val resultAbsen = mFirestore.collection("absensi").whereEqualTo("tanggal", tanggal)
                        .whereEqualTo("siswaId", siswa.uid).get().await()
                    for (documentAbsen in resultAbsen) {
                        val absen = documentAbsen.toObject(AbsenModel::class.java)
                        datanya.docId = documentAbsen.id
                        datanya.status = absen.status
                        datanya.sudahAbsen="sudah"
                        Log.d(TAG,"Datanya Dokumen Absen: ${documentAbsen.id} => ${documentAbsen.data}")
                        Log.d( TAG,"Datanya : ${datanya.docId} => ${datanya.status}")
                    }
                    siswas.add(datanya)
                    Log.d(TAG, "Datanya Siswa: ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    dataList.addAll(siswas)
                    jumlahSiswa = siswas.size
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun listener(data:AbsenModel){
        Log.d(TAG,"Datanya Listener: ${data.siswa} => ${data.sudahAbsen} => ${data.status} " +
                "=> ${data.docId} => ${data.siswaId} ")
        editData(data)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun tambahData(data:AbsenModel){
        progressDialog.show()
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        val formatted = currentDateTime.format(formatter)
        val createdAt = formatted
        val listData = hashMapOf(
            "uid" to UUID.randomUUID().toString(),
            "tanggal" to tanggal,
            "siswaId" to data.siswaId,
            "siswa" to data.siswa,
            "status" to data.status,
            "sudahAbsen" to "sudah",
            "createdAt" to createdAt,
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("absensi")
            .add(listData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan data")
                data.sudahAbsen="sudah"
                data.docId = documentReference.id
                dataAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan data ${e.message}")
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun editData(data:AbsenModel){
        progressDialog.show()
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        val formatted = currentDateTime.format(formatter)
        val createdAt = formatted
        val listData = hashMapOf(
            "tanggal" to tanggal,
            "siswaId" to data.siswaId,
            "siswa" to data.siswa,
            "status" to data.status,
            "sudahAbsen" to "sudah",
            "editAt" to createdAt,
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("absensi")
            .document(data.docId.toString())
            .update(listData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan data")
                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan data ${e.message}")
            }
    }
    fun initClick(){
        btnSelesai.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
    }
}