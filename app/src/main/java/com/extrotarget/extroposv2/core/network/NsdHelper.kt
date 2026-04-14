package com.extrotarget.extroposv2.core.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.Executor

@Singleton
class NsdHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val SERVICE_TYPE = "_extropos._tcp."
    private val SERVICE_NAME = "ExtroPOS_Master"

    private var registrationListener: NsdManager.RegistrationListener? = null

    fun registerService(port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(regInfo: NsdServiceInfo) {
                Log.d("NSD", "Service registered: ${regInfo.serviceName}")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NSD", "Registration failed: $errorCode")
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                Log.d("NSD", "Service unregistered")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NSD", "Unregistration failed: $errorCode")
            }
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                Log.e("NSD", "Error unregistering service", e)
            }
            registrationListener = null
        }
    }

    fun discoverServices(): Flow<List<NsdServiceInfo>> = callbackFlow {
        val discoveredServices = mutableMapOf<String, NsdServiceInfo>()
        val activeCallbacks = mutableMapOf<String, NsdManager.ServiceInfoCallback>()

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("NSD", "Service discovery started")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service found: ${serviceInfo.serviceName}")
                if (serviceInfo.serviceType.contains("extropos")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val callback = object : NsdManager.ServiceInfoCallback {
                            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                                Log.e("NSD", "Callback registration failed: $errorCode")
                            }

                            override fun onServiceUpdated(resolvedServiceInfo: NsdServiceInfo) {
                                Log.d("NSD", "Service updated/resolved: ${resolvedServiceInfo.host?.hostAddress}")
                                discoveredServices[resolvedServiceInfo.serviceName] = resolvedServiceInfo
                                trySend(discoveredServices.values.toList())
                            }

                            override fun onServiceLost() {
                                Log.d("NSD", "Service callback lost")
                                discoveredServices.remove(serviceInfo.serviceName)
                                trySend(discoveredServices.values.toList())
                            }

                            override fun onServiceInfoCallbackUnregistered() {
                                Log.d("NSD", "Callback unregistered")
                            }
                        }
                        activeCallbacks[serviceInfo.serviceName] = callback
                        nsdManager.registerServiceInfoCallback(serviceInfo, context.mainExecutor, callback)
                    } else {
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) {
                                Log.e("NSD", "Resolve failed: $errorCode")
                            }

                            override fun onServiceResolved(resolvedServiceInfo: NsdServiceInfo) {
                                Log.d("NSD", "Service resolved: ${resolvedServiceInfo.host.hostAddress}")
                                discoveredServices[resolvedServiceInfo.serviceName] = resolvedServiceInfo
                                trySend(discoveredServices.values.toList())
                            }
                        })
                    }
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d("NSD", "Service lost: ${serviceInfo.serviceName}")
                discoveredServices.remove(serviceInfo.serviceName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    activeCallbacks.remove(serviceInfo.serviceName)?.let {
                        try { nsdManager.unregisterServiceInfoCallback(it) } catch (e: Exception) {}
                    }
                }
                trySend(discoveredServices.values.toList())
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i("NSD", "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NSD", "Discovery failed: $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NSD", "Stop discovery failed: $errorCode")
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            nsdManager.stopServiceDiscovery(discoveryListener)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activeCallbacks.values.forEach { 
                    try { nsdManager.unregisterServiceInfoCallback(it) } catch (e: Exception) {}
                }
                activeCallbacks.clear()
            }
        }
    }
}
