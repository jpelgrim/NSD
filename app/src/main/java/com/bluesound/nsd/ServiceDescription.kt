package com.bluesound.nsd

class ServiceDescription(val name: String, val address: String, val port: Int) {
    override fun toString(): String {
        return "ServiceDescription(name='$name', address='$address', port=$port)"
    }
}
