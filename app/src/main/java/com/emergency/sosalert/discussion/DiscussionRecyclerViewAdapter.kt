package com.emergency.sosalert.discussion

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emergency.sosalert.R

class DiscussionRecyclerViewAdapter(
    private val discussionList: ArrayList<Discussion>
) :
    RecyclerView.Adapter<DiscussionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.discussion_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return discussionList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bind(discussionList[i])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.discussion_title)
        val desc = itemView.findViewById<TextView>(R.id.discussion_desc)

        fun bind(discussion: Discussion) {
            title.text = discussion.title
            desc.text = discussion.description
        }
    }

}