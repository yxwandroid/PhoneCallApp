package com.ajiew.phonecallapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import com.ajiew.phonecallapp.entity.CallLogEntity;

import java.util.LinkedList;
import java.util.List;

public class CallLogUtil {

    public static List<CallLogEntity> queryLog(Context context, long startTime, long endTime, String phone, int limit) throws SecurityException {
        List<CallLogEntity> list = new LinkedList<>();
        ContentResolver contentResolver = context.getContentResolver();
        String selection = "";
        String[] selectionArgs = null;
        if (OtherUtils.isEmpty(phone)) {
            selection = " date > ? and date < ? ";
            selectionArgs = new String[]{String.valueOf(startTime), String.valueOf(endTime)};
        } else {
            selection = " number = ? and date > ? and date < ? ";
            selectionArgs = new String[]{phone, String.valueOf(startTime), String.valueOf(endTime)};
        }
        Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                new String[]{
                        "_id",
                        "name",  //姓名
                        "number",    //号码
                        "type",  //呼入/呼出(2)/未接
                        "date",  //拨打时间
                        "duration",   //通话时长
                }, selection, selectionArgs, " date desc limit " + limit);
        if (!OtherUtils.isEmpty(cursor)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String number = cursor.getString(cursor.getColumnIndex("number"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                long duration = cursor.getLong(cursor.getColumnIndex("duration"));
                CallLogEntity callLogEntity = new CallLogEntity();
                callLogEntity.setId(id);
                callLogEntity.setName(name);
                callLogEntity.setPhoneNumber(number);
                callLogEntity.setType(type);
                callLogEntity.setDate(date);
                callLogEntity.setDuration(duration);
                list.add(callLogEntity);
            }
            cursor.close();
        }
        return list;
    }

    public static List<CallLogEntity> queryLog(Context context, long callTime) throws SecurityException {
        List<CallLogEntity> list = new LinkedList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                new String[]{
                        "_id",
                        "name",  //姓名
                        "number",    //号码
                        "type",  //呼入/呼出(2)/未接
                        "date",  //拨打时间
                        "duration",   //通话时长
                }, "date > ?", new String[]{String.valueOf(callTime)}, "date asc");
        if (!OtherUtils.isEmpty(cursor)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String number = cursor.getString(cursor.getColumnIndex("number"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                long duration = cursor.getLong(cursor.getColumnIndex("duration"));
                CallLogEntity callLogEntity = new CallLogEntity();
                callLogEntity.setId(id);
                callLogEntity.setName(name);
                callLogEntity.setPhoneNumber(number);
                callLogEntity.setType(type);
                callLogEntity.setDate(date);
                callLogEntity.setDuration(duration);
                list.add(callLogEntity);
            }
            cursor.close();
        }
        return list;
    }

    public static CallLogEntity queryLog(Context context, String phone) throws SecurityException {
        ContentResolver contentResolver = context.getContentResolver();

        String[] projection = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        String selection = CallLog.Calls.NUMBER + " = ? ";

        String[] selectionArgs = new String[]{phone};

        String sortOrder = "date desc limit 1";

        try (Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, sortOrder)) {
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String number = cursor.getString(cursor.getColumnIndex("number"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                long duration = cursor.getLong(cursor.getColumnIndex("duration"));
                CallLogEntity callLogEntity = new CallLogEntity();
                callLogEntity.setId(id);
                callLogEntity.setName(name);
                callLogEntity.setPhoneNumber(number);
                callLogEntity.setType(type);
                callLogEntity.setDate(date);
                callLogEntity.setDuration(duration);
                return callLogEntity;
            }
            return null;
        }
    }

//    /**
//     * 拒绝接听
//     * service call phone 3
//     */
//    public static void rejectCall() {
//        try {
//            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
//            IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
//            ITelephony telephony = ITelephony.Stub.asInterface(binder);
//            telephony.endCall();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
