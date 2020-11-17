package com.emergency.sosalert.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
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
import kotlinx.android.synthetic.main.activity_create_discussion.*
import kotlinx.android.synthetic.main.dialog_filter.view.*
import kotlinx.android.synthetic.main.discussion_list_item.view.*
import kotlinx.android.synthetic.main.fragment_discussion.*

class Discussion : Fragment() {

    private lateinit var firestoreAdapter: FirestorePagingAdapter<Discussion, ViewHolder>
    private val REVERSE_COMMENTCOUNT = 6
    private val COMMENTCOUNT = 5
    private val REVERSE_ALPHABETIC = 4
    private val ALPHABETIC = 3
    private val REVERSE_DEFAULT = 2
    private val SEARCH = 1
    private val DEFAULT = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addDiscussion.setOnClickListener {
            startActivity(Intent(context, CreateDiscussion::class.java))
        }

        discussionRefresh.setOnRefreshListener {
            firestoreAdapter.refresh()
        }

        discussionRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        applyData(DEFAULT)

        searchButton.setOnClickListener {
            searchBox.visibility = View.VISIBLE
            closeSearch.visibility = View.VISIBLE
            filterButton.hide()
            discussionActionBarText.visibility = View.GONE
            addDiscussion.visibility = View.GONE
            searchButton.visibility = View.GONE

            searchBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    applyData(SEARCH)
                }
            })
        }

        filterButton.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_filter, null)
            val sortSpinner = dialogView.sortByList
            val orderSpinner = dialogView.orderList

            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sortby,
                android.R.layout.simple_spinner_dropdown_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sortSpinner.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.order,
                android.R.layout.simple_spinner_dropdown_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                orderSpinner.adapter = adapter
            }

            val dialogBuilder =
                AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton("Search") { _, _ ->
                        when (sortSpinner.selectedItemPosition) {
                            0 -> {
                                if (orderSpinner.selectedItemPosition == 0) {
                                    applyData(REVERSE_DEFAULT)
                                } else {
                                    applyData(DEFAULT)
                                }
                            }
                            1 -> {
                                if (orderSpinner.selectedItemPosition == 0) {
                                    applyData(ALPHABETIC)
                                } else {
                                    applyData(REVERSE_ALPHABETIC)
                                }
                            }
                            2 -> {
                                if (orderSpinner.selectedItemPosition == 0) {
                                    applyData(COMMENTCOUNT)
                                } else {
                                    applyData(REVERSE_COMMENTCOUNT)
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                    }
            val dialog = dialogBuilder.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimaryDark
                    )
                )
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimaryDark
                    )
                )
            }

            dialog.show()
        }

        closeSearch.setOnClickListener {
            closeKeyboard()
            searchBox.visibility = View.GONE
            closeSearch.visibility = View.INVISIBLE
            filterButton.show()
            discussionActionBarText.visibility = View.VISIBLE
            addDiscussion.visibility = View.VISIBLE
            searchButton.visibility = View.VISIBLE
            applyData(DEFAULT)
        }
    }

    private fun applyData(mode: Int) {
        lateinit var baseQuery: Query
        when (mode) {
            DEFAULT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("uploadtime", Query.Direction.DESCENDING)
            }
            SEARCH -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("title")
                    .startAt("${searchBox.text}")
                    .endAt("${searchBox.text}\uf8ff")
            }
            REVERSE_DEFAULT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("uploadtime", Query.Direction.ASCENDING)
            }
            ALPHABETIC -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("title", Query.Direction.ASCENDING)
            }
            REVERSE_ALPHABETIC -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("title", Query.Direction.DESCENDING)
            }
            COMMENTCOUNT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("commentcount", Query.Direction.ASCENDING)
            }
            REVERSE_COMMENTCOUNT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("commentcount", Query.Direction.DESCENDING)
            }
        }

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(3)
            .setPageSize(10)
            .build()

        val options = FirestorePagingOptions.Builder<Discussion>()
            .setLifecycleOwner(this)
            .setQuery(baseQuery, config, Discussion::class.java)
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
                            DiscussionDetails::class.java
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

        if (firestoreAdapter.itemCount == 0) {
            noDiscussionText.visibility = View.VISIBLE
        } else {
            noDiscussionText.visibility = View.GONE
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

    private fun closeKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.discussion_title as TextView
        private val ownerName = itemView.ownerName as TextView
        private val postDate = itemView.postDate as TextView
        private val image = itemView.discussionImage as ImageView
        private val ownerImage = itemView.ownerImage as ImageView
        private val loading = itemView.image_loading_bar as ProgressBar

        @SuppressLint("CheckResult", "SimpleDateFormat")
        fun bind(discussion: Discussion) {
            ownerImage.clipToOutline = true
            title.text = discussion.title
            postDate.text = discussion.uploadtime.toDate().toString()
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

