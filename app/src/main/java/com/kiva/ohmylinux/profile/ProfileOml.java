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

package com.kiva.ohmylinux.profile;

import android.content.Context;

import bsh.Interpreter;

/**
 * @author Kiva
 * @date 2015/12/10
 */
public interface ProfileOml {
    /**
     * 配置被解析的时候，此方法被调用
     *
     * @param profile 被解析的配置文件
     */
    void onSetup(LinuxProfile profile);

    /**
     * 配置被展示给用户看的时候，此方法被调用
     *
     * @param context 上下文
     * @param profile 被展示的配置文件
     */
    void onShow(Context context, LinuxProfile profile);

    /**
     * 配置被启动前，此方法被调用
     *
     * @param context 上下文
     * @param profile 将要启动的配置文件
     */
    void onLaunch(Context context, LinuxProfile profile);

    /**
     * 配置成功启动后，此方法被调用
     *
     * @param context 上下文
     * @param profile 启动成功的配置文件
     */
    void onPostLaunch(Context context, LinuxProfile profile);

    Interpreter getInterpreter();
}
