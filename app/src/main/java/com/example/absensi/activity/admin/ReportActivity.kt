package com.example.absensi.activity.admin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.absensi.R
import com.example.absensi.activity.LoginActivity
import com.example.absensi.adapter.KelasAdapter
import com.example.absensi.adapter.RekapAbsenAdapter
import com.example.absensi.model.KelasModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

class ReportActivity : AppCompatActivity() {
    lateinit var spKelas : Spinner
    lateinit var etTanggal : TextView

    private lateinit var contentView: RelativeLayout
    private lateinit var btnSimpan: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressDialog: ProgressDialog

    val TAG = "LOAD DATA ReportActivity"
    private val dataList: MutableList<KelasModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    var kelas =""
    var kelasId = ""
    var tanggal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        initView()
        initData()
        initClick()
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()

        spKelas = findViewById(R.id.spKelas)
        etTanggal = findViewById(R.id.etTanggal)
        contentView = findViewById(R.id.contentView)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }

    private fun initData(){
        readData()

    }
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("kelas").get().await()
                val plants = mutableListOf<KelasModel>()
                var kelasArray = mutableListOf<String>()
                var kelasIdArray = mutableListOf<String>()
                for (document in result) {
                    val plant = document.toObject(KelasModel::class.java)
                    val docId = document.id
                    plant.docId = docId
                    kelasArray.add(plant.nama.toString())
                    kelasIdArray.add(plant.docId.toString())
                    plants.add(plant)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    dataList.addAll(plants)
                    spKelas.adapter = ArrayAdapter(this@ReportActivity, android.R.layout.simple_spinner_item, kelasArray)

                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }
    private fun initClick(){
        btnSimpan.setOnClickListener {
            kelasId = dataList[spKelas.selectedItemPosition].docId.toString()
            kelas = dataList[spKelas.selectedItemPosition].nama.toString()
            tanggal = etTanggal.text.toString()
            if(tanggal.isEmpty()){
                etTanggal.error = "Tanggal tidak boleh kosong"
                etTanggal.requestFocus()
                return@setOnClickListener
            }
            val intent = Intent(this, ReportDetailActivity::class.java)
            intent.putExtra("kelasId", kelasId)
            intent.putExtra("kelas", kelas)
            intent.putExtra("tanggal", tanggal)
            startActivity(intent)
        }
        etTanggal.setOnClickListener {
            showDatePickerDialog(etTanggal)
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
}