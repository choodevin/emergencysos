package com.emergency.sosalert.locationTracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getDrawable
import com.emergency.sosalert.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_tracker_map.*


class TrackerMap : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var targetUid: String
    private val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 3
    private val targetMarker = MarkerOptions().position(LatLng(0.0, 0.0))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_map)

        trackingBackBtn.setOnClickListener {
            onBackPressed()
        }

        targetUid = intent.extras?.get("targetuid") as String

        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)
        supportFragmentManager.beginTransaction().replace(R.id.trackermap, mapFragment).commit()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            val location = LatLng(it.latitude, it.longitude)
            googleMap?.addMarker(
                MarkerOptions().position(location).title("You")
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_selfmarker))
            )
            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(location))
            googleMap?.animateCamera(CameraUpdateFactory.zoomTo(17F))
        }

        FirebaseDatabase.getInstance().reference.child("userlocation/$targetUid")
            .addValueEventListener(object : ValueEventListener {
                private val markerOpt = googleMap?.addMarker(targetMarker)
                override fun onDataChange(snapshot: DataSnapshot) {
                    val latlong = snapshot.getValue(LatLong::class.java)
                    val targetLocation = LatLng(
                        latlong?.latitude!!.toDouble(),
                        latlong.longitude.toDouble()
                    )
                    markerOpt?.position = targetLocation
                    markerOpt?.setIcon(
                        bitmapDescriptorFromVector(
                            applicationContext,
                            R.drawable.ic_trackingmarker
                        )
                    )
                    FirebaseFirestore.getInstance().collection("user").document(targetUid).get()
                        .addOnSuccessListener {
                            markerOpt?.title = it.get("name") as String
                        }
                    googleMap?.moveCamera(
                        CameraUpdateFactory.newLatLng(
                            targetLocation
                        )
                    )
                    googleMap?.animateCamera(CameraUpdateFactory.zoomTo(17F))
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
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