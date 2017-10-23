package me.dynerowicz.wtest.presenter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.dynerowicz.wtest.databinding.ItemPostalCodeRowBinding

class PostalCodeRowAdapter(
    var items: List<PostalCodeRow>? = null
) : RecyclerView.Adapter<PostalCodeRowAdapter.PostalCodeRowHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostalCodeRowHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPostalCodeRowBinding.inflate(inflater, parent, false)
        return PostalCodeRowHolder(binding)
    }

    override fun onBindViewHolder(holder: PostalCodeRowHolder, position: Int) {
        holder.fields.postalCodeRow = items?.get(position)
        holder.fields.executePendingBindings()
    }

    override fun getItemCount(): Int = items?.size ?: 0

    inner class PostalCodeRowHolder(val fields: ItemPostalCodeRowBinding) : RecyclerView.ViewHolder(fields.root)
}