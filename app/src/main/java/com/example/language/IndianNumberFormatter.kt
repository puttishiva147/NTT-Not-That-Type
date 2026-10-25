package com.example.language

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object IndianNumberFormatter {

    fun formatIndianNumber(value: Long, currencySymbol: String = ""): String {
        val isNegative = value < 0
        val absVal = if (isNegative) -value else value
        val str = absVal.toString()

        val formatted = if (str.length <= 3) {
            str
        } else {
            val lastThree = str.substring(str.length - 3)
            val rest = str.substring(0, str.length - 3)
            val sb = StringBuilder()
            var count = 0
            for (i in rest.length - 1 downTo 0) {
                sb.append(rest[i])
                count++
                if (count == 2 && i != 0) {
                    sb.append(",")
                    count = 0
                }
            }
            sb.reverse().toString() + "," + lastThree
        }

        val prefix = if (currencySymbol.isNotBlank()) "$currencySymbol" else ""
        val sign = if (isNegative) "-" else ""
        return "$sign$prefix$formatted"
    }

    fun formatDecimal(value: Double, currencySymbol: String = ""): String {
        val wholePart = value.toLong()
        val formattedWhole = formatIndianNumber(wholePart, currencySymbol)
        val fractionPart = ((value - wholePart) * 100).toInt()
        return if (fractionPart > 0) {
            "$formattedWhole.${String.format(Locale.US, "%02d", fractionPart)}"
        } else {
            formattedWhole
        }
    }
}
