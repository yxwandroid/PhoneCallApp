package com.ajiew.phonecallapp.record;

import android.media.MediaRecorder;
import android.os.Build;

import com.ajiew.phonecallapp.utils.FileUtil;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;


public class PhoneRecord2 {
    private MediaRecorder mMediaRecorder;
    private String filePath;

    private PhoneRecord2() {
    }


    public static PhoneRecord2 instance = new PhoneRecord2();

    public void startRecord() {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
//
//            val audioSource = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                MediaRecorder.AudioSource.MIC
//            } else {
//                MediaRecorder.AudioSource.VOICE_CALL
//            }
            int audioSource;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                audioSource = MediaRecorder.AudioSource.MIC;
            } else {
                audioSource = MediaRecorder.AudioSource.VOICE_CALL;
            }
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(audioSource);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            filePath = "/sdcard/acWechat/WechatRecord/22.amr";
            FileUtil.touch(filePath);
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Logger.e("call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        } catch (IOException e) {
            Logger.e("call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        }
    }

    public void stopRecord() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            filePath = "";
        } catch (RuntimeException e) {
            Logger.e(e.toString());
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;

            File file = new File(filePath);
            if (file.exists())
                file.delete();

            filePath = "";
        }
    }
}
