package com.example.rawan.livestream

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import net.ossrs.rtmp.ConnectCheckerRtmp

class MainActivity : AppCompatActivity(),ConnectCheckerRtmp, SurfaceHolder.Callback
{


    private val videoPermissions  = arrayOf<String>(
        Manifest.permission.CAMERA
        , Manifest.permission.RECORD_AUDIO)
    private val requestVideoCode = 1
    private var layout: RelativeLayout? = null
    private var startStopButton : Button?=null
    private var rtmpCamera: RtmpCamera1?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.layout)

        if(ActivityCompat.checkSelfPermission(this,videoPermissions[0]) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this,videoPermissions[0]) != PackageManager.PERMISSION_GRANTED){

            requestVideoPermissions()
        }else{

        }
    }
    private fun startLiveStream() {
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        rtmpCamera = RtmpCamera1(surfaceView, this)
        startStopButton = findViewById(R.id.b_start_stop)
        surfaceView.holder.addCallback(this)
        startStopButton!!.setOnClickListener {
            if (!rtmpCamera!!.isStreaming) {
                if (rtmpCamera!!.isRecording || rtmpCamera!!.prepareAudio() && rtmpCamera!!.prepareVideo()) {
                    startStopButton!!.text = "Stop"
                    rtmpCamera!!.startStream("rtmp://rtmp-global.cloud.vimeo.com/live")
                } else {
                    Toast.makeText(
                        this, "Error preparing stream, This device cant do it",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                startStopButton!!.text = "Start"
                rtmpCamera!!.stopStream()
            }
        }
    }



        @TargetApi(Build.VERSION_CODES.M)
    private  fun requestVideoPermissions(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,videoPermissions[0])||
            ActivityCompat.shouldShowRequestPermissionRationale(this, videoPermissions[1])){
           showSnackBar(1,R.string.permission_not_granted)

        }
        else{
            ActivityCompat.requestPermissions(this, videoPermissions,requestVideoCode)
        }

    }
    @TargetApi(Build.VERSION_CODES.M)
    private fun showSnackBar(action: Int, idString: Int)
    {
        if(action==0)
        {
            Snackbar.make(layout!!,idString, Snackbar.LENGTH_SHORT).show()

        }else
        {
            Snackbar.make(layout!!,idString, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok){
                    ActivityCompat.requestPermissions(this,videoPermissions,requestVideoCode)
                }.show()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if(requestCode==requestVideoCode){
            var isGranted=0
            for(grant in grantResults){
                if(grant!=PackageManager.PERMISSION_GRANTED){
                    isGranted=1
                    break
                }
            }
            if(isGranted==0){
                //start the live streaming
                startLiveStream()
            }
        }
    }
    override fun onConnectionSuccessRtmp() {
        runOnUiThread { Toast.makeText(this@MainActivity, "Connection success", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Connection failed. $reason", Toast.LENGTH_SHORT)
                .show()
            rtmpCamera!!.stopStream()
            startStopButton!!.text = "Start"
        }
    }


    override fun onDisconnectRtmp() {
        runOnUiThread({ Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show() })
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread({ Toast.makeText(this@MainActivity, "Auth error", Toast.LENGTH_SHORT).show() })
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread { Toast.makeText(this@MainActivity, "Auth success", Toast.LENGTH_SHORT).show() }
    }


    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        rtmpCamera!!.startPreview()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera!!.isRecording) {
            rtmpCamera!!.stopRecord()
        }
        if (rtmpCamera!!.isStreaming) {
            rtmpCamera!!.stopStream()
            startStopButton!!.text = "Start"
        }
        rtmpCamera!!.stopPreview()
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        rtmpCamera!!.startPreview()
    }


}
