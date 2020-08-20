package com.ajiew.phonecallapp.logger


import android.os.Handler
import android.os.Looper
import android.os.Message
import com.orhanobut.logger.LogStrategy
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Abstract class that takes care of background threading the file log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 *
 * Writes all logs to the disk with CSV format.
 */
class DiskLogStrategy(private val handler: Handler) : LogStrategy {

    override fun log(level: Int, tag: String?, message: String) {
        // do nothing on the calling thread, simply pass the tag/msg to the background thread
        handler.sendMessage(handler.obtainMessage(level, message))
    }

    internal class WriteHandler(
            looper: Looper,
            private val folder: String,
            private val maxFileSize: Int,
            private val maxHistory: Int,
            private val totalSizeCap: Long
    ) : Handler(looper) {

        init {
            val folder = File(folder)
            if (!folder.exists()) {
                folder.mkdirs()
            }
            // 初始化~~
        }

        private var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

        override fun handleMessage(msg: Message) {
            var fileWriter: FileWriter? = null
            try {
                val content = msg.obj as String
                val logFile = getLogFile() ?: return

                fileWriter = FileWriter(logFile, true)

                writeLog(fileWriter, content)

                fileWriter.flush()
                fileWriter.close()
            } catch (e: IOException) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush()
                        fileWriter.close()
                    } catch (e1: IOException) { /* fail silently */
                    }
                }
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        private fun writeLog(fileWriter: FileWriter, content: String) {
            fileWriter.append(content)
        }

        private fun getLogFile(): File? {

            val fileName = dateFormat.format(Date())
            val folder = File(folder)

            var totalSize = 0L
            val files = folder.listFiles { _, name ->
                Pattern.matches("^\\d{4}-\\d{2}-\\d{2}_\\d+[.]csv$", name)
            } ?: arrayOf()

            val sortedFiles = ArrayList<File>(files.sortedBy {
                totalSize += it.length()
                it.lastModified()
            })

            while (totalSize > totalSizeCap) {
                val removedFile = sortedFiles.removeAt(0)
                totalSize -= removedFile.length()
                try {
                    removedFile.delete()
                } catch (e: Throwable) {
                    /* fail silently */
                }
            }

            val dateMap = TreeMap<String, Int>()
            sortedFiles.forEach {
                val fileDate = it.name.substring(0, 10)
                val count = dateMap[fileDate] ?: 0
                dateMap[fileDate] = count + 1
            }

            while (dateMap.size > maxHistory) {
                val fileDate = dateMap.keys.first()
                val count = dateMap.remove(dateMap.keys.first()) ?: 1
                for (i in 0 until count) {
                    val f = File(folder, String.format("%s_%s.csv", fileDate, i))
                    try {
                        f.delete()
                    } catch (e: Throwable) {
                        /* fail silently */
                    }
                }
            }

            var newFileCount = 0
            var newFile: File
            var existingFile: File? = null

            newFile = File(folder, String.format("%s_%s.csv", fileName, newFileCount))
            while (newFile.exists()) {
                existingFile = newFile
                newFileCount++
                newFile = File(folder, String.format("%s_%s.csv", fileName, newFileCount))
            }

            return if (existingFile != null) {
                if (existingFile.length() >= maxFileSize) {
                    newFile
                } else existingFile
            } else newFile

        }
    }
}