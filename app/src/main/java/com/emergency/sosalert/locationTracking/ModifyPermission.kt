package com.emergency.sosalert.locationTracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.chat.ChatListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_modify_permission.*
import kotlinx.android.synthetic.main.chat_list_item.view.*
import kotlinx.android.synthetic.main.chat_list_item.view.friend_name
import kotlinx.android.synthetic.main.chat_list_item.view.user_image
import kotlinx.android.synthetic.main.permlist_item.*
import kotlinx.android.synthetic.main.permlist_item.view.*
import java.security.Permission

class ModifyPermission : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_permission)
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        modPermRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        FirebaseFirestore.getInstance().collection("user").document(currentUid!!).get()
            .addOnSuccessListener {
                val enabledList = it.data!!["allowTrackingList"] as ArrayList<String>
                modPermRecycler.adapter = PermissionAdapter(enabledList)
            }

        backBtnPerm.setOnClickListener {
            onBackPressed()
        }
    }

    class PermissionAdapter(private val userlist: ArrayList<String>) :
        RecyclerView.Adapter<PermissionAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name = itemView.perm_friend_name
            val image = itemView.perm_user_image
            val btn = itemView.buttonRemoveTracking
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.permlist_item, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.image.clipToOutline = true

            FirebaseFirestore.getInstance().collection("user").document(userlist[position]).get()
                .addOnSuccessListener {
                    holder.name.text = it.data!!["name"].toString()
                }

            FirebaseStorage.getInstance().reference.child("profilepicture/${userlist[position]}").downloadUrl.addOnSuccessListener {
                Glide.with(holder.itemView.context).load(it).into(holder.image)
            }

            holder.btn.setOnClickListener {
                FirebaseFirestore.getInstance().collection("user")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .update("allowTrackingList", FieldValue.arrayRemove(userlist[position]))
                userlist.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
            }
        }

        override fun getItemCount(): Int {
            return userlist.size
        }
    }
}