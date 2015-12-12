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
import android.content.Intent;

import com.kiva.ohmylinux.profile.LinuxProfile;

import java.io.File;

/**
 * @author Kiva
 * @date 2015/12/11
 */
public class LaunchUtils {

    /**
     * 统一管理bash的路径
     *
     * @param context 上下文
     * @return bash的文件对象
     */
    public static File getBash(Context context) {
        return new File(context.getFilesDir(), "bash");
    }

    /**
     * 统一管理busybox的路径
     *
     * @param context 上下文
     * @return busybox的文件对象
     */
    public static File getBusybox(Context context) {
        return new File(context.getFilesDir(), "busybox");
    }

    /**
     * 统一管理启动脚本的路径
     *
     * @param context 上下文
     * @return 启动脚本的文件对象
     */
    public static File getLinux(Context context) {
        return new File(context.getFilesDir(), "linux");
    }

    /**
     * 转义字符串使其可以直接作为shell的参数
     * hello => "hello"
     * $@ => \$@
     *
     * @param s 需要被引号的字符串
     * @return 可以作为shell参数的字符串
     */
    public static String quoteStringForShell(String s) {
        StringBuilder builder = new StringBuilder();
        String specialChars = "\"\\$`!";
        builder.append('"');
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\');
            }
            builder.append(c);
        }
        builder.append('"');
        return builder.toString();
    }

    /**
     * 为配置文件创建启动的命令
     *
     * @param context 上下文
     * @param profile 配置文件
     * @return 启动的命令
     */
    public static String createTerminalCommand(Context context, LinuxProfile profile) {
        StringBuilder builder = new StringBuilder();

        builder.append(getBash(context).getAbsolutePath());
        builder.append(" ");
        builder.append(quoteStringForShell(getLinux(context).getAbsolutePath()));
        builder.append(" busybox=");
        builder.append(quoteStringForShell(getBusybox(context).getAbsolutePath()));
        builder.append(profile.toCommandlineOption());

        return builder.toString();
    }

    /**
     * 创建一个打开终端并且执行指定命令的 Intent
     *
     * @param initCommand 执行的命令
     * @return 打开终端的Intent
     */
    public static Intent createTerminalIntent(String initCommand) {
        Intent i = new Intent();
        i.setAction("jackpal.androidterm.RUN_SCRIPT");
        i.setClassName("com.romide.terminal", "com.romide.terminal.activity.RemoteInterface");
        i.putExtra("jackpal.androidterm.iInitialCommand", initCommand);
        return i;
    }
}
