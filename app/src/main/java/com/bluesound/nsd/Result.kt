package com.bluesound.nsd

import android.net.nsd.NsdServiceInfo

sealed class Result {
    data object Loading : Result()
    class Success(val services: List<NsdServiceInfo>) : Result()
    data object Error : Result()
}
