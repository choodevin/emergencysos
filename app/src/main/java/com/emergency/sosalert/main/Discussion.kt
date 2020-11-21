package com.emergency.sosalert.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.dialog_filter.view.*
import kotlinx.android.synthetic.main.discussion_list_item.view.*
import kotlinx.android.synthetic.main.fragment_discussion.*
import java.text.DateFormatSymbols
import java.util.*

@Suppress("PrivatePropertyName")
class Discussion : Fragment() {

    private lateinit var firestoreAdapter: FirestorePagingAdapter<Discussion, ViewHolder>
    private val selectedStartCal = Calendar.getInstance()
    private val selectedEndCal = Calendar.getInstance()
    private val ENABLE = 1
    private val DISABLE = 0

    enum class ApplyMode {
        SEARCH,
        TODAY,
        DEFAULT, REVERSE_DEFAULT,
        ALPHABETIC, REVERSE_ALPHABETIC,
        COMMENT_COUNT, REVERSE_COMMENT_COUNT,
        MONTH, REVERSE_MONTH,
        WEEK, REVERSE_WEEK,
        DATE_RANGE, REVERSE_DATE_RANGE
    }

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

        applyData(ApplyMode.DEFAULT)

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
                    applyData(ApplyMode.SEARCH)
                }
            })
        }

        filterButton.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_filter, null)
            val sortSpinner = dialogView.sortByList
            val orderSpinner = dialogView.orderList
            val checkToday = dialogView.checkToday
            val checkLastMonth = dialogView.checkLastMonth
            val checkLastWeek = dialogView.checkLastWeek
            val orderTipsText = dialogView.orderTipsText
            val checkCustom = dialogView.checkCustom
            val selectDateStart = dialogView.selectDateStart
            val selectDateEnd = dialogView.selectDateEnd

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

            checkToday.setOnCheckedChangeListener { _, b ->
                if (b) {
                    checkLastMonth.isChecked = false
                    checkLastWeek.isChecked = false
                    orderSpinner.isEnabled = false
                    checkCustom.isChecked = false
                } else {
                    if (!checkLastMonth.isChecked && !checkLastWeek.isChecked) {
                        orderSpinner.isEnabled = true
                    }
                }
            }
            checkLastMonth.setOnCheckedChangeListener { _, b ->
                if (b) {
                    checkToday.isChecked = false
                    checkLastWeek.isChecked = false
                    checkCustom.isChecked = false
                    orderSpinner.isEnabled = true
                    selectDateEnd.visibility = View.GONE
                    selectDateStart.visibility = View.GONE
                } else {
                    if (!checkToday.isChecked && !checkLastWeek.isChecked) {
                        orderSpinner.isEnabled = true
                    }
                }
            }
            checkLastWeek.setOnCheckedChangeListener { _, b ->
                if (b) {
                    checkLastMonth.isChecked = false
                    checkToday.isChecked = false
                    checkCustom.isChecked = false
                    orderSpinner.isEnabled = true
                    selectDateEnd.visibility = View.GONE
                    selectDateStart.visibility = View.GONE
                } else {
                    if (!checkLastMonth.isChecked && !checkToday.isChecked) {
                        orderSpinner.isEnabled = true
                    }
                }
            }
            checkCustom.setOnCheckedChangeListener { _, b ->
                if (b) {
                    checkLastMonth.isChecked = false
                    checkToday.isChecked = false
                    checkLastWeek.isChecked = false
                    orderSpinner.isEnabled = true
                    selectDateEnd.visibility = View.VISIBLE
                    selectDateStart.visibility = View.VISIBLE
                } else {
                    selectDateStart.visibility = View.GONE
                    selectDateEnd.visibility = View.GONE
                    if (!checkLastMonth.isChecked && !checkToday.isChecked) {
                        orderSpinner.isEnabled = true
                    }
                }
            }

            selectDateStart.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    requireContext(),
                    R.style.DatePickerDialogTheme,
                    { _, selectYear, selectMonth, selectDay ->
                        selectedStartCal.set(selectYear, selectMonth, selectDay, 0, 0, 0)
                        val tempStr =
                            "$selectDay ${DateFormatSymbols().months[selectMonth]} $selectYear"
                        selectDateStart.text = tempStr
                    },
                    year,
                    month,
                    day
                )
                dpd.show()
            }

            selectDateEnd.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    requireContext(),
                    R.style.DatePickerDialogTheme,
                    { _, selectYear, selectMonth, selectDay ->
                        selectedEndCal.set(selectYear, selectMonth, selectDay, 23, 59, 59)
                        val tempStr =
                            "$selectDay ${DateFormatSymbols().months[selectMonth]} $selectYear"
                        selectDateEnd.text = tempStr
                    },
                    year,
                    month,
                    day
                )
                dpd.show()
            }

            sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 == 0) {
                        dateExtra(ENABLE)
                    } else {
                        dateExtra(DISABLE)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                private fun dateExtra(mode: Int) {
                    if (mode == ENABLE) {
                        checkToday.visibility = View.VISIBLE
                        checkLastMonth.visibility = View.VISIBLE
                        checkLastWeek.visibility = View.VISIBLE
                        checkCustom.visibility = View.VISIBLE
                        orderTipsText.visibility = View.VISIBLE
                    } else if (mode == DISABLE) {
                        checkToday.visibility = View.GONE
                        checkLastMonth.visibility = View.GONE
                        checkLastWeek.visibility = View.GONE
                        checkCustom.visibility = View.GONE
                        orderTipsText.visibility = View.GONE
                        selectDateStart.visibility = View.GONE
                        selectDateEnd.visibility = View.GONE
                    }
                }
            }

            val dialogBuilder =
                AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton("Search") { _, _ ->
                        when (sortSpinner.selectedItemPosition) {
                            0 -> {
                                if (checkToday.isChecked || checkLastMonth.isChecked || checkLastWeek.isChecked || checkCustom.isChecked) {
                                    when {
                                        checkToday.isChecked -> {
                                            applyData(ApplyMode.TODAY)
                                        }
                                        checkLastMonth.isChecked -> {
                                            if (orderSpinner.selectedItemPosition == 0) {
                                                applyData(ApplyMode.REVERSE_MONTH)
                                            } else {
                                                applyData(ApplyMode.MONTH)
                                            }
                                        }
                                        checkLastWeek.isChecked -> {
                                            if (orderSpinner.selectedItemPosition == 0) {
                                                applyData(ApplyMode.REVERSE_WEEK)
                                            } else {
                                                applyData(ApplyMode.WEEK)
                                            }
                                        }
                                        checkCustom.isChecked -> {
                                            if (selectedEndCal.before(selectedStartCal)) {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Invalid date range",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                applyData(ApplyMode.DEFAULT)
                                            } else {
                                                if (orderSpinner.selectedItemPosition == 0) {
                                                    applyData(ApplyMode.REVERSE_DATE_RANGE)
                                                } else {
                                                    applyData(ApplyMode.DATE_RANGE)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (orderSpinner.selectedItemPosition == 0) {
                                        applyData(ApplyMode.REVERSE_DEFAULT)
                                    } else {
                                        applyData(ApplyMode.DEFAULT)
                                    }
                                }
                            }
                            1 -> {
                                if (orderSpinner.selectedItemPosition == 0) {
                                    applyData(ApplyMode.ALPHABETIC)
                                } else {
                                    applyData(ApplyMode.REVERSE_ALPHABETIC)
                                }
                            }
                            2 -> {
                                if (orderSpinner.selectedItemPosition == 0) {
                                    applyData(ApplyMode.COMMENT_COUNT)
                                } else {
                                    applyData(ApplyMode.REVERSE_COMMENT_COUNT)
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
            applyData(ApplyMode.DEFAULT)
        }
    }

    private fun applyData(mode: ApplyMode) {
        lateinit var baseQuery: Query
        when (mode) {
            ApplyMode.DEFAULT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("uploadtime", Query.Direction.DESCENDING)
            }
            ApplyMode.SEARCH -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("title")
                    .startAt("${searchBox.text}")
                    .endAt("${searchBox.text}\uf8ff")
            }
            ApplyMode.REVERSE_DEFAULT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("uploadtime", Query.Direction.ASCENDING)
            }
            ApplyMode.ALPHABETIC -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("title", Query.Direction.ASCENDING)
            }
            ApplyMode.REVERSE_ALPHABETIC -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("title", Query.Direction.DESCENDING)
            }
            ApplyMode.COMMENT_COUNT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("commentcount", Query.Direction.ASCENDING)
            }
            ApplyMode.REVERSE_COMMENT_COUNT -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .orderBy("commentcount", Query.Direction.DESCENDING)
            }
            ApplyMode.TODAY -> {
                val baseCal = Calendar.getInstance()
                val endDate = baseCal.time
                val tempDate = Calendar.getInstance()
                tempDate.set(
                    baseCal.get(Calendar.YEAR),
                    baseCal.get(Calendar.MONTH),
                    baseCal.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0
                )
                val startDate = tempDate.time
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .whereLessThanOrEqualTo("uploadtime", endDate)
                    .whereGreaterThanOrEqualTo("uploadtime", startDate)
            }
            ApplyMode.MONTH -> {
                val baseCal = Calendar.getInstance()
                val endDate = baseCal.time
                val tempDate = Calendar.getInstance()
                tempDate.set(
                    baseCal.get(Calendar.YEAR),
                    baseCal.get(Calendar.MONTH),
                    baseCal.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0
                )
                tempDate.add(Calendar.DATE, -30)
                val startDate = tempDate.time
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .whereLessThanOrEqualTo("uploadtime", endDate)
                    .whereGreaterThanOrEqualTo("uploadtime", startDate)
                    .orderBy("uploadtime", Query.Direction.DESCENDING)
            }
            ApplyMode.REVERSE_MONTH -> {
                val baseCal = Calendar.getInstance()
                val endDate = baseCal.time
                val tempDate = Calendar.getInstance()
                tempDate.set(
                    baseCal.get(Calendar.YEAR),
                    baseCal.get(Calendar.MONTH),
                    baseCal.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0
                )
                tempDate.add(Calendar.DATE, -30)
                val startDate = tempDate.time
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .whereLessThanOrEqualTo("uploadtime", endDate)
                    .whereGreaterThanOrEqualTo("uploadtime", startDate)
                    .orderBy("uploadtime", Query.Direction.ASCENDING)
            }
            ApplyMode.WEEK -> {
                val baseCal = Calendar.getInstance()
                val endDate = baseCal.time
                val tempDate = Calendar.getInstance()
                tempDate.set(
                    baseCal.get(Calendar.YEAR),
                    baseCal.get(Calendar.MONTH),
                    baseCal.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0
                )
                tempDate.add(Calendar.DATE, -7)
                val startDate = tempDate.time
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .whereLessThanOrEqualTo("uploadtime", endDate)
                    .whereGreaterThanOrEqualTo("uploadtime", startDate)
                    .orderBy("uploadtime", Query.Direction.DESCENDING)
            }
            ApplyMode.REVERSE_WEEK -> {
                val baseCal = Calendar.getInstance()
                val endDate = baseCal.time
                val tempDate = Calendar.getInstance()
                tempDate.set(
                    baseCal.get(Calendar.YEAR),
                    baseCal.get(Calendar.MONTH),
                    baseCal.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0
                )
                tempDate.add(Calendar.DATE, -7)
                val startDate = tempDate.time
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .whereLessThanOrEqualTo("uploadtime", endDate)
                    .whereGreaterThanOrEqualTo("uploadtime", startDate)
                    .orderBy("uploadtime", Query.Direction.ASCENDING)
            }
            ApplyMode.DATE_RANGE -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .whereLessThanOrEqualTo("uploadtime", selectedEndCal.time)
                    .whereGreaterThanOrEqualTo("uploadtime", selectedStartCal.time)
                    .orderBy("uploadtime", Query.Direction.DESCENDING)
            }
            ApplyMode.REVERSE_DATE_RANGE -> {
                baseQuery = FirebaseFirestore.getInstance().collection("discussion")
                    .whereEqualTo("status", "approved")
                    .whereLessThanOrEqualTo("uploadtime", selectedEndCal.time)
                    .whereGreaterThanOrEqualTo("uploadtime", selectedStartCal.time)
                    .orderBy("uploadtime", Query.Direction.ASCENDING)
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

