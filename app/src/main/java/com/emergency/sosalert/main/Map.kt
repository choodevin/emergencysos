package com.emergency.sosalert.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emergency.sosalert.R
import com.emergency.sosalert.discussion.Discussion
import com.emergency.sosalert.discussion.DiscussionDetails
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_map.*

class Map : Fragment(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 3
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction().replace(R.id.locationPreview, mapFragment).commit()

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isCompassEnabled = true

        applyMarker(googleMap)

        refreshButton.setOnClickListener {
            googleMap?.clear()
            applyMarker(googleMap)
        }
    }

    private fun applyMarker(googleMap: GoogleMap?) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                val location = LatLng(it.latitude, it.longitude)
                googleMap?.addMarker(
                    MarkerOptions().position(location).title("You")
                        .icon(
                            bitmapDescriptorFromVector(
                                requireContext(),
                                R.drawable.ic_selfmarker
                            )
                        )
                )
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(location))
                googleMap?.animateCamera(CameraUpdateFactory.zoomTo(15F))
            } else {
                Toast.makeText(context, "Failed to get current location", Toast.LENGTH_LONG).show()
            }

        }

        FirebaseFirestore.getInstance().collection("discussion")
            .whereEqualTo("status", "approved").get()
            .addOnSuccessListener { allDisc ->
                val discList = ArrayList<Discussion>()
                val markerList = ArrayList<MarkerOptions>()

                for (disc in allDisc) {
                    val discussion: Discussion = disc.toObject(Discussion::class.java)
                    val marker = MarkerOptions()
                        .position(
                            LatLng(
                                discussion.latitude.toDouble(),
                                discussion.longitude.toDouble()
                            )
                        )
                        .title(discussion.title)
                        .icon(
                            bitmapDescriptorFromVector(
                                requireContext(),
                                R.drawable.ic_dangermarker
                            )
                        )
                    discList.add(discussion)
                    markerList.add(marker)
                    googleMap?.addMarker(marker)
                }

                googleMap?.setOnInfoWindowClickListener { mappedMarker ->
                    for ((position, marker) in markerList.withIndex()) {
                        if (marker.position == mappedMarker.position) {
                            startActivity(
                                Intent(
                                    context,
                                    DiscussionDetails::class.java
                                ).putExtra("discussiondetails", discList[position])
                            )
                            break
                        }
                    }
                }
            }

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}