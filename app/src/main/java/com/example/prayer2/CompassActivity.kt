package com.example.prayer2

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var compassImage: ImageView? = null
    private var qiblahDirectionTextView: TextView? = null
    private var currentAzimuth: Float = 0f
    private var qiblahBearing: Float = 0f

    // Sensor values for magnetic field and accelerometer
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        // Initialize UI components and sensor manager
        compassImage = findViewById(R.id.compass_image)
        qiblahDirectionTextView = findViewById(R.id.qiblah_direction_text)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLocationAndCalculateQiblah()
    }

    override fun onResume() {
        super.onResume()
        // Register sensor listeners for accelerometer and magnetic field
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
    }

    private fun getLocationAndCalculateQiblah() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                qiblahBearing = calculateQiblahDirection(location.latitude, location.longitude)
                qiblahDirectionTextView?.text = "Qiblah Direction: $qiblahBearing°"
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Calculate Qiblah direction (bearing) from current location to Mecca
    private fun calculateQiblahDirection(userLat: Double, userLon: Double): Float {
        val kaabaLat = 21.4225  // Latitude of the Kaaba
        val kaabaLon = 39.8262  // Longitude of the Kaaba

        val userLatRad = Math.toRadians(userLat)
        val userLonRad = Math.toRadians(userLon)
        val kaabaLatRad = Math.toRadians(kaabaLat)
        val kaabaLonRad = Math.toRadians(kaabaLon)

        val deltaLon = kaabaLonRad - userLonRad
        val y = sin(deltaLon) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLon)
        return (Math.toDegrees(atan2(y, x)).toFloat() + 360) % 360
    }

    // Sensor data processing
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values
        }

        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)

            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                // Azimuth in degrees
                currentAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                // Calculate rotation to point to the Qiblah
                val rotation = currentAzimuth - qiblahBearing
                compassImage?.rotation = -rotation
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not needed for this implementation
    }

    // Handle the result of the location permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, fetch location
            getLocationAndCalculateQiblah()
        } else {
            Toast.makeText(this, "Permission denied. Unable to access location.", Toast.LENGTH_SHORT).show()
        }
    }
}
