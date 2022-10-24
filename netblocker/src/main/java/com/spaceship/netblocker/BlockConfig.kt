package com.spaceship.netblocker

/**
 * @author wangkai
 */
class BlockConfig(
    var packageId: String,
    // 白名单 （可以访问网络）
    val whiteAppList: MutableList<String> = arrayListOf(),
    // 黑名单 （禁止访问网络）
    val blackAppList: MutableList<String> = arrayListOf(),
    // 不经过VPN 网络的APP
    val allowedAppSet: HashSet<String> = HashSet()
)
