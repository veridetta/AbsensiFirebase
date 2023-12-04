package com.example.absensi.adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.absensi.R
import com.example.absensi.model.KelasModel
import com.example.absensi.model.SiswaModel
import java.util.Locale


class SiswaAdapter(
    private var dataList: MutableList<SiswaModel>,
    val context: Context,
    private val onEditClickListener: (SiswaModel) -> Unit,
) : RecyclerView.Adapter<SiswaAdapter.ProductViewHolder>() {
    public var filteredDataList: MutableList<SiswaModel> = mutableListOf()
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
                val nam = product.nama?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredDataList.add(product)
                    Log.d("Ada ", product.nama.toString())
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
            .inflate(R.layout.item_siswa, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredDataList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentData = filteredDataList[position]

        holder.tvNama.text = currentData.nama
        holder.btnUbah.setOnClickListener { onEditClickListener(currentData) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val btnUbah: LinearLayout = itemView.findViewById(R.id.btnUbah)
    }
}
