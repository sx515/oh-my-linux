/*
 *     Copyright (C) 2015  Joker
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.kiva.ohmylinux.utils;

import android.util.Log;

/**
 * @author Kiva
 * @date 2015/12/8
 */
public class L {
    public interface LogCallback {
        void onE(String errorMsg);

        void onD(String debugMsg);

        void onI(String infoMsg);

        void onW(String warnMsg);
    }

    public static final String TAG = "Oh-My-Linux";
    private static LogCallback logCallback;

    public static void setLogCallback(LogCallback cb) {
        logCallback = cb;
    }

    public static void d(String msg) {
        Log.d(TAG, msg);

        if (logCallback != null)
            logCallback.onD(msg);
    }

    public static void i(String msg) {
        Log.d(TAG, msg);

        if (logCallback != null)
            logCallback.onI(msg);
    }

    public static void e(Exception e) {
        e(e.toString());
    }

    public static void e(String msg) {
        Log.e(TAG, msg);

        if (logCallback != null)
            logCallback.onE(msg);
    }

    public static void w(Exception e) {
        w(e.toString());
    }

    public static void w(String msg) {
        Log.w(TAG, msg);

        if (logCallback != null)
            logCallback.onW(msg);
    }
}
