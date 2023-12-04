package com.example.absensi.adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.absensi.R
import com.example.absensi.model.AbsenModel
import com.example.absensi.model.KelasModel
import com.example.absensi.model.SiswaModel
import java.util.Locale


class SiswaAbsenAdapter(
    private var dataList: MutableList<AbsenModel>,
    val context: Context,
    private val onChangeListener: (AbsenModel) -> Unit,
) : RecyclerView.Adapter<SiswaAbsenAdapter.ProductViewHolder>() {
    public var filteredDataList: MutableList<AbsenModel> = mutableListOf()
    init {
        filteredDataList.addAll(dataList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredDataList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }
    fun filter(query: String) {
        filteredDataList.clear()
        if (query !== null || query !=="") {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (product in dataList) {
                val nam = product.siswa?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredDataList.add(product)
                    Log.d("Ada ", product.siswa.toString())
                }
            }
        } else {
            filteredDataList.addAll(dataList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredDataList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_absen, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredDataList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentData = filteredDataList[position]

        holder.tvNama.text = currentData.siswa
        //set spinner dari string.xml status
        val status = context.resources.getStringArray(R.array.status_absen)
        val adapterStatus = ArrayAdapter(context, android.R.layout.simple_spinner_item, status)
        holder.spAbsen.adapter = adapterStatus
        //set spinner dari string.xml status
        holder.spAbsen.setSelection(status.indexOf(currentData.status))
        //spinner change listener
        var r = 0
        holder.spAbsen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                val item = parent.getItemAtPosition(position).toString()
                currentData.status = item
                if(r>0){
                    currentData.sudahAbsen="sudah"
                }
                r=+1
                onChangeListener(currentData)
                Log.d("Status", item)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val spAbsen: Spinner = itemView.findViewById(R.id.spAbsen)
    }
}
