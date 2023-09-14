package com.app.voip

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.voip.databinding.ActivityMainBinding
import com.app.voip.mvvm.MainActivityViewModel
import io.agora.rtc2.RtcEngine




class MainActivity : AppCompatActivity() {

    private var className = MainActivity::class.java.simpleName
    //UI Binding class
    private lateinit var binding: ActivityMainBinding
    //View model class
    private lateinit var viewModel: MainActivityViewModel
    //Permission request ID
    private val PERMISSION_REQ_ID = 22
    //Permission Required for audio transmission on API level 23 and above
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        android.Manifest.permission.RECORD_AUDIO
    )

    // Fill the App ID of your project generated on Agora Console.
    private val appId = ""
    private val appCertificate = ""

    // Fill the channel name.
    private val channelName = "voip_public_room"

    // Fill the temp token generated on Agora Console.
    private val token: String? = null

    // An integer that identifies the local user.
    private val uid = 0

    // Track the status of your connection
    private val isJoined = false
    private val isMuted = false

    private val genIntID = 0

    // Agora engine instance
    private val agoraEngine: RtcEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize view model class with default values
        viewModel = MainActivityViewModel("JOIN NOW",
            "MUTE CALL", "NetworkStatus")
        binding.model = viewModel //now set the view model to UI binding class

        //Initialize click listener for buttons
        binding.amJoinLeaveBtn.setOnClickListener {
            //TODO
        }
        binding.amMuteUnmuteBtn.setOnClickListener {
            //TODO
        }
    }
}