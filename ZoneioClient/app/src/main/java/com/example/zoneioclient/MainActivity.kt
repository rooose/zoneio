package com.example.zoneioclient

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.RuntimeExecutionException
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val requestHandler: RequestHandler = RequestHandler()
    private var username: String = "";
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val PERMISSION_ID = 42
    private val reqSetting: LocationRequest = LocationRequest.create().apply {
        fastestInterval = 1000
        interval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 1.0f
    }

    private val REQUEST_CHECK_STATE = 12300 // any suitable ID
    private val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(reqSetting)

    private val locationUpdates = object : LocationCallback() {
        override fun onLocationResult(lr: LocationResult) {
            Log.e("LOG", lr.toString())
            Log.e("LOG", "Newest Location: " + lr.locations.last())

            val parameters = mapOf("user_id" to username, "x" to lr.locations.last().latitude, "y" to lr.locations.last().longitude, "timestamp" to getDateTime())
            val request = mapOf("endpoint" to "/coordinates", "parameters" to parameters)
            val response = requestHandler.execute(request)
        }
    }

    private var isCapturing = false

    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        username = intent.getStringExtra("username")
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build()).addOnCompleteListener { task ->
            try {
                val state: LocationSettingsStates = task.result.locationSettingsStates
                Log.e(
                    "LOG", "LocationSettings: \n" +
                            " GPS present: ${state.isGpsPresent} \n" +
                            " GPS usable: ${state.isGpsUsable} \n" +
                            " Location present: " +
                            "${state.isLocationPresent} \n" +
                            " Location usable: " +
                            "${state.isLocationUsable} \n" +
                            " Network Location present: " +
                            "${state.isNetworkLocationPresent} \n" +
                            " Network Location usable: " +
                            "${state.isNetworkLocationUsable} \n"
                )
            } catch (e: RuntimeExecutionException) {
                if (e.cause is ResolvableApiException)
                    (e.cause as ResolvableApiException).startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_STATE
                    )
            }
        }

        button.setOnClickListener { toggleAsyncCapture() }

    }

    private fun checkPermission(vararg perm: String): Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(this, it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if (perm.toList().any {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                }
            ) {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Permission")
                    .setMessage("Permission needed!")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this, perm, PERMISSION_ID
                        )
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .create()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }

    private fun captureLocationSync() {
        if (checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener(
                this
            ) { location: Location? ->
                // Got last known location. In some rare
                // situations this can be null.
                if (location == null) {
                    // TODO, handle it
                } else location.apply {
                    // Handle location object
                    Log.e("LOG", location.toString())
                }
            }
        }
    }

    private fun startLocationCaptureAsync() {
        if (checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            fusedLocationClient?.requestLocationUpdates(
                reqSetting,
                locationUpdates,
                null /* Looper */
            )
        }

    }

    private fun stopLocationCaptureAsync() {
        fusedLocationClient?.removeLocationUpdates(locationUpdates)
    }

    private fun toggleAsyncCapture() {
        isCapturing = !isCapturing

        if (isCapturing) {
            startLocationCaptureAsync()
            button.setBackgroundColor(Color.GREEN)
            button.text = "CAPTURING..."
        } else {
            stopLocationCaptureAsync()
            button.setBackgroundColor(Color.LTGRAY)
            button.text = "CLICK TO START CAPTURE"
        }
    }
}
