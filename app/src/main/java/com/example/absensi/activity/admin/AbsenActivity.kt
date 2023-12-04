package com.example.absensi.activity.admin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.absensi.R
import com.example.absensi.helper.showSnack
import com.example.absensi.model.AbsenModel
import com.example.absensi.model.SiswaModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

class AbsenActivity : AppCompatActivity() {
    private var kelasId = ""
    private var kelas = ""
    private var tanggal = ""
    lateinit var etTanggal :TextView
    lateinit var btnSimpan: Button
    lateinit var btnBack: ImageView
    var iterasi = 0

    private lateinit var progressDialog: ProgressDialog
    private val dataList: MutableList<AbsenModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore
    private val TAG = "LOAD DATA Absen Activity"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen)
        initView()
        initIntent()
        initClick()
    }

    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()
        btnSimpan = findViewById(R.id.btnSimpan)
        etTanggal = findViewById(R.id.etTanggal)
        btnBack = findViewById(R.id.btnBack)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
    }
    private fun initIntent(){
        kelasId = intent.getStringExtra("kelasId").toString()
        kelas = intent.getStringExtra("kelas").toString()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initClick(){
        etTanggal.setOnClickListener {
            showDatePickerDialog(etTanggal)
        }
        btnSimpan.setOnClickListener {
            tanggal = etTanggal.text.toString()
            //pindah ke AbsenDetailActivity
            if (iterasi==0){
                initData()
            }else{
                val intent = Intent(this, AbsenDetailActivity::class.java)
                intent.putExtra("kelasId", kelasId)
                intent.putExtra("kelas", kelas)
                intent.putExtra("tanggal", tanggal)
                startActivity(intent)
            }
        }
        btnBack.setOnClickListener {
            finish()
        }
    }
    private fun showDatePickerDialog(input : TextView){
        val datePicker = DatePicker(this)
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { view: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                input.text = selectedDate
            },
            calendar.get(Calendar.YEAR), // Tahun
            calendar.get(Calendar.MONTH), // Bulan
            calendar.get(Calendar.DAY_OF_MONTH) // Hari
        )

        // Tampilkan dialog
        datePickerDialog.show()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initData(){
        readData()
        insertMultiple()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun tambahData(data: AbsenModel){
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
                showSnack(this,"Berhasil mengatur data")
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan data ${e.message}")
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("siswa").
                whereEqualTo("kelasId", kelasId).get().await()
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
                    }
                    //tambahData(datanya)
                    siswas.add(datanya)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }
                withContext(Dispatchers.Main) {
                    dataList.addAll(siswas)
                    insertMultiple()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }
    private fun insertMultiple() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        for (i in 0 until dataList.size) {
            if(dataList[i].sudahAbsen=="sudah"){
                continue
            }
            val listData = hashMapOf(
                "uid" to UUID.randomUUID().toString(),
                "tanggal" to tanggal,
                "siswaId" to dataList[i].siswaId,
                "siswa" to dataList[i].siswa,
                "status" to dataList[i].status,
                "sudahAbsen" to "sudah"
            )
            val docRef = db.collection("absensi").document()
            batch.set(docRef, listData)
        }
        batch.commit().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showSnack(this, "Berhasil mengatur data")
                btnSimpan.text = "Lanjutkan"
                iterasi += 1 // Menggunakan operator += untuk menambah variabel iterasi
            } else {
                // Penanganan kesalahan jika batch commit gagal
                val exception = task.exception
                showSnack(this, "Gagal mengatur data: ${exception?.message}")
            }
        }
    }

}