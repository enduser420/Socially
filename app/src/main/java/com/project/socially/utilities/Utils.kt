package com.project.socially.utilities

import java.text.SimpleDateFormat

class Utils {
    companion object {
        const val SECOND_MILLIS: Long = 1000
        const val MINUTE_MILLIS: Long = 60 * SECOND_MILLIS
        const val HOUR_MILLIS: Long = 60 * MINUTE_MILLIS
        const val DAY_MILLIS: Long = 24 * HOUR_MILLIS

        fun getTimeAgo(time: Long): String? {
            val now: Long = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return null
            }

            val diff = now - time
            return if (diff < MINUTE_MILLIS) {
                "just now"
            } else if (diff < 2 * MINUTE_MILLIS) {
                "a minute ago"
            } else if (diff < 50 * MINUTE_MILLIS) {
                (diff / MINUTE_MILLIS).toString() + " minutes ago"
            } else if (diff < 90 * MINUTE_MILLIS) {
                "an hour ago"
            } else if (diff < 24 * HOUR_MILLIS) {
                (diff / HOUR_MILLIS).toString() + " hours ago"
            } else if (diff < 48 * HOUR_MILLIS) {
                "yesterday"
            } else {
                if ((diff / DAY_MILLIS) > 5L) {
                    val simpledateformat = SimpleDateFormat("dd/MM/yyyy")
                    simpledateformat.format(time)
                } else {
                    (diff / DAY_MILLIS).toString() + " days ago"
                }
            }
        }
    }
}