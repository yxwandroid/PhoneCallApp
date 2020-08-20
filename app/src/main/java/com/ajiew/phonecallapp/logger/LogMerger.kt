package com.ajiew.phonecallapp.logger

import android.os.Build
import android.support.annotation.RequiresApi
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Throws

private val EMPTY = Line()

class LogMerger(logRootDir: String) {

    private val logRootDirFile: File = File(logRootDir)

    init {
        if (!logRootDirFile.exists()) {
            throw IOException("$logRootDir not exists")
        }

        if (!logRootDirFile.isDirectory) {
            throw IOException(logRootDir + "not a directory")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun merge(saveDir: String, fileNames: List<String>): List<File> {

        val mergeFiles = arrayListOf<File>()
        val map = collectNeedMergeFiles(logRootDirFile, fileNames)
//        val notEmptyLogFiles = ArrayList<LogFile>()

        for ((key, listLogFiles) in map) {  //迭代文件集合
            val mergePath = File("$saveDir/$key")
            val mergeFile = WritableLogFile(mergePath)//输出位置
            mergeFiles.add(mergePath)

            val lines = ArrayList<Line>(listLogFiles.size)
            for (logFile in listLogFiles) {
                lines.add(logFile.readLine())
            }

            // 上一轮没有合并完的也要参与到这个文件的合并
//            for (logFile in notEmptyLogFiles) {
//                lines.add(logFile.readLine())
//            }

            while (true) {

//                val needToRemoveLines = ArrayList<Line>()

                for (i in lines.indices) {
//                    if (i < listLogFiles.size) { // 列表中前半部分保存的是这一次需要处理的文件的行
                        if (lines[i] === EMPTY) { //
                            lines[i] = listLogFiles[i].readLine()
                        }
//                    } else { // 后半部分保存的是上一次没有处理完毕的文件的行
//                        if (lines[i] === EMPTY) {
//                            val idx =  i - listLogFiles.size - ()
//                            lines[i] = notEmptyLogFiles[idx].readLine()
//                            if (lines[i] === EMPTY) {
//                                notEmptyLogFiles.remove(listLogFiles[i])
//                                needToRemoveLines.add(lines[i])
//                            }
//                        }
//                    }
                }

//                lines.removeAll(needToRemoveLines)

//                if (lines.any { it === EMPTY }) { // 任何为空的说明
//                    for (i in lines.indices) {
//                        if (lines[i] !== EMPTY) {
//                            notEmptyLogFiles.add(listLogFiles[i])
//                        }
//                    }
//                    mergeFile.close()//最小的为空 证明文件读完
//                    break
//                }


                val minLine = Collections.min(lines)//取最小值
                if (minLine === EMPTY) {
                    mergeFile.close()//最小的为空 证明文件读完
                    break
                }

                val context = minLine.content
                mergeFile.writeln(context)//输入
                lines[minLine.index] = EMPTY
            }
        }

        return mergeFiles

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun collectNeedMergeFiles(logRootDirFile: File, fileNames: List<String>): Map<String, List<LogFile>> {
        val map = HashMap<String, List<LogFile>>()
        // 获取指定目录下的所有目录
        val filterDirs = logRootDirFile.listFiles { it -> it.isDirectory }
                ?: return map

        //循环所有文件夹
        for (file in filterDirs) {

            // 收集符合要求的文件
            val files = file.listFiles { it ->
                if (it.isDirectory) {
                    false
                } else {
                    fileNames.any { name ->
                        val suffix = name.substring(0, 10)
                        it.name.startsWith(suffix)
                    }
                }
            } ?: continue

            for (it in files) {
                val sameFileNameFiles = map.computeIfAbsent(it.name) { ArrayList() } as ArrayList
                val logFile = LogFile(it.path, sameFileNameFiles.size)
                sameFileNameFiles.add(logFile)
            }
        }
        return map
    }


}

private class LogFile(filePath: String, var index: Int) {

    var bufferedReader01: BufferedReader? = null
    var isEmpty: Boolean = false

    init {
        val inputStream01 = FileInputStream(filePath)
        bufferedReader01 = BufferedReader(InputStreamReader(inputStream01, "UTF8"))
    }

    fun readLine(): Line {
        if (isEmpty) {
            return EMPTY
        }
        val lineStr = bufferedReader01!!.readLine()
        if (lineStr == null) {
            bufferedReader01!!.close()
            isEmpty = true
            return EMPTY
        }
        val line = Line()
        line.content = lineStr
        line.date = parseDate(lineStr)
        line.index = index
        return line
    }

    private fun parseDate(a: String): Long {
        return try {
            val s1 = a.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS")
            format.parse(s1[1]).time
        } catch (e: Throwable) {
            0L
        }

    }
}

private class WritableLogFile(csvFile: File) : Closeable {
    var bw: BufferedWriter? = null

    init {
        if (!csvFile.parentFile.exists()) {
            csvFile.parentFile.mkdirs()
        }
        bw = BufferedWriter(FileWriter(csvFile, false)) // 附加
    }

    @Throws(IOException::class)
    fun writeln(content: String?) {
        // 添加新的数据行
        bw!!.write(content)
        bw!!.newLine()
    }

    override fun close() {
        if (bw != null) {
            try {
                bw!!.flush()
                bw!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}

private class Line : Comparable<Line> {
    var date = java.lang.Long.MAX_VALUE
    var content: String? = null
    var index: Int = 0

    override fun compareTo(o: Line): Int {
        return when {
            date > o.date -> 1
            date < o.date -> -1
            else -> 0
        }
    }
}