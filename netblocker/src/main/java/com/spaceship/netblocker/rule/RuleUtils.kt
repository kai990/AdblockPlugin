package com.spaceship.netblocker.rule



const val RULE_SEPARATOR = "$"

const val RULE_UNBLOCK = "@@"


const val RULE_APP = "app="
const val RULE_HIGH_PRIORITY = "important"


fun replaceRuleDomain(rule: String, domain: String): String {
    val list = rule.split(RULE_SEPARATOR).toMutableList()
    list[0] = domain
    return list.joinToString(separator = RULE_SEPARATOR) { it }
}


fun processRuleLevel(rule: String, level: String, isAdd: Boolean): String {
    val list = rule.split(RULE_SEPARATOR).toMutableList()
    val oldLevels = list.getOrElse(1) { "" }.split(",").toMutableList()

    if (isAdd) {
        oldLevels.add(level)
    } else {
        oldLevels.remove(level)
    }

    val levels = ArrayList<String>()

    oldLevels.firstOrNull { it.startsWith(RULE_APP) }?.let { levels.add(it) }
    oldLevels.firstOrNull { it == RULE_HIGH_PRIORITY }?.let { levels.add(it) }

    return if (levels.size > 0) {
        list[0] + RULE_SEPARATOR + levels.joinToString(separator = ",") { it }
    } else list[0]
}

fun getRuleApp(rule: String): String? {
    runCatching {
        return rule.split(RULE_SEPARATOR)[1].split(",")[0].split("=")[1]
    }
    return null
}

fun getRuleDomain(rule: String): String {
    return rule.split(RULE_SEPARATOR)[0]
}