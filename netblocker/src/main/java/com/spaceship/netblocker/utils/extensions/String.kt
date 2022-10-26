package com.spaceship.netblocker.utils.extensions


fun String?.upperFirst(length: Int): String? {
    if (!this.isNullOrEmpty()) {
        return "${this.substring(0, length).toUpperCase()}${this.substring(length)}"
    }
    return this
}

fun String?.trimLeftNum(): String? {
    if (!this.isNullOrEmpty()) {
        val sb = StringBuffer()
        var numberStop = false
        for (c in this) {
            if (!numberStop) {
                numberStop = !Character.isDigit(c)
            }
            if (numberStop) {
                sb.append(c)
            }
        }
        return sb.toString()
        return "${this.substring(0, length).toUpperCase()}${this.substring(length)}"
    }
    return this
}


fun String.removeZLWChar(): String {
    return this.replace("[\\p{Cf}]".toRegex(), "")
}

fun String.getFileNameFromUrl(): String {
    return this.substring(this.lastIndexOf('/') + 1, length)
}
