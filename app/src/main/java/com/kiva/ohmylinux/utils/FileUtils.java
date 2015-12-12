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
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Kiva
 * @date 2015/12/9
 */
public class FileUtils {

    /**
     * 得到 sdcard 路径
     *
     * @return sdcard路径
     */
    public static String getSdcardPath() {
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null) {
            return sdcard.getAbsolutePath();
        }

        return null;
    }

    /**
     * 新建文件夹
     *
     * @param file 文件夹
     * @return 是否成功
     */
    public static boolean mkdir(File file) {
        return file.isDirectory() || file.mkdirs();
    }

    /**
     * 新建文件
     *
     * @param file 文件
     * @return 是否成功
     */
    public static boolean touch(File file) {
        if (file.isFile()) {
            return true;
        }

        try {
            return file.createNewFile();
        } catch (Exception e) {
            L.e(e);
        }

        return false;
    }

    /**
     * 删除文件夹/文件
     *
     * @param file 文件或文件夹
     * @return 是否成功
     */
    public static boolean rmrf(File file) {
        if (!file.exists()) {
            return false;
        }

        if (file.isFile()) {
            return file.delete();
        } else if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                rmrf(f);
            }

            return file.delete();
        }

        return false;
    }

    /**
     * 写出文件
     *
     * @param file   文件
     * @param buffer 内容的buffer
     * @return 是否成功
     */
    public static boolean writeFile(File file, byte[] buffer) {
        if (!touch(file)) {
            return false;
        }

        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            os.write(buffer);

            return true;
        } catch (Exception e) {
            L.e(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    L.e(e);
                }
            }
        }

        return false;
    }

    /**
     * 从Uri里提取文件绝对路径
     *
     * @param context 上下文
     * @param uri     uri
     * @return 绝对路径
     */
    public static String getPathFromUri(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equalsIgnoreCase(uri.getAuthority())) {
                String docId = DocumentsContract.getDocumentId(uri);
                L.d(docId);
                String[] arr = docId.split(":");

                return getSdcardPath() + "/" + arr[1];
            }
        }

        return uri.getPath();
    }
}
