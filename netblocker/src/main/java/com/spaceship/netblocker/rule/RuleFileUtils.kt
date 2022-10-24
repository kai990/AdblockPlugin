package com.spaceship.netblocker.rule

import android.text.format.DateUtils
import com.spaceship.netblocker.Env
import com.spaceship.netblocker.utils.Slog
import com.spaceship.netblocker.utils.thread.ThreadPool.io
import java.io.*
import java.net.URL

/**
 * @author John
 */

/**
 * 自定义规则路径
 */
private const val CUSTOM_RULE_FILE_NAME = "ss_custom_rule_file.txt"

private val context by lazy { Env.storageContext() }

private val RULE_PATH by lazy {
    File(context.noBackupFilesDir.absolutePath + "/rule_2_0").apply {
        if (!exists()) {
            mkdirs()
        }
    }
}

/**
 * 从文件导入的规则文件存储路径
 */
val CUSTOM_RULE_FILE by lazy {
    val folder = File(context.noBackupFilesDir.absolutePath + "/custom_rule").apply {
        if (!exists()) {
            mkdirs()
        }
    }
    File(folder, "custom_rule.txt")
}


/**
 * 规则配置文件 > online 版 default_rule_config.json
 */
val CONFIG_FILE = File(RULE_PATH, "rule_config.json")

/**
 * 是否是自定义规则文件
 */
fun isCustomRuleFile(file: File): Boolean {
    return file.absolutePath.contains(CUSTOM_RULE_FILE_NAME)
}

/**
 * 获取最后规则文件同步的时间（排除本地自定义规则）
 */
fun getRuleLastSyncTime(): Long {
    return getRuleFiles().filter { !isCustomRuleFile(it) }.maxByOrNull { it.lastModified() }?.lastModified() ?: 0
}

/**
 * 返回 acl 规则列表
 */
fun getRuleFiles() = RULE_PATH.listFiles().orEmpty()

fun getUrlRuleFile(url: String): File {
    val filename = url.substring(url.lastIndexOf("/") + 1, url.length)
    return File(RULE_PATH, filename)
}

fun getRuleFile(filename: String): File {
    return File(RULE_PATH, filename)
}

fun isRuleFileExist(): Boolean {
    return !getRuleFiles().isEmpty()
}

fun downloadRule(url: String): File? {
    try {
        val ruleFile = getUrlRuleFile(url)
        // 10 分钟内只下载一次
        if (ruleFile.exists() && System.currentTimeMillis() - ruleFile.lastModified() < 10 * DateUtils.MINUTE_IN_MILLIS) {
            Slog.v("download rule", "下载间隔过短:$url")
            return ruleFile
        }

        Slog.d("download rule", "download rule:$url")
        val ruleStr = URL(url).openStream().bufferedReader()
            .use { it.readText() }.trim()
//        if (BuildConfig.DEBUG) {
//            Slog.v("download rule", "rule content:\n$ruleStr")
//        }
//
//        if (ruleFile.startsWith("<!DOCTYPE html>")) {
//            Slog.w("download rule", "download fail:$ruleStr")
//            return null
//        }

        ruleFile.let { file ->
            file.printWriter().use { it.write(ruleStr) }
            Slog.d("download rule", "download success:$url")
            return file
        }
    } catch (e: Exception) {
        Slog.e(e, false)
        return null
    }
}

/**
 * 保存自定义规则
 * callback -> true:保存成功 false:规则存在
 */
fun saveCustomRule(rule: String, callback: (Boolean) -> Unit) {
    io {
        val file = getRuleFile(CUSTOM_RULE_FILE_NAME)
        if (file.exists()) {
            BufferedReader(InputStreamReader(file.inputStream())).use { reader ->
                reader.lineSequence().forEach { regex ->
                    if (regex == rule) {
                        callback(false)
                        return@io
                    }
                }
            }
        }
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true))).use { writer ->
            writer.newLine()
            writer.write(rule)
            RuleMatcher.read(file.inputStream())
            callback(true)
            return@io
        }
    }
}
