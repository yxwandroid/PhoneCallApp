package com.android.service.main.phone.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.ajiew.phonecallapp.phone.receiver.CallRecordReceiver
import com.orhanobut.logger.Logger
import java.util.Date

abstract class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (intent.action == CallRecordReceiver.ACTION_OUT) {
            savedNumber = intent.extras!!.getString(CallRecordReceiver.EXTRA_PHONE_NUMBER)
        } else {
            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
            var number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            number="18518318385"
            savedNumber = number
            var state = 0

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_IDLE -> state = TelephonyManager.CALL_STATE_IDLE  //挂断
                TelephonyManager.EXTRA_STATE_OFFHOOK -> state = TelephonyManager.CALL_STATE_OFFHOOK // 接听
                TelephonyManager.EXTRA_STATE_RINGING -> state = TelephonyManager.CALL_STATE_RINGING //响铃
            }
            Logger.i("[PhoneRecord] $number intent.action: $action ")
            Logger.i("[PhoneRecord] $number  state $state ")
            onCallStateChanged(context, state, number)
        }

    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract fun onIncomingCallReceived(context: Context, number: String?, start: Date)

    protected abstract fun onIncomingCallAnswered(context: Context, number: String?, start: Date)

    protected abstract fun onIncomingCallEnded(context: Context, number: String?, start: Date, end: Date)

    protected abstract fun onOutgoingCallStarted(context: Context, number: String?, start: Date)

    protected abstract fun onOutgoingCallEnded(context: Context, number: String?, start: Date, end: Date)

    protected abstract fun onMissedCall(context: Context, number: String?, start: Date)

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                Logger.i("[PhoneRecord] $number" +"--onIncomingCallReceived-- "+TelephonyManager.CALL_STATE_RINGING )
                onIncomingCallReceived(context, number, callStartTime)
            }
            //正在通话
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    Logger.i("[PhoneRecord] $number" +"--onOutgoingCallStarted--"+TelephonyManager.CALL_STATE_OFFHOOK )
                    onOutgoingCallStarted(context, savedNumber, callStartTime)
                } else {
                    isIncoming = true
                    callStartTime = Date()
                    Logger.i("[PhoneRecord] $number" +"--onIncomingCallAnswered--"+TelephonyManager.CALL_STATE_OFFHOOK )
                    onIncomingCallAnswered(context, savedNumber, callStartTime)
                }
            //电话挂断
            TelephonyManager.CALL_STATE_IDLE ->
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    Logger.i("[PhoneRecord] $number" +"--onMissedCall--"+TelephonyManager.CALL_STATE_IDLE )
                    onMissedCall(context, savedNumber, callStartTime)
                } else if (isIncoming) {
                    Logger.i("[PhoneRecord] $number" +"--onIncomingCallEnded--"+TelephonyManager.CALL_STATE_IDLE )
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date())
                } else {
                    Logger.i("[PhoneRecord] $number" +"--onOutgoingCallEnded--"+TelephonyManager.CALL_STATE_IDLE )
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                }
        }
        lastState = state
    }

    companion object {
        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date = Date()
        private var isIncoming: Boolean = false
        private var savedNumber: String? =
            null  //because the passed incoming is only valid in ringing
    }
}