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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.InputStream;

/**
 * @author Kiva
 * @date 2015/12/11
 */
public class ResourceUtils {

    /**
     * 检查当前的资源版本是否是最新的版本
     *
     * @param context 上下文
     * @return 是否最新
     */
    private static boolean checkResourcesVersion(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int resVersion = pref.getInt("resVersion", -1);
        int appVersionCode = AppUtils.getAppVersionCode(context);

        if (resVersion < appVersionCode) {
            pref.edit().putInt("resVersion", appVersionCode).commit();
            return false;
        }

        return true;
    }

    /**
     * 从assets文件夹读取内容
     *
     * @param context  上下文
     * @param fileName 文件名
     * @return 文件的buffer
     */
    public static byte[] readAssetsFile(Context context, String fileName) {
        AssetManager mgr = context.getAssets();
        InputStream is = null;
        byte[] buffer = null;

        try {
            is = mgr.open(fileName);
            buffer = new byte[is.available()];
            is.read(buffer);
        } catch (Exception e) {
            L.e(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    L.e(e);
                }
            }
        }

        return buffer;
    }

    /**
     * 初始化启动需要的资源文件， bash busybox 等等
     *
     * @param context 上下文
     */
    public static void initResources(Context context) {
        if (checkResourcesVersion(context)) {
            return;
        }

        byte[] bashBuffer = readAssetsFile(context, "bash");
        byte[] boxBuffer = readAssetsFile(context, "busybox");
        byte[] linuxBuffer = readAssetsFile(context, "linux");

        File bash = LaunchUtils.getBash(context);
        File box = LaunchUtils.getBusybox(context);
        File linux = LaunchUtils.getLinux(context);

        FileUtils.writeFile(bash, bashBuffer);
        FileUtils.writeFile(box, boxBuffer);
        FileUtils.writeFile(linux, linuxBuffer);

        try {
            Runtime.getRuntime().exec("chmod 755 " + bash.getAbsolutePath() + " " + box.getAbsolutePath());
        } catch (Exception e) {
            L.e(e);
        }
    }
}
