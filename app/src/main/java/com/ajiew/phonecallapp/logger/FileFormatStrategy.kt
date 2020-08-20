package com.ajiew.phonecallapp.logger

import android.os.Environment
import android.os.HandlerThread
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.LogStrategy
import com.orhanobut.logger.Logger.*
import java.text.SimpleDateFormat
import java.util.*

class FileFormatStrategy internal constructor(builder: Builder) : FormatStrategy {
    private val NEW_LINE = System.getProperty("line.separator")
    private val NEW_LINE_REPLACEMENT = " <br> "
    private val SEPARATOR = ","

    private var date: Date = builder.date
    private var dateFormat: SimpleDateFormat = builder.dateFormat
    private var logStrategy: LogStrategy = builder.logStrategy!!
    private var tag: String = builder.tag

    override fun log(priority: Int, onceOnlyTag: String?, message: String) {

        val tag = formatTag(onceOnlyTag)

        date.time = System.currentTimeMillis()

        val builder = StringBuilder()

        // thread id
        builder.append("threadId:")
        builder.append(Thread.currentThread().id)

        // human-readable date/time
        builder.append(SEPARATOR)
        builder.append(dateFormat.format(date))

        // level
        builder.append(SEPARATOR)
        builder.append(logLevel(priority))

        // tag
        builder.append(SEPARATOR)
        builder.append(tag)

        builder.append(SEPARATOR)
        // message
        if (message.contains(NEW_LINE)) {
            // a new line would break the CSV format, so we replace it here
            val msg = message.replace(NEW_LINE.toRegex(), NEW_LINE_REPLACEMENT)
            builder.append(msg)
        } else {
            builder.append(message)
        }

        // new line
        builder.append(NEW_LINE)

        logStrategy.log(priority, tag, builder.toString())
    }

    private fun formatTag(tag: String?): String? {
        return if (!tag.isNullOrBlank() && this.tag != tag) {
            this.tag + "-" + tag
        } else this.tag
    }

    companion object {

        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }

        private fun logLevel(value: Int): String {
            return when (value) {
                VERBOSE -> "VERBOSE"
                DEBUG -> "DEBUG"
                INFO -> "INFO"
                WARN -> "WARN"
                ERROR -> "ERROR"
                ASSERT -> "ASSERT"
                else -> "UNKNOWN"
            }
        }
    }

    class Builder internal constructor() {

        internal var date = Date()
        internal var dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)
        internal var tag: String = "PRETTY_LOGGER"
        internal var logStrategy: LogStrategy? = null

        fun date(date: Date): Builder {
            this.date = date
            return this
        }

        fun dateFormat(dateFormat: SimpleDateFormat): Builder {
            this.dateFormat = dateFormat
            return this
        }

        fun logStrategy(logStrategy: LogStrategy): Builder {
            this.logStrategy = logStrategy
            return this
        }

        fun tag(tag: String): Builder {
            this.tag = tag
            return this
        }

        fun build(logDir: String): FileFormatStrategy {
            if (logStrategy == null) {
                val diskPath = Environment.getExternalStorageDirectory().absolutePath
                val folder = "$diskPath/logger/$logDir"
                val ht = HandlerThread("AndroidFileLogger.$folder")
                ht.start()
                val handler = DiskLogStrategy.WriteHandler(ht.looper, folder, MAX_BYTES, MAX_HISTORY, TOTAL_SIZE_CAP)
                logStrategy = DiskLogStrategy(handler)
            }
            return FileFormatStrategy(this)
        }

        companion object {
            private const val MAX_BYTES = 1024 * 1024 * 30 // 30M per file
            private const val MAX_HISTORY = 5 // 5 days
            private const val TOTAL_SIZE_CAP = 1024 * 1024 * 1024 * 2L // 2GB
        }
    }
}