package com.spaceship.netblocker

import com.spaceship.netblocker.message.dispatchDomain
import com.spaceship.netblocker.model.DispatchPacket


class ProxyDispatcher {

    private var dispatchHandler: RequestDispatchHandler? = null

    
    fun dispatch(packet: DispatchPacket): Int {
        dispatchHandler?.let {
            return it.handleDispatch(packet)
        }

        return dispatchDomain(packet)
    }

    fun setDispatchHandler(dispatchHandler: RequestDispatchHandler) {
        this.dispatchHandler = dispatchHandler
    }

    fun getDispatchHandler(): RequestDispatchHandler? {
        return dispatchHandler
    }

    interface RequestDispatchHandler {
        
        fun handleDispatch(packet: DispatchPacket): Int
    }

    companion object {
        
        const val TYPE_DIRECT = 0

        
        const val TYPE_BLOCK = 1

        
        const val TYPE_REMOTE = 2
    }
}
