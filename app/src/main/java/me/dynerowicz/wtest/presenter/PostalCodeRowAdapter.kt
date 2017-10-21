package me.dynerowicz.wtest.presenter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView

class PostalCodeRowAdapter(
    var items: List<PostalCodeRow>? = null
) : RecyclerView.Adapter<PostalCodeRowAdapter.PostalCodeRowHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostalCodeRowHolder(TextView(parent.context))

    override fun onBindViewHolder(holder: PostalCodeRowHolder, position: Int) {
        holder.textView.text = items?.get(position).toString()
    }

    override fun getItemCount(): Int = items?.size ?: 0

    inner class PostalCodeRowHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}