package com.spaceship.netblocker

import com.spaceship.netblocker.message.dispatchDomain
import com.spaceship.netblocker.model.DispatchPacket

/**
 * 分发请求
 *
 * @author John
 * @since 2019-05-01 16:21
 */
class ProxyDispatcher {

    private var dispatchHandler: RequestDispatchHandler? = null

    /**
     * 是否拦截
     */
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
        /**
         * 判断是否拦截请求
         */
        fun handleDispatch(packet: DispatchPacket): Int
    }

    companion object {
        /**
         * 直连
         */
        const val TYPE_DIRECT = 0

        /**
         * 阻止
         */
        const val TYPE_BLOCK = 1

        /**
         * 通过 VPN 连接
         */
        const val TYPE_REMOTE = 2
    }
}
