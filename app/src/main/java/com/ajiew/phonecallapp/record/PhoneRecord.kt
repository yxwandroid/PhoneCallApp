package com.ajiew.phonecallapp.record

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.ajiew.phonecallapp.entity.RecordEntity
import com.ajiew.phonecallapp.utils.CallLogUtil
import com.ajiew.phonecallapp.utils.FileUtil
import com.alibaba.fastjson.JSON
import com.orhanobut.logger.Logger
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

object PhoneRecord {
    private var audioFile: File? = null
    private var isRecordStarted = false
    private var recordEntity: RecordEntity? = null;
    private var mNumber: String? = null;
    private const val RECORD_PATH = "/sdcard/acWechat/WechatRecord"
    private var recorder: MediaRecorder? = null

    /**
     *seed  incoming
     */
    @JvmStatic
    fun startRecord(seed: String, phoneNumber: String?) {
        try {
            mNumber = phoneNumber;
            if (isRecordStarted) {
                try {
                    recorder?.stop()  // stop the recording
                } catch (e: RuntimeException) {
                    Logger.e(e, "[PhoneRecord] $phoneNumber RuntimeException: stop() is called immediately after start()")
                    audioFile?.delete()
                }
                releaseMediaRecorder()
                isRecordStarted = false
            } else {
                if (prepareAudioRecorder(seed, phoneNumber)) {
                    recorder?.start()
                    isRecordStarted = true
                    Logger.i("[PhoneRecord] $phoneNumber record start")
                    Logger.i("[PhoneRecord] $phoneNumber record audioFile %s", audioFile)
                } else {
                    releaseMediaRecorder()
                }
            }
        } catch (e: IllegalStateException) {
            Logger.e(e, "[PhoneRecord]  record start IllegalStateException")
            e.printStackTrace()
            releaseMediaRecorder()
        } catch (e: RuntimeException) {
            Logger.e(e, "[PhoneRecord]  record start RuntimeException")
            e.printStackTrace()
            releaseMediaRecorder()
        } catch (e: Exception) {
            Logger.e(e, "[PhoneRecord]  record start Exception")
            e.printStackTrace()
            releaseMediaRecorder()
        }
    }

    @JvmStatic
    fun stopRecord(context: Context) {
        Logger.i("[PhoneRecord] $mNumber stopRecord")
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder()
                isRecordStarted = false

                var endRecordTime = System.currentTimeMillis()
                recordEntity?.endTime = endRecordTime;
                recordEntity?.createEndFileName()
                var outPutFile = recordEntity?.endFilePath;
                Logger.i("[PhoneRecord] $mNumber stopRecord $outPutFile")
                thread {
                    try {
                        var callLog = CallLogUtil.queryLog(context, recordEntity?.phoneNumber)
                        val callLogContent = JSON.toJSONString(callLog)

                        Logger.i("[PhoneRecord] $mNumber stopRecord 查询通过记录 $callLogContent")

                        FileUtil.saveTo("$RECORD_PATH/${callLog.id}.json", callLogContent)

                        outPutFile = outPutFile?.replace("callLogId", callLog.id.toString())

                        Logger.i("[PhoneRecord] $mNumber stopRecord  outPutFile$outPutFile")

                        audioFile?.renameTo(File(outPutFile))
                    } catch (e: Throwable) {
                        Logger.e(e, "[PhoneRecord] $mNumber stopRecord query calllog error: %s", this)
                    }
                }
            }
        } catch (e: Exception) {
            releaseMediaRecorder()
            e.printStackTrace()
            Logger.e(e, "[PhoneRecord] $mNumber stopRecord  e $e")
        }
    }


    //创建录音文件路径
    private fun createRecordFilePath(seed: String, phoneNumber: String): String {
        var dirPath = RECORD_PATH;
        val sampleDir = File(dirPath)
        if (!sampleDir.exists()) {
            sampleDir.mkdirs()
        }
        var type: Int = if (seed.startsWith("incoming")) {
            1
        } else {
            0
        }
        var recordFileName = phoneNumber + "_beginRecordTime_" + type + "_endRecordTime_callLogId.amr"
        var recordFilePath = "$dirPath/$recordFileName"

        Logger.i("[PhoneRecord] $phoneNumber createRecordFilePath 存储文件名:${recordFileName}")
        Logger.i("[PhoneRecord] $phoneNumber createRecordFilePath 存储文件路径:${recordFilePath}")
        return recordFilePath;
    }

    private fun prepareAudioRecorder(seed: String, phoneNumber: String?): Boolean {
        try {

            var filePath = phoneNumber?.let { createRecordFilePath(seed, it) };
            audioFile = File(filePath)

            var beginRecordTime = System.currentTimeMillis()
            recordEntity = RecordEntity();
            recordEntity?.phoneNumber = phoneNumber;
            recordEntity?.startTime = beginRecordTime;
            recordEntity?.seed = seed;
            recordEntity?.startFilePath = filePath

            Logger.i("[PhoneRecord] $phoneNumber prepareAudioRecorder 录音信息:${recordEntity.toString()}")

            recorder = MediaRecorder()
            recorder?.apply {
                val audioSource = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    MediaRecorder.AudioSource.MIC
                } else {
                    MediaRecorder.AudioSource.VOICE_CALL
                }
                setAudioSource(audioSource)
//                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile!!.absolutePath)
                setOnErrorListener { _, _, _ -> }
            }

            try {
                recorder?.prepare()
            } catch (e: IllegalStateException) {
                Logger.e(e, "[PhoneRecord] $phoneNumber IllegalStateException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            } catch (e: IOException) {
                Logger.e(e, "[PhoneRecord] $phoneNumber IOException preparing MediaRecorder: " + e.message)
                releaseMediaRecorder()
                return false
            }

            return true
        } catch (e: Exception) {
            Logger.e(e, "[PhoneRecord] $phoneNumber IOException preparing MediaRecorder: " + e.message)
            e.printStackTrace()
            return false
        }
    }

    private fun releaseMediaRecorder() {
        recorder?.apply {
            reset()
            release()
        }
        recorder = null
    }


}
