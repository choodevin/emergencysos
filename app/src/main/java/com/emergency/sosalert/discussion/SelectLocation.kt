package com.emergency.sosalert.discussion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.emergency.sosalert.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_select_location.*

class SelectLocation : AppCompatActivity(), OnMapReadyCallback {
    private val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var selectedLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.locationPreview) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
            val currentLocation = LatLng(it.latitude, it.longitude)
            googleMap?.addMarker(MarkerOptions().position(currentLocation).title("You"))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
            googleMap?.animateCamera(CameraUpdateFactory.zoomTo(17F))
        }

        googleMap?.setOnCameraIdleListener {
            selectedLocation = googleMap.cameraPosition.target
            val toDisplayLatLng = "${selectedLocation.latitude},${selectedLocation.longitude}"
            confirmLocationButton.text = toDisplayLatLng

            confirmLocationButton.setOnClickListener {
                val resultIntent = intent.putExtra("selectedLocation", toDisplayLatLng)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

}