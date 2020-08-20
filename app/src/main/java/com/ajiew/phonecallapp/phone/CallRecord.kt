package com.android.service.main.phone

import android.content.Context
import android.content.IntentFilter
import com.ajiew.phonecallapp.phone.receiver.CallRecordReceiver
import com.orhanobut.logger.Logger

/**
 * 通过记录广播
 */
class CallRecord(private val mContext: Context) {
    private var mCallRecordReceiver: CallRecordReceiver? = null

    fun startCallReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(CallRecordReceiver.ACTION_IN)
        intentFilter.addAction(CallRecordReceiver.ACTION_OUT)

        if (mCallRecordReceiver == null) {
            mCallRecordReceiver = CallRecordReceiver(this)
        }
        mContext.registerReceiver(mCallRecordReceiver, intentFilter)
    }

    fun stopCallReceiver() {
        try {
            if (mCallRecordReceiver != null) {
                mContext.unregisterReceiver(mCallRecordReceiver)
            }
        } catch (e: Exception) {
            Logger.e(e.toString())
        }
    }

    class Builder(private val mContext: Context) {
        fun build(): CallRecord {
            return CallRecord(mContext)
        }

    }

    companion object {
        private val TAG = CallRecord::class.java.simpleName

    }
}
