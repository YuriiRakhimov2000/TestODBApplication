package com.yura.testodbapplication
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sohrab.obd.reader.application.Preferences
import com.sohrab.obd.reader.constants.DefineObdReader.ACTION_CONNECTION_STATUS_MSG
import com.sohrab.obd.reader.constants.DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA
import com.sohrab.obd.reader.obdCommand.ObdConfiguration
import com.sohrab.obd.reader.service.ObdReaderService
import com.sohrab.obd.reader.trip.TripRecord



class MainActivity : AppCompatActivity() {
    private var mObdInfoTextView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mObdInfoTextView = findViewById<TextView>(R.id.tv_obd_info)
        ObdConfiguration.setmObdCommands(this, null)


        // set gas price per litre so that gas cost can calculated. Default is 7 $/l


        // set gas price per litre so that gas cost can calculated. Default is 7 $/l
        val gasPrice = 7f // per litre, you should initialize according to your requirement.

        Preferences.get(this).setGasPrice(gasPrice)
        /**
         * Register receiver with some action related to OBD connection status
         */
        /**
         * Register receiver with some action related to OBD connection status
         */
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_READ_OBD_REAL_TIME_DATA)
        intentFilter.addAction(ACTION_CONNECTION_STATUS_MSG)
        registerReceiver(mObdReaderReceiver, intentFilter)

        //start service which will execute in background for connecting and execute command until you stop

        //start service which will execute in background for connecting and execute command until you stop
        startService(Intent(this, ObdReaderService::class.java))

    }

    private val mObdReaderReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            findViewById<View>(R.id.progress_bar).visibility = View.GONE
            mObdInfoTextView?.setVisibility(View.VISIBLE)
            val action = intent.action
            if (action == ACTION_CONNECTION_STATUS_MSG) {
                val connectionStatusMsg = intent.getStringExtra(ObdReaderService.INTENT_EXTRA_DATA)
                mObdInfoTextView?.setText(connectionStatusMsg)
                Toast.makeText(this@MainActivity, connectionStatusMsg, Toast.LENGTH_SHORT).show()
                if (connectionStatusMsg == getString(R.string.obd_connected)) {
                    //OBD connected  do what want after OBD connection
                } else if (connectionStatusMsg == getString(R.string.connect_lost)) {
                    //OBD disconnected  do what want after OBD disconnection
                } else {
                    // here you could check OBD connection and pairing status
                }
            } else if (action == ACTION_READ_OBD_REAL_TIME_DATA) {
                val tripRecord = TripRecord.getTripRecode(this@MainActivity)
                mObdInfoTextView?.setText(tripRecord.toString())
                // here you can fetch real time data from TripRecord using getter methods like
                //tripRecord.getSpeed();
                //tripRecord.getEngineRpm();
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregister receiver
        unregisterReceiver(mObdReaderReceiver)
        //stop service
        stopService(Intent(this, ObdReaderService::class.java))
        // This will stop background thread if any running immediately.
        Preferences.get(this).setServiceRunningStatus(false)
    }

}