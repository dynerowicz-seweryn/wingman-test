package me.dynerowicz.wtest.presenter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import me.dynerowicz.wtest.database.ScrollingCursor
import me.dynerowicz.wtest.databinding.ItemPostalCodeRowBinding

class PostalCodeRowAdapter : RecyclerView.Adapter<PostalCodeRowAdapter.PostalCodeRowHolder>() {
    var postalCodeRows: List<PostalCodeRow>? = null
    var postalCodeRowResults: ScrollingCursor? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostalCodeRowHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPostalCodeRowBinding.inflate(inflater, parent, false)
        return PostalCodeRowHolder(binding)
    }

    override fun onBindViewHolder(holder: PostalCodeRowHolder, position: Int) {
        val localList = postalCodeRows

        localList?.let {
            val postalCodeRow = localList[position]
            holder.fields.postalCodeRow = postalCodeRow
            Log.v(TAG, "onBindViewHolder : ${postalCodeRow.postalCode}-${String.format("%03d", postalCodeRow.extension)}")
            holder.fields.executePendingBindings()
        }
    }

    override fun getItemCount(): Int = postalCodeRows?.size ?: 0

    inner class PostalCodeRowHolder(val fields: ItemPostalCodeRowBinding) : RecyclerView.ViewHolder(fields.root)

    companion object {
        const val TAG = "PostalCodeRowAdapter"
    }
}