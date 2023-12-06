package com.bluesound.nsd

import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private var _screenStateFlow: MutableStateFlow<Result> = MutableStateFlow(Result.Loading)

    private val _services: MutableSet<NsdServiceInfo> = mutableSetOf()

    val screenStateFlow: StateFlow<Result>
        get() = _screenStateFlow

    fun addService(serviceInfo: NsdServiceInfo) {
        _services.add(serviceInfo)
        _screenStateFlow.value = Result.Success(_services.toList())
    }

    fun removeService(service: NsdServiceInfo) {
        _services.remove(service)
        _screenStateFlow.value = Result.Success(_services.toList())
    }

}
