package com.emergency.sosalert.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.emergency.sosalert.R
import com.emergency.sosalert.discussion.CreateDiscussion
import com.emergency.sosalert.discussion.Discussion
import com.emergency.sosalert.discussion.DiscussionDetails
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.discussion_list_item.view.*
import kotlinx.android.synthetic.main.fragment_discussion.*

class AllDiscussion : Fragment() {

    private lateinit var firestoreAdapter: FirestorePagingAdapter<Discussion, ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        discussionRefresh.setOnRefreshListener {
            firestoreAdapter.refresh()
        }

        discussionRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        applyData()
    }

    override fun onResume() {
        super.onResume()
        firestoreAdapter.refresh()
    }

    private fun applyData() {
        val basequery =
            FirebaseFirestore.getInstance().collection("discussion")
                .whereEqualTo("status", "pending")
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
            FirestorePagingAdapter<Discussion, ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup, viewType: Int
            ): ViewHolder {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.discussion_list_item, parent, false)
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
                            context,
                            AdminDiscussionDetails::class.java
                        ).putExtra("discussiondetails", discussion)
                    )
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when (state) {
                    LoadingState.ERROR -> {
                        Snackbar.make(
                            view!!,
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
        discussionRecycler.adapter = firestoreAdapter
    }

    override fun onStart() {
        super.onStart()
        firestoreAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        firestoreAdapter.stopListening()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.discussion_title
        val image = itemView.discussionImage
        val loading = itemView.image_loading_bar
        val postDate = itemView.postDate
        val ownerName = itemView.ownerName
        val ownerImage = itemView.ownerImage

        @SuppressLint("CheckResult")
        fun bind(discussion: Discussion) {
            title.text = discussion.title
            postDate.text = discussion.uploadtime.toDate().toString()
            ownerImage.clipToOutline = true
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