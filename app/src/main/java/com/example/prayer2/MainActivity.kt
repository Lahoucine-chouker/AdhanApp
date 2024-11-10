package com.example.prayer2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var adapter: PrayerAdapter
    lateinit var listView: ListView
    lateinit var mosqueLogo: ImageView
    lateinit var titleImage: ImageView
    lateinit var titleText: TextView
    lateinit var compassButton: Button


    val prayerTimes = listOf(
        "Fajr: 6:24 AM",
        "Dhuhr: 13:16 PM",
        "Asr: 16:06 PM",
        "Maghrib: 18:31 PM",
        "Isha: 19:48 PM"
    )

    lateinit var mediaPlayer: MediaPlayer

    // Map to track if Adhan has been played for each prayer time
    val prayerTimeFlags = mutableMapOf(
        "Fajr" to false,
        "Dhuhr" to false,
        "Asr" to false,
        "Maghrib" to false,
        "Isha" to false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        mosqueLogo = findViewById(R.id.mosque_logo)
        titleImage = findViewById(R.id.title_image)
        titleText = findViewById(R.id.title_text)
        listView = findViewById(R.id.prayer_list)
        compassButton = findViewById(R.id.compass_button)

        // Set an onClickListener to open CompassActivity
        compassButton.setOnClickListener {
            val intent = Intent(this, CompassActivity::class.java)
            startActivity(intent)
        }

        // Set up adapter and list view
        adapter = PrayerAdapter(this, prayerTimes)
        listView.adapter = adapter

        // Set mosque logo and title
        mosqueLogo.setImageResource(R.drawable.logo)
        titleImage.setImageResource(R.drawable.mosque)
        titleText.text = "Prayer Times App"

        // Initialize the MediaPlayer
        mediaPlayer = MediaPlayer()

        // Checking time every second (faster checks)
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                checkPrayerTime()  // Check prayer time every second
                handler.postDelayed(this, 1000)  // Check every second
            }
        }
        handler.post(runnable)
    }

    // Function to check if it's prayer time and handle Adhan
    fun checkPrayerTime() {
        val currentPrayerTime = getCurrentTime()
        if (currentPrayerTime.isNotEmpty() && !prayerTimeFlags[currentPrayerTime]!!) {
            playAdhan(currentPrayerTime)  // Play the Adhan for the current prayer time
            prayerTimeFlags[currentPrayerTime] = true  // Set flag to true after playing Adhan
        }
    }

    // Function to play the call to prayer sound
    fun playAdhan(prayer: String) {
        if (mediaPlayer.isPlaying) {
            return  // Don't play the Adhan if it's already playing
        }

        // Set the URL to the Adhan sound (song)
        val adhanUrl = "https://www.islamcan.com/audio/adhan/azan10.mp3"

        try {
            mediaPlayer.reset() // Reset the player to clear any previous state
            mediaPlayer.setDataSource(adhanUrl)  // Set the data source to the online audio URL

            // Prepare and start the media player
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()  // Start the audio once it's prepared
                Log.d("PrayerTimes", "Adhan started playing for $prayer")  // Log confirmation
                // Show confirmation to the user that Adhan has started
                Toast.makeText(this, "Adhan for $prayer started", Toast.LENGTH_SHORT).show()
            }

            // Prepare the media player asynchronously
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error, maybe show a Toast message to the user
            Toast.makeText(this, "Error playing Adhan sound", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to get the current time
    fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Format the current time as HH:mm:ss
        val currentTimeString = String.format("%02d:%02d:%02d", hour, minute, second)
        Log.d("PrayerTimes", "Current time: $currentTimeString")  // Log current time

        // Prayer times with a small window of +-30 seconds around the prayer time
        val prayerTimesWithBuffer = listOf(
            "06:24:00" to "Fajr",
            "13:16:00" to "Dhuhr",
            "16:06:00" to "Asr",
            "18:31:00" to "Maghrib",
            "19:48:00" to "Isha"
        )

        // Allow a 30-second window before and after the prayer time
        for ((prayerTime, prayerLabel) in prayerTimesWithBuffer) {
            val prayerTimeParts = prayerTime.split(":")
            val prayerHour = prayerTimeParts[0].toInt()
            val prayerMinute = prayerTimeParts[1].toInt()
            val prayerSecond = prayerTimeParts[2].toInt()

            // Check if current time is within the prayer time window (+- 30 seconds)
            val timeDifference = getTimeDifferenceInSeconds(hour, minute, second, prayerHour, prayerMinute, prayerSecond)
            Log.d("PrayerTimes", "Time difference for $prayerLabel: $timeDifference")  // Log time difference

            if (timeDifference in -30..30) {  // Allow up to 30 seconds before/after the prayer time
                Log.d("PrayerTimes", "Prayer time detected: $prayerLabel")
                return prayerLabel  // Return the prayer label once prayer time is detected
            }
        }

        return ""  // No prayer time detected
    }

    // Function to calculate the difference in seconds between current time and prayer time
    fun getTimeDifferenceInSeconds(
        currentHour: Int, currentMinute: Int, currentSecond: Int,
        prayerHour: Int, prayerMinute: Int, prayerSecond: Int
    ): Int {
        val currentTotalSeconds = currentHour * 3600 + currentMinute * 60 + currentSecond
        val prayerTotalSeconds = prayerHour * 3600 + prayerMinute * 60 + prayerSecond
        return currentTotalSeconds - prayerTotalSeconds
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()  // Release the media player when the activity is destroyed
        }
    }
}
