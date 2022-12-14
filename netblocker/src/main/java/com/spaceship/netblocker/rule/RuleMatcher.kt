package com.spaceship.netblocker.rule

import androidx.annotation.WorkerThread
import com.spaceship.netblocker.utils.Slog
import com.spaceship.netblocker.utils.extensions.safeRun
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


object RuleMatcher {
    private const val TAG = "AclMatcher"

    
    private val whiteDict = HashMap<String, ArrayList<String>>()
    private val blackDict = HashMap<String, ArrayList<String>>()

    private val whiteAppDict = HashMap<String, HashMap<String, ArrayList<String>>>()
    private val blackAppDict = HashMap<String, HashMap<String, ArrayList<String>>>()

    fun clear() {
        blackDict.clear()
        whiteDict.clear()
        whiteAppDict.clear()
        blackAppDict.clear()
    }

    
    @WorkerThread
    @Suppress("UNCHECKED_CAST")
    fun read(inStream: InputStream) {
        Slog.d(TAG, "read start")
        BufferedReader(InputStreamReader(inStream)).use { reader ->
            reader.lineSequence().forEach { line ->
                if (line.isEmpty()) {
                    return@forEach
                }
                val regex = pureRegex(line)
                // word match
                if (regex.startsWith("#") || regex.startsWith("[")) {

                } else if (regex.contains(RULE_APP)) {

                    val pkg = getRuleApp(regex) ?: return@forEach
                    val rootDict = (if (regex.startsWith(RULE_UNBLOCK)) whiteAppDict else blackAppDict)
                    val dict = if (rootDict[pkg] == null) {
                        val hashMap = HashMap<String, ArrayList<String>>()
                        rootDict[pkg] = hashMap
                        hashMap
                    } else {
                        rootDict[pkg]
                    }
                    dumpAcl(dict, splitRegexDomain(regex))
                } else {
                    val dict = if (regex.startsWith(RULE_UNBLOCK)) whiteDict else blackDict
                    dumpAcl(dict, splitRegexDomain(regex))
                }
            }
        }
        Slog.d(TAG, "read finish")
    }

    
    @Suppress("UNCHECKED_CAST")
    fun isMatched(domain: String?, pkg: String?): Boolean {
        if (domain.isNullOrEmpty()) {
            return false
        }

        val isWhite = isMatchDomain(whiteDict, domain) || isMatchDomain(whiteAppDict[pkg], domain)
        if (isWhite) {
            return false
        }

//        if (domain.contains("doubleclick")) {
//            Slog.w("xxx", domain)
//        }
        return isMatchDomain(blackDict, domain) || isMatchDomain(blackAppDict[pkg], domain)
    }

    private fun isMatchDomain(dict: HashMap<String, ArrayList<String>>?, domain: String): Boolean {
        if (dict == null || !domain.contains(".")) return false

        val split = domain.split(".").reversed()
        val mainDomain = "${split[0]}.${split[1]}"
        val ruleList = dict[mainDomain] ?: return false

        ruleList.forEach { rule ->
            val ruleSplit = rule.split(".")
            if (containsDomain(ruleSplit, split)) {
                return true
            }
        }

        return false
    }

    
    private fun containsDomain(listA: List<String>, listB: List<String>): Boolean {
        if (listA == listB) {
//            Slog.w(TAG, "containsDomain listA == listB matched")
            return true
        }

//        Slog.w(TAG, "containsDomain listA:${listA},listB${listB}")

        if (listA.size > listB.size) {
//            Slog.w(TAG, "containsDomain listA.size > listB.size -> false")
            return false
        }


        listA.forEachIndexed { index, s ->
            if (s != listB[index]) {
//                Slog.w(TAG, "containsDomain not match:s=${s},b=${listB[index]}")
                return false
            }
        }
//        Slog.w(TAG, "containsDomain matched")

        return true
    }

    private fun dumpAcl(dict: HashMap<String, ArrayList<String>>?, domainSplit: List<String>) {
        safeRun {
            if (dict == null || domainSplit.size < 2) return@safeRun

            val mainDomain = "${domainSplit[0]}.${domainSplit[1]}"
            val domain = domainSplit.joinToString(separator = ".") { it }
            val list = dict.getOrElse(mainDomain) { null } ?: arrayListOf()

            if (list.contains(domain)) {
                return@safeRun
            }
            dict[mainDomain] = list
            list.add(domain)
        }
    }

    
    private fun splitRegexDomain(domain: String): List<String> {
        return getRuleDomain(domain).removePrefix("www.").replace("\\.", ".").split(".").reversed()
    }

    private fun pureRegex(line: String): String {
        return line.replace("127.0.0.1", "").replace("0.0.0.0", "").trim().split(" ")[0].trim()
    }
}
