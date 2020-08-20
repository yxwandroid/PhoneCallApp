package com.ajiew.phonecallapp.utils;

import android.util.SparseArray;

import java.util.List;

public class OtherUtils {

    public static boolean isEmpty(Object obj) {
        try {
            if (obj == null) {
                return true;
            }
            String str = String.valueOf(obj);
            if (str.equals("null")) {
                return true;
            }
            if (str.equals("")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean isNotEmpty(Object obj) {
        try {
            if (obj == null) {
                return false;
            }
            String str = String.valueOf(obj).trim();
            if (str.equals("")) {
                return false;
            }
            if (str.equals("null")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T> boolean isListNotEmpty(List<T> list) {
        try {
            if (list == null) {
                return false;
            }
            if (list.size() <= 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T> boolean isSparseArrayNotEmpty(SparseArray<T> sparseArray) {
        try {
            if (sparseArray == null) {
                return false;
            }
            if (sparseArray.size() <= 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String isStrNullTo(String str, String str2) {
        return str != null ? str : str2;
    }
}
