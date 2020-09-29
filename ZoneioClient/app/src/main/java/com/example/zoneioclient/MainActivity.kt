package com.example.zoneioclient

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.RuntimeExecutionException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private var username: String = "";
    private var authToken: String = "";
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

            val formBody: RequestBody = FormBody.Builder()
                .add("latitude", lr.locations.last().latitude.toString())
                .add("longitude", lr.locations.last().longitude.toString())
                .build()

            val request: Request = Request.Builder()
                .addHeader("x-access-token", authToken)
                .url("http://192.168.0.110:8000/api/coordinates/")
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    backgroundThreadShortToast(applicationContext, "An error happened: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = JSONObject(response.body!!.string())
                    if(response.isSuccessful) {
                        backgroundThreadShortToast(applicationContext, "Sent coordinates!")
                    } else {
                        backgroundThreadShortToast(applicationContext, body["error"] as String)
                        goToLoginActivity(username)
                    }
                }
            })
        }
    }

    private var isCapturing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        username = intent.getStringExtra("username")
        authToken = intent.getStringExtra("token")
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

    fun backgroundThreadShortToast(context: Context?, msg: String?) {
        if (context != null && msg != null) {
            Handler(Looper.getMainLooper()).post(Runnable {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun goToLoginActivity(username: String) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("username", username)
        }
        startActivity(intent)
    }

}
