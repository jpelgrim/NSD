package com.bluesound.nsd

import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private var _screenStateFlow: MutableStateFlow<Result> = MutableStateFlow(Result.Loading)

    private val _services: MutableList<NsdServiceInfo> = mutableListOf()

    val screenStateFlow: StateFlow<Result>
        get() = _screenStateFlow

    fun addService(nsdServiceInfo: NsdServiceInfo) {
        _services.removeIf { it.serviceName == nsdServiceInfo.serviceName }
        _services.add(nsdServiceInfo)
        _screenStateFlow.value = Result.Success(_services.sortedBy { it.serviceName }.toList())
    }

    fun removeService(nsdServiceInfo: NsdServiceInfo) {
        _services.removeIf { it.serviceName == nsdServiceInfo.serviceName }
        _screenStateFlow.value = Result.Success(_services.sortedBy { it.serviceName }.toList())
    }

    fun clear() {
        _services.clear()
        _screenStateFlow.value = Result.Loading
    }

}
