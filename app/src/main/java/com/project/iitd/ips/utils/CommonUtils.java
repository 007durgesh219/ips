package com.project.iitd.ips.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by durgesh on 16/11/16.
 */

public class CommonUtils {
    public static boolean checkPermissions(Context context, String... permissions) {
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        for (String permission : permissions) {
            if (!checkPermission(context, permission)) {
                return false;
            }
        }*/
        return true;
    }

    public static boolean checkPermission(Context context, String permission) {
        return true;//ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void toast(Context context, String msg) {
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    public static void log(String msg) {
        Log.d("Debug Log: ", msg);
    }
}
