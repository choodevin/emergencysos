package com.emergency.sosalert.discussion

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.emergency.sosalert.R
import com.firebase.ui.firestore.SnapshotParser
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_my_post.*
import kotlinx.android.synthetic.main.discussion_list_item.view.discussionImage
import kotlinx.android.synthetic.main.discussion_list_item.view.discussion_desc
import kotlinx.android.synthetic.main.discussion_list_item.view.discussion_title
import kotlinx.android.synthetic.main.discussion_list_item.view.image_loading_bar
import kotlinx.android.synthetic.main.discussion_list_item.view.ownerImage
import kotlinx.android.synthetic.main.discussion_list_item.view.ownerName
import kotlinx.android.synthetic.main.discussion_list_item.view.postDate
import kotlinx.android.synthetic.main.my_post_list_item.view.*

class MyPost : AppCompatActivity() {
    private lateinit var firestoreAdapter: FirestorePagingAdapter<Discussion, ViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post)
        myPostBack.setOnClickListener {
            onBackPressed()
        }

        discussionRefresh.setOnRefreshListener {
            firestoreAdapter.refresh()
        }

        myPostRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        applyData()
    }

    private fun applyData() {
        val basequery = FirebaseFirestore.getInstance().collection("discussion")
            .whereEqualTo("ownerUid", FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy("uploadtime", Query.Direction.DESCENDING)

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(3)
            .setPageSize(10)
            .build()

        val options = FirestorePagingOptions.Builder<Discussion>()
            .setLifecycleOwner(this)
            .setQuery(basequery, config, Discussion::class.java)
            .build()

        firestoreAdapter = object :
            FirestorePagingAdapter<Discussion, ViewHolder>(
                options
            ) {
            override fun onCreateViewHolder(
                parent: ViewGroup, viewType: Int
            ): ViewHolder {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.my_post_list_item, parent, false)
                return ViewHolder(v)
            }

            override fun onBindViewHolder(
                holder: ViewHolder,
                position: Int,
                discussion: Discussion
            ) {
                holder.bind(discussion)
                holder.itemView.setOnClickListener {
                    startActivity(
                        Intent(
                            applicationContext,
                            MyPostDiscussionDetails::class.java
                        ).putExtra("discussiondetails", discussion)
                    )
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when (state) {
                    LoadingState.ERROR -> {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error loading discussions, please try again",
                            Snackbar.LENGTH_LONG
                        ).show()
                        discussionRefresh.isRefreshing = false
                    }
                    LoadingState.LOADING_INITIAL -> {
                        discussionRefresh.isRefreshing = true
                    }
                    LoadingState.LOADING_MORE -> {
                        discussionRefresh.isRefreshing = true
                    }
                    LoadingState.LOADED -> {
                        discussionRefresh.isRefreshing = false
                    }
                    LoadingState.FINISHED -> {
                        discussionRefresh.isRefreshing = false
                    }
                }
            }
        }
        myPostRecycler.adapter = firestoreAdapter
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.discussion_title as TextView
        private val description = itemView.discussion_desc as TextView
        private val ownerName = itemView.ownerName as TextView
        private val postDate = itemView.postDate as TextView
        private val status = itemView.statusText as TextView
        private val image = itemView.discussionImage as ImageView
        private val ownerImage = itemView.ownerImage as ImageView
        private val loading = itemView.image_loading_bar as ProgressBar

        @SuppressLint("CheckResult", "SimpleDateFormat")
        fun bind(discussion: Discussion) {
            ownerImage.clipToOutline = true
            title.text = discussion.title
            description.text = discussion.description
            postDate.text = discussion.uploadtime.toDate().toString()
            status.text = discussion.status
            FirebaseStorage.getInstance()
                .getReferenceFromUrl(discussion.imageUrl).downloadUrl.addOnSuccessListener {
                    if (it != null) {
                        val reqOp = RequestOptions()
                        reqOp.optionalFitCenter()
                        Glide.with(itemView).load(it).apply(reqOp).into(image)
                        loading.visibility = View.GONE
                    }
                }
            FirebaseFirestore.getInstance().collection("user").document(discussion.ownerUid).get()
                .addOnSuccessListener {
                    ownerName.text = it.get("name").toString()
                }
            FirebaseStorage.getInstance()
                .getReference("profilepicture/${discussion.ownerUid}").downloadUrl.addOnSuccessListener {
                    if (it != null) {
                        Glide.with(itemView).load(it).into(ownerImage)
                    }
                }
        }
    }
}