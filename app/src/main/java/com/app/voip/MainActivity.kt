package com.app.voip

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.voip.agora.RtcTokenBuilder2Generator
import com.app.voip.databinding.ActivityMainBinding
import com.app.voip.mvvm.MainActivityViewModel
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.internal.LastmileProbeConfig
import java.util.Random


class MainActivity : AppCompatActivity() {

    private var className = MainActivity::class.java.simpleName
    //UI Binding class
    private lateinit var binding: ActivityMainBinding
    //View model class
    private lateinit var viewModel: MainActivityViewModel
    //Permission request ID
    private val permissionReqId = 22
    //Permission Required for audio transmission on API level 23 and above
    private val requestedPermission = arrayOf(
        android.Manifest.permission.RECORD_AUDIO
    )

    // Fill the App ID of your project generated on Agora Console.
    private var appId = ""
    private var appCertificate = ""

    // Fill the channel name.
    private val channelName = "voip_public_room"

    // Fill the temp token generated on Agora Console.
    private var token: String? = null

    // An integer that identifies the local user.
    private val uid = 0

    // Track the status of your connection
    private var isJoined = false
    private var isMuted = false

    private var genIntID = 0

    // Agora engine instance
    private var agoraEngine: RtcEngine? = null

    //Text-Watcher
    private var channelNameTextWatcher: TextWatcher? = null

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            viewModel.title = "Remote user joined: $uid"
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            // Successfully joined a channel
            isJoined = true
            showMessage("Joined Channel $channel")
            viewModel.title = "Waiting for a remote user to join"
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            // Listen for remote users leaving the channel
            showMessage("Remote user offline $uid $reason")
            viewModel.title = "Waiting for a remote user to join"
        }

        override fun onLeaveChannel(stats: RtcStats) {
            // Listen for the local user leaving the channel
            viewModel.title = "Press the button to join a channel"
            isJoined = false
        }

        override fun onLastmileQuality(quality: Int) {
            runOnUiThread { updateNetworkStatus(quality) }
        }

        override fun onLastmileProbeResult(result: LastmileProbeResult) {
            agoraEngine!!.stopLastmileProbeTest()
            // The result object contains the detailed test results that help you
            // manage call quality. For example, the down-link jitter"
            showMessage("Downlink jitter: " + result.downlinkReport.jitter)
        }

        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            // Use DownLink NetQuality to update the network status
            runOnUiThread { updateNetworkStatus(rxQuality) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        //remove channel name input text listener to prevent memory leaks
        binding.amJoinChannelEditText.removeTextChangedListener(channelNameTextWatcher)

        // Destroy the engine in a sub-thread to avoid congestion
        Thread {
            agoraEngine!!.leaveChannel()
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get the agora appId and appCertificate from BuildConfig
        appId = BuildConfig.agoraAppID
        appCertificate = BuildConfig.agoraAppCertificate

        //Print the appId and appCertificate in logCat
        Log.d(className, "AppID: $appId AppCertificate: $appCertificate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize view model class with default values
        viewModel = MainActivityViewModel("Welcome", "JOIN NOW",
            "MUTE CALL", "NetworkStatus", false, false)
        binding.model = viewModel //now set the view model to UI binding class

        //Generate random ID so that we can use it later for creating token
        val rand = Random()
        genIntID = rand.nextInt(5000)
        Log.d(className, "Random ID: $genIntID")

        //Initialize click listener for buttons
        binding.amJoinLeaveBtn.setOnClickListener {
            //First check if we have audio permissions or not.
            // If all the permissions are granted, initialize the RtcEngine object and join a channel.
            if (!checkSelfPermission()) {
                //We don't have audio permissions so request it now.
                ActivityCompat.requestPermissions(
                    this@MainActivity, requestedPermission, permissionReqId)
            } else {
                //Permissions are granted...
                //Generate a unique ID for user
                joinLeaveChannel()
            }
        }
        binding.amMuteUnmuteBtn.setOnClickListener {
            if ( agoraEngine != null ) {
                if (isMuted) {
                    //audio is muted so un-mute it
                    agoraEngine!!.muteLocalAudioStream(false);
                    isMuted = false;
                    viewModel.muteUnMuteButtonText = "MUTE";
                } else {
                    //audio is un-muted so mute it
                    agoraEngine!!.muteLocalAudioStream(true);
                    isMuted = true;
                    viewModel.muteUnMuteButtonText = "UN-MUTE";
                }
            }
        }

        //Channel name Edit Text Change Listener
        channelNameTextWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int, i2: Int, i3: Int
            ) { }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int, i2: Int, i3: Int
            ) {
                // Do whatever
                if (!charSequence.toString().trim { it <= ' ' }.isEmpty()) {
                    Log.d(className, "Text Entered")
                    viewModel.isJoinButtonEnabled = true
                } else {
                    Log.d(className, "Blank Text")
                    viewModel.isJoinButtonEnabled = false
                }
            }
        }
        binding.amJoinChannelEditText.addTextChangedListener(channelNameTextWatcher)

        //Initialize Agora SDK
        setupVoiceSDKEngine()
    }

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@MainActivity, requestedPermission[0] ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupVoiceSDKEngine() {
        try {
            token = RtcTokenBuilder2Generator.RtcTokenGenerate(
                appId,
                appCertificate,
                channelName,
                genIntID,
                3600,
                3600
            )
            val config = RtcEngineConfig()
            config.mContext = this@MainActivity
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            object : Thread() {
                override fun run() {
                    startProbeTest()
                }
            }.start()
        } catch (e: Exception) {
            throw RuntimeException("Check the error. $e")
        }
    }

    fun startProbeTest() {
        // Configure a LastmileProbeConfig instance.
        val config = LastmileProbeConfig()
        // Probe the uplink network quality.
        config.probeUplink = true
        // Probe the downlink network quality.
        config.probeDownlink = true
        // The expected uplink bitrate (bps). The value range is [100000,5000000].
        config.expectedUplinkBitrate = 100000
        // The expected downlink bitrate (bps). The value range is [100000,5000000].
        config.expectedDownlinkBitrate = 100000
        agoraEngine!!.startLastmileProbeTest(config)
        showMessage("Running the last mile probe test ...")
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateNetworkStatus(quality: Int) {
        if (quality in 1..2) {
            binding.networkStatus.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity, android.R.color.holo_green_light
                )
            )
            viewModel.networkStatus = "Network Status: Good"
        } else if (quality <= 4) {
            binding.networkStatus.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity, android.R.color.holo_orange_light
                )
            )
            viewModel.networkStatus = "Network Status: Average"
        } else if (quality <= 6) {
            binding.networkStatus.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity, android.R.color.holo_red_dark
                )
            )
            viewModel.networkStatus = "Network Status: Poor"
        } else {
            binding.networkStatus.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity, android.R.color.darker_gray
                )
            )
            viewModel.networkStatus = "Network Status: Un-Available"
        }
    }

    private fun joinLeaveChannel() {
        if (isJoined) {
            object : Thread() {
                override fun run() {
                    agoraEngine!!.leaveChannel()
                }
            }.start()
            viewModel.joinLeaveButtonText = "JOIN"
        } else {
            object : Thread() {
                override fun run() {
                    joinChannel()
                }
            }.start()
            viewModel.joinLeaveButtonText = "LEAVE"
        }
    }

    private fun joinChannel() {
        val options = ChannelMediaOptions()
        options.autoSubscribeAudio = true
        // Set both clients as the BROADCASTER.
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        // Set the channel profile as BROADCASTING.
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING

        // Join the channel with a temp token.
        // You need to specify the user ID yourself, and ensure that it is unique in the channel.
        if (token != null) {
            agoraEngine!!.joinChannel(token, channelName, genIntID, options)
        } else {
            Log.d(className, "token was null")
        }
    }
}