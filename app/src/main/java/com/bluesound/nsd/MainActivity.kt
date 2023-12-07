package com.bluesound.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.HandlerCompat.postDelayed
import com.bluesound.nsd.ui.theme.NSDTheme

private const val SERVICE_TYPE = "_tidalconnect._tcp."
private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val nsdManager: NsdManager by lazy { getSystemService(NsdManager::class.java) }
    private val wifiManager: WifiManager by lazy { getSystemService(WifiManager::class.java) }
    private lateinit var multicastLock: WifiManager.MulticastLock

    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success $service")
            nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    // Called when the resolve fails. Use the error code for debugging purposes.
                    Log.e(TAG, "Resolve failed: $errorCode")
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    Log.e(TAG, "Resolve Succeeded: $serviceInfo")
                    mainViewModel.addService(serviceInfo)
                }
            })
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
            mainViewModel.removeService(service)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val screenState by mainViewModel.screenStateFlow.collectAsState(Result.Loading)
            val scrollState = rememberScrollState()

            NSDTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    when (screenState) {
                        is Result.Success -> {
                            val services = (screenState as Result.Success).services
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                            ) {
                                services.firstOrNull()?.let {
                                    Text(
                                        modifier = Modifier.padding(16.dp),
                                        text = "${it.serviceType} Services",
                                        style = TextStyle(
                                            fontSize = 24.sp, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                services.forEach {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = it.serviceName.substringBefore("-"),
                                            style = TextStyle(
                                                fontSize = 20.sp, fontWeight = FontWeight.Bold
                                            )
                                        )
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && it.hostAddresses.isNotEmpty()) {
                                            it.hostAddresses.forEach {
                                                Text(text = "Host address: ${it.hostAddress}")
                                            }
                                        } else {
                                            Text(text = "Host address: ${it.host}")
                                        }
                                    }
                                }
                            }
                        }

                        is Result.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Text(
                                        modifier = Modifier.padding(16.dp),
                                        text = "Loading...",
                                        style = TextStyle(
                                            fontSize = 20.sp, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        is Result.Error -> {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = "Error...",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        multicastLock = wifiManager.createMulticastLock("multicastLock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()

        postDelayed(Handler(mainLooper), {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }, null, 2000)
    }

    override fun onStop() {
        mainViewModel.clear()
        multicastLock.release() // release after browsing
        nsdManager.stopServiceDiscovery(discoveryListener)
        super.onStop()
    }

}
