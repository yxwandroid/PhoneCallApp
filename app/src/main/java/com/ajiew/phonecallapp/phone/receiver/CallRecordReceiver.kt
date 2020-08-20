package com.ajiew.phonecallapp.phone.receiver

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.ajiew.phonecallapp.utils.CallLogUtil
import com.alibaba.fastjson.JSON
import com.android.service.main.phone.RecordEntity
import com.android.service.main.phone.CallRecord
import com.android.service.main.phone.receiver.PhoneCallReceiver
import com.ajiew.phonecallapp.utils.FileUtil
import com.orhanobut.logger.Logger
import java.io.File
import java.io.IOException
import java.util.Date
import kotlin.concurrent.thread

open class CallRecordReceiver(private var callRecord: CallRecord) : PhoneCallReceiver() {

    companion object {
        const val ACTION_IN = "android.intent.action.PHONE_STATE"
        const val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        const val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"
        private var recorder: MediaRecorder? = null
    }

    private var audioFile: File? = null
    private var isRecordStarted = false
    private var recordEntity: RecordEntity? = null;
    private var mNumber: String? = null;
    private var outputFile: String? = null;
    private val RECORD_PATH = "/sdcard/acWechat/WechatRecord"

    override fun onIncomingCallReceived(context: Context, number: String?, start: Date) {
    }

    override fun onIncomingCallAnswered(context: Context, number: String?, start: Date) {
        startRecord(context, "incoming", number)
    }

    override fun onIncomingCallEnded(context: Context, number: String?, start: Date, end: Date) {
        stopRecord(context)
    }

    override fun onOutgoingCallStarted(context: Context, number: String?, start: Date) {
        startRecord(context, "outgoing", number)
    }

    override fun onOutgoingCallEnded(context: Context, number: String?, start: Date, end: Date) {
        stopRecord(context)
    }

    override fun onMissedCall(context: Context, number: String?, start: Date) {
    }

    // Derived classes could override these to respond to specific events of interest
    protected open fun onRecordingStarted(context: Context, callRecord: CallRecord, audioFile: File?) {}

    protected open fun onRecordingFinished(context: Context, callRecord: CallRecord, audioFile: File?) {}

    /**
     *seed  incoming
     */
    private fun startRecord(context: Context, seed: String, phoneNumber: String?) {
        try {
            if (isRecordStarted) {
                try {
                    recorder?.stop()  // stop the recording
                } catch (e: RuntimeException) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
                    Logger.e(e, "[PhoneRecord] $phoneNumber RuntimeException: stop() is called immediately after start()")
                    audioFile?.delete()
                }

                releaseMediaRecorder()
                isRecordStarted = false
            } else {
                if (prepareAudioRecorder(context, seed, phoneNumber)) {
                    recorder!!.start()
                    isRecordStarted = true
                    onRecordingStarted(context, callRecord, audioFile)
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

    private fun stopRecord(context: Context) {
        Logger.i("[PhoneRecord] $mNumber stopRecord")
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder()
                isRecordStarted = false
                onRecordingFinished(context, callRecord, audioFile)

                var endRecordTime = System.currentTimeMillis()
                recordEntity?.endTime = endRecordTime;
                recordEntity?.createEndFileName()
                var outPutFile = recordEntity?.endFilePath;
                Logger.i("[PhoneRecord] $mNumber stopRecord $outPutFile")
                thread {
                    try {
                        var callLog = CallLogUtil.queryLog(context, this.recordEntity?.phoneNumber)
                        val callLogContent = JSON.toJSONString(callLog)

                        Logger.i("[PhoneRecord] $mNumber stopRecord 查询通过记录 $callLogContent")

                        FileUtil.saveTo("${RECORD_PATH}/${callLog.id}.json", callLogContent)

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
            0;
        } else {
            1
        }
        var recordFileName = phoneNumber + "_beginRecordTime_" + type + "_endRecordTime_callLogId.amr"
        var recordFilePath = "$dirPath/$recordFileName"

        Logger.i("[PhoneRecord] $phoneNumber createRecordFilePath 存储文件名:${recordFileName}")
        Logger.i("[PhoneRecord] $phoneNumber createRecordFilePath 存储文件路径:${recordFilePath}")
        return recordFilePath;
    }

    private fun prepareAudioRecorder(
            context: Context, seed: String, phoneNumber: String?
    ): Boolean {
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
//                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
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
