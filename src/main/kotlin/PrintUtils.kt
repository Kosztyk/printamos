package com.pswidersk

import java.util.regex.Pattern

fun isValidPrinterName(name: String): Boolean {
    val pattern = Pattern.compile("^[a-zA-Z0-9_-]{1,128}$")
    return pattern.matcher(name).matches()
}