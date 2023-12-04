package com.example.absensi.activity.admin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.absensi.R
import com.example.absensi.helper.showSnack
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddSiswaActivity : AppCompatActivity() {
    lateinit var etNama : EditText
    lateinit var contentView: RelativeLayout
    lateinit var btnBack: ImageView
    lateinit var btnSimpan: Button
    lateinit var tvJudul: TextView

    private var type = "" //edit atau add
    private var uid = ""
    private var docId = ""
    private var nama = ""
    private var kelasId = ""
    private var kelas = ""
    private var editAt = ""
    private var jumlahSiswa = ""
    private var createdAt = ""

    lateinit var progressDialog: ProgressDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_siswa)
        initView()
        initIntent()
        setIntent()
        initClick()
    }
    private fun initView(){
        etNama = findViewById(R.id.etNama)
        contentView = findViewById(R.id.contentView)
        tvJudul = findViewById(R.id.tvJudul)
        btnBack = findViewById(R.id.btnBack)
        btnSimpan = findViewById(R.id.btnSimpan)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        uid = intent.getStringExtra("uid").toString()
        docId = intent.getStringExtra("docId").toString()
        nama = intent.getStringExtra("nama").toString()
        editAt = intent.getStringExtra("editAt").toString()
        kelasId = intent.getStringExtra("kelasId").toString()
        kelas = intent.getStringExtra("kelas").toString()
        jumlahSiswa = intent.getStringExtra("jumlahSiswa").toString()
        createdAt = intent.getStringExtra("createdAt").toString()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setIntent(){
        if(type == "edit"){
            etNama.setText(nama)
            tvJudul.text = "Edit Siswa"
            btnSimpan.text = "Simpan"
            //tanggal sekarang dan jam
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formatted = currentDateTime.format(formatter)
            editAt = formatted
        }else{
            tvJudul.text = "Tambah Siswa"
            btnSimpan.text = "Tambah"
            //tanggal sekarang dan jam
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formatted = currentDateTime.format(formatter)
            createdAt = formatted
        }
    }
    private fun chekData(){
        if(etNama.text.toString().isEmpty()){
            etNama.error = "Nama kelas tidak boleh kosong"
            etNama.requestFocus()
            return
        }
        //tambah atau edit data
        if(type == "edit"){
            editData()
        }else{
            tambahData()
        }

    }
    private fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
        btnSimpan.setOnClickListener {
            chekData()
        }
    }
    private fun tambahData(){
        var jumlah = 0
        if(jumlahSiswa.isNotEmpty() || jumlahSiswa != ""){
            jumlah = jumlahSiswa.toInt()+1
        }
        progressDialog.show()
        val listData = hashMapOf(
            "uid" to UUID.randomUUID().toString(),
            "nama" to etNama.text.toString(),
            "kelas" to kelas,
            "kelasId" to kelasId,
            "editAt" to editAt,
            "createdAt" to createdAt,
        )
        val db = FirebaseFirestore.getInstance()
        // Add the product data to Firestore
        db.collection("siswa")
            .add(listData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                //update data jumlahsiswa dalam collection kelas where kelasId
                val listData = hashMapOf(
                    "jumlahSiswa" to jumlah.toString(),
                )
                val db = FirebaseFirestore.getInstance()
                db.collection("kelas")
                    .document(kelasId)
                    .update(listData as Map<String, Any>)
                    .addOnSuccessListener { documentReference ->
                        showSnack(this,"Berhasil menyimpan data")
                        progressDialog.dismiss()
                        // Redirect to SellerActivity fragment home
                        val intent = Intent(this, AdminActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // Error occurred while adding product
                        progressDialog.dismiss()
                        showSnack(this,"Gagal menyimpan data ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan data ${e.message}")
            }
    }
    private fun editData(){
        progressDialog.show()
        val listData = hashMapOf(
            "nama" to etNama.text.toString(),
            "editAt" to editAt,
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("siswa")
            .document(docId)
            .update(listData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan data")
                progressDialog.dismiss()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan data ${e.message}")
            }
    }
}