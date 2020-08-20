package com.ajiew.phonecallapp.utils

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import java.text.DecimalFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object FileUtil {

    @JvmStatic
    @JvmOverloads
    fun readAll(fileName: String, charset: String = "utf-8"): String? {
        return readAll(File(fileName), charset)
    }

    @JvmStatic
    @JvmOverloads
    fun readAll(file: File, charset: String = "utf-8"): String? {

        if (!file.exists() || file.isDirectory) {
            return null
        }

        try {
            FileInputStream(file).use { fileInputStream ->
                val inputStreamReader = InputStreamReader(fileInputStream, charset)
                return inputStreamReader.readText()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    @JvmStatic
    @JvmOverloads
    fun readAll(url: URL, charset: String = "utf-8"): String? {
        try {
            url.openStream().use {
                val inputStreamReader = InputStreamReader(it, charset)
                return inputStreamReader.readText()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }

    @JvmStatic
    fun saveTo(fileName: String, content: String) {
        saveTo(File(fileName), content)
    }

    @JvmStatic
    fun saveTo(fileName: String, content: String, charset: String) {
        saveTo(File(fileName), content, charset)
    }

    @JvmStatic
    fun saveTo(fileName: String, bytes: ByteArray) {
        saveTo(File(fileName), bytes)
    }

    @JvmStatic
    fun saveTo(file: File, content: String) {
        saveTo(file, content.toByteArray(charset("utf-8")))
    }

    @JvmStatic
    fun saveTo(file: File, content: String, charset: String) {
        saveTo(file, content.toByteArray(charset(charset)))
    }

    @JvmStatic
    fun saveTo(file: File, bytes: ByteArray) {
        PathUtil.mkdirs(file)
        FileOutputStream(file).use { fileOutputStream ->
            fileOutputStream.write(bytes)
            fileOutputStream.flush()
        }
    }

    @JvmStatic
    fun saveTo(fileName: String, url: URL) {
        saveTo(File(fileName), url)
    }


    @JvmStatic
    fun saveTo(file: File, url: URL) {
        var connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        val code = connection.responseCode
        if (code in 300..307 && code != 306 && code != 304) {
            val location = connection.getHeaderField("Location")
            val redirectUrl = URL(location)
            connection = redirectUrl.openConnection() as HttpURLConnection
            if (location.startsWith("https")) {
                val httpsConnection = connection as HttpsURLConnection
                trustAllHosts(httpsConnection)
                //设置不验证主机 
                httpsConnection.setHostnameVerifier { hostname, session -> true }
            }
        }

        connection.inputStream.use { stream -> saveTo(file, stream) }
    }

    /**
     * 覆盖java默认的证书验证
     */
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
    })


    /**
     *     * 信任所有
     *     * @param connection
     *     * @return
     *    
     */
    private fun trustAllHosts(connection: HttpsURLConnection) {
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            val newFactory = sc.socketFactory
            connection.sslSocketFactory = newFactory
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    @JvmStatic
    fun saveTo(fileName: String, stream: InputStream) {
        saveTo(File(fileName), stream)
    }

    @JvmStatic
    fun saveTo(file: File, stream: InputStream) {
        val buffer = ByteArray(1024 * 16) // 16KB
        PathUtil.mkdirs(file)
        FileOutputStream(file).use {
            while (true) {
                val len = stream.read(buffer)
                if (len <= 0) break
                it.write(buffer, 0, len)
            }
            it.flush()
        }
    }

    @JvmStatic
    @Synchronized
    fun appendLineTo(fileName: String, content: String) {
        PathUtil.mkdirs(fileName)
        FileWriter(fileName, true).use {
            it.appendln(content)
        }
    }

    @JvmStatic
    fun saveToTmp(inputStream: InputStream, name: String, ext: String = "tmp"): File {
        val tmpFile = File.createTempFile(name, ".$ext")
        tmpFile.deleteOnExit()

        FileOutputStream(tmpFile).use { fos ->
            val bytes = ByteArray(1024 * 100)
            var read = inputStream.read(bytes)
            while (read != -1) {
                fos.write(bytes, 0, read)
                read = inputStream.read(bytes)
            }

            fos.flush()
            return tmpFile
        }
    }


    @JvmStatic
    fun saveToTmp(bytes: ByteArray, name: String, ext: String = "tmp"): File {
        val tmpFile = File.createTempFile(name, ".$ext")
        tmpFile.deleteOnExit()
        FileOutputStream(tmpFile).use { fos ->
            fos.write(bytes)
            fos.flush()
            return tmpFile
        }
    }


    /**
     * 对临文件夹和文件夹下的文件进行删除
     */
    @JvmStatic
    fun delete(delpath: String) {

        if (delpath.isNullOrBlank()) {
            return
        }

        try {
            val file = File(delpath)
            if (!file.exists()) {
                return
            }

            if (!file.isDirectory) {
                file.delete()
            } else if (file.isDirectory) {
                val filelist = file.list()
                for (i in filelist!!.indices) {
                    val delfile = File(delpath + File.separator + filelist[i])
                    if (!delfile.isDirectory) {
                        delfile.delete()
                    } else if (delfile.isDirectory) {
                        delete(delpath + File.separator + filelist[i])
                    }
                }
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @JvmStatic
    fun delete(file: File) {
        try {
            if (!file.exists()) {
                return
            }

            if (!file.isDirectory) {
                file.delete()
            } else if (file.isDirectory) {
                val delpath = file.absolutePath
                val filelist = file.list()
                for (i in filelist!!.indices) {
                    val delfile = File(delpath + File.separator + filelist[i])
                    if (!delfile.isDirectory) {
                        delfile.delete()
                    } else if (delfile.isDirectory) {
                        delete(delpath + File.separator + filelist[i])
                    }
                }
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun exists(fileName: String): Boolean = File(fileName).exists()


    /**
     * 判断文件的编码格式

     * @param fileName :file
     * *
     * @return 文件编码格式
     * *
     * @throws Exception
     */
    @JvmStatic
    @Throws(Exception::class)
    fun getCharSetName(fileName: String): String {
        return getCharSetName(FileInputStream(fileName))
    }

    @JvmStatic
    @Throws(IOException::class)
    fun getCharSetName(stream: InputStream): String {
        val bin = BufferedInputStream(stream)
        val p = (bin.read() shl 8) + bin.read()
        val code: String

        code = when (p) {
            0xefbb -> "UTF-8"
            0xfffe -> "Unicode"
            0xfeff -> "UTF-16BE"
            else -> "GBK"
        }

        return code
    }


    @JvmStatic
    fun formatFileSize(size: Long): String {
        return if (size < 1024) {
            "${size}B"
        } else if (size >= 1024 && size < (1024 * 1024)) {
            "${decimalFormat.format(size / 1024.0)}KB"
        } else if (size >= (1024 * 1024) && size < (1024 * 1024 * 1024)) {
            "${decimalFormat.format(size / (1024 * 1024.0))}MB"
        } else if (size >= (1024 * 1024 * 1024) && size < (1024L * 1024 * 1024 * 1024)) {
            "${decimalFormat.format(size / (1024 * 1024 * 1024.0))}GB"
        } else {
            "size too lager..."
        }
    }


    private var decimalFormat = DecimalFormat("#.00")


    @JvmStatic
    fun recursiveFind(dir: File, fileName: String): File? {
        if (!dir.isDirectory) {
            throw IllegalArgumentException("dir must be a directory")
        }

        val files = dir.listFiles()
        files.sortByDescending { it.lastModified() }

        for (file in files) {
            if (file.isDirectory) {
                val f = recursiveFind(file, fileName)
                if (f != null) {
                    return f
                }
            } else if (file.name == fileName) {
                return file
            }
        }

        return null
    }

    @JvmStatic
    fun touch(file: File) = if (file.exists()) {
        file.setLastModified(System.currentTimeMillis())
    } else {
        PathUtil.mkdirs(file)
        file.createNewFile()
    }


    @JvmStatic
    fun touch(file: String) = touch(File(file))

    @JvmStatic
    fun getLastModified(file: String): Long = getLastModified(File(file))

    @JvmStatic
    fun getLastModified(file: File): Long = if (file.exists()) {
        file.lastModified()
    } else {
        0
    }

    @JvmStatic
    fun sort(fileList: List<File>): List<File> {
        Collections.sort(fileList, object : Comparator<File> {
            override fun compare(f1: File, f2: File): Int {
                val diff = f1.length() - f2.length()
                return if (diff > 0)
                    -1
                else if (diff == 0L)
                    0
                else
                    1//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }

            override fun equals(obj: Any?): Boolean {
                return true
            }
        })
        return fileList
    }

}