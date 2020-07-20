package com.humoud.apksneeze

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.net.InetAddress
import java.net.UnknownHostException


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M) // For using: Settings.Global.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvProxy: TextView = findViewById<TextView>(R.id.currproxy)
        val tvIP: TextView = findViewById<TextView>(R.id.ipaddress)
        val tvVersion: TextView = findViewById<TextView>(R.id.androidVersion)

        val wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo

        if (info.supplicantState == SupplicantState.COMPLETED) {
            tvProxy.text = getProxyDetails(applicationContext)
            tvIP.text = intToInetAddress(info.ipAddress).toString()
            tvVersion.text = getAndroidVersion()
        }

        // Handle Popup for proxy setup
        val proxySetupBtn = findViewById<Button>(R.id.setProxyBtn)
        proxySetupBtn.setOnClickListener {
            val li = LayoutInflater.from(applicationContext)
            val promptsView: View = li.inflate(R.layout.proxy_prompt_layout, null)

            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(
                this@MainActivity
            )
            alertDialogBuilder.setView(promptsView)

            val setProxyValue = promptsView.findViewById(R.id.proxypopup) as EditText
            // set dialog message
            alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(
                    "OK"
                ) { dialog, id -> // get user input and set it to result
                    // edit text
                    val ipPort = ("" + setProxyValue.text)
                    Settings.Global.putString(contentResolver, Settings.Global.HTTP_PROXY, ipPort)
                    Toast.makeText(this@MainActivity, "Proxy Settings Modified", Toast.LENGTH_SHORT).show()
                    // update view
                    tvProxy.text = getProxyDetails(applicationContext)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, id -> dialog.cancel() }
            // create alert dialog
            val alertDialog = alertDialogBuilder.create()
            // show it
            alertDialog.show()
        }

        // Handle Clear Proxy Settings button
        val clearProxyBtn = findViewById<Button>(R.id.clearProxyBtn)
        clearProxyBtn.setOnClickListener {
            Settings.Global.putString(contentResolver, Settings.Global.HTTP_PROXY, ":0")
            // update view
            tvProxy.text = getProxyDetails(applicationContext)
            Toast.makeText(this@MainActivity, "Cleared Proxy Settings", Toast.LENGTH_SHORT).show()
        }

        // Handle refresh button
        val refreshBtn = findViewById<Button>(R.id.refreshBtn)
        refreshBtn.setOnClickListener{
            finish();
            startActivity(intent);
        }
    }

    private fun getProxyDetails(context: Context?): String? {
        var proxyAddress = String()
        try {
            if(System.getProperty("http.proxyHost") == null)
                proxyAddress = "No Proxy"
            else {
                proxyAddress = System.getProperty("http.proxyHost")
                proxyAddress += ":" + System.getProperty("http.proxyPort")
            }
        } catch (ex: Exception) {
            //ignore
        }
        return proxyAddress
    }

    private fun intToInetAddress(hostAddress: Int): InetAddress? {
        val addressBytes = byteArrayOf(
            (0xff and hostAddress).toByte(),
            (0xff and (hostAddress shr 8)).toByte(),
            (0xff and (hostAddress shr 16)).toByte(),
            (0xff and (hostAddress shr 24)).toByte()
        )
        return try {
            InetAddress.getByAddress(addressBytes)
        } catch (e: UnknownHostException) {
            throw AssertionError()
        }
    }

//    @Throws(java.lang.Exception::class)
//    private fun runShellCommand(command: String) {
//        val process = Runtime.getRuntime().exec(command)
//        process.waitFor()
//    }

    private fun getAndroidVersion(): String? {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "Android SDK: $sdkVersion ($release)"
    }
}