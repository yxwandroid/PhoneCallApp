package com.ajiew.phonecallapp.utils

import java.io.File

/**
 * Created by Administrator on 2018\5\22 0022.
 */
object PathUtil {

    @JvmStatic
    fun combine(vararg paths: String): String {

        val sb = StringBuilder()
        for (i in paths.indices) {
            var p = paths[i]

            val notLast = i < paths.size - 1
            val notFirst = i != 0

            if (notFirst && p.startsWith(File.separator)) {
                p = p.substring(1)
            }

            sb.append(p)

            if (notLast && !p.endsWith(File.separator)) {
                sb.append(File.separator)
            }
        }

        return sb.toString()

    }

    @JvmStatic
    fun mkdirs(path: String, isDirectory: Boolean = false) {
        val file = if (isDirectory) {
            File(combine(path, "1.tmp"))
        } else {
            File(path)
        }
        mkdirs(file)
    }

    @JvmStatic
    fun mkdirs(file: File) {
        val parent = file.parent
        if (parent != null) {
            val p = File(parent)
            if (!p.exists()) {
                p.mkdirs()
            }
        }
    }


}