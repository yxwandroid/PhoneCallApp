package com.ajiew.phonecallapp.phonecallui;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.telecom.InCallService;

import com.ajiew.phonecallapp.ActivityStack;
import com.orhanobut.logger.Logger;


/**
 * 监听电话通信状态的服务，实现该类的同时必须提供电话管理的 UI
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneCallService extends InCallService {

    private Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            super.onStateChanged(call, state);

            Logger.d("wilson PhoneCallService Call  " + call.toString());
            switch (state) {
                case Call.STATE_ACTIVE: {
                    //积极支持对话时的状态 Call 建立连接
                    Logger.d("wilson PhoneCallService STATE_ACTIVE 开始通话阶段");
                    break;
                }
                case Call.STATE_DIALING: {
                    //拨打远程号码时，拨出 Call的状态，但尚未连接。
                    Logger.d("wilson PhoneCallService STATE_DIALING 响铃阶段");
                    break;
                }
                case Call.STATE_DISCONNECTED: {
                    Logger.d("wilson PhoneCallService STATE_DISCONNECTED 断开通话阶段");
                    //断开连接状态
                    ActivityStack.getInstance().finishActivity(PhoneCallActivity.class);
                    break;
                }

            }
        }
    };

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Logger.d("wilson PhoneCallService onCallAdded  ");
        call.registerCallback(callback);
        PhoneCallManager.call = call;

        CallType callType = null;

        if (call.getState() == Call.STATE_RINGING) {
            callType = CallType.CALL_IN;
        } else if (call.getState() == Call.STATE_CONNECTING) {
            callType = CallType.CALL_OUT;
        }

        if (callType != null) {
            Call.Details details = call.getDetails();
            String phoneNumber = details.getHandle().getSchemeSpecificPart();
            PhoneCallActivity.actionStart(this, phoneNumber, callType);
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Logger.d("wilson PhoneCallService onCallRemoved  ");
        call.unregisterCallback(callback);
        PhoneCallManager.call = null;
    }

    public enum CallType {
        CALL_IN,
        CALL_OUT,
    }
}
