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
import android.text.TextUtils;

import com.kiva.ohmylinux.utils.L;
import com.kiva.ohmylinux.utils.LaunchUtils;

import java.io.File;

import bsh.BshMethod;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.UtilEvalError;


/**
 * @author Kiva
 * @date 2015/12/8
 */

public class LinuxProfile {

    /**
     * 内部对配置文件生命周期管理的实现
     */
    private class ProfileOmlImpl implements ProfileOml {
        Interpreter interpreter;

        public ProfileOmlImpl() {
            interpreter = new Interpreter();
        }

        /**
         * 得到一个方法
         *
         * @param methodName 方法名
         * @param paraTypes  参数类型
         * @return 方法实现
         */
        private BshMethod getMethod(String methodName, Class[] paraTypes) {
            try {
                BshMethod bm = interpreter.getNameSpace()
                        .getMethod(methodName, paraTypes);
                if (bm != null) {
                    return bm;
                }
            } catch (UtilEvalError utilEvalError) {
                L.e(utilEvalError);
            }

            return null;
        }

        /**
         * 把对象转换成对象类类型
         *
         * @param args 需要转换的对象数组
         * @return 对象类类型
         */
        private Class[] getMethodArgumentClass(Object[] args) {
            if (args == null || args.length == 0) {
                return new Class[0];
            }

            Class[] classes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                classes[i] = args[i].getClass();
            }

            return classes;
        }

        /**
         * 调用一个方法
         *
         * @param methodName 方法名
         * @param args       参数
         * @throws EvalError
         */
        private void callMethod(String methodName, Object[] args) throws EvalError {
            if (TextUtils.isEmpty(methodName)) {
                return;
            }

            Class[] classes = getMethodArgumentClass(args);
            BshMethod method = getMethod(methodName, classes);

            if (method == null) {
                L.i("cannot find " + methodName + "() in " + nickName);
                return;
            }

            method.invoke(args, getInterpreter());
        }

        @Override
        public void onSetup(LinuxProfile profile) {
            try {
                callMethod("onSetup", new Object[]{profile});
            } catch (EvalError evalError) {
                L.e(evalError);
            }
        }

        @Override
        public void onShow(Context context, LinuxProfile profile) {
            try {
                callMethod("onShow", new Object[]{context, profile});
            } catch (EvalError evalError) {
                L.e(evalError);
            }
        }

        @Override
        public void onLaunch(Context context, LinuxProfile profile) {
            try {
                callMethod("onLaunch", new Object[]{context, profile});
            } catch (EvalError evalError) {
                L.e(evalError);
            }
        }

        @Override
        public void onPostLaunch(Context context, LinuxProfile profile) {
            try {
                callMethod("onPostLaunch", new Object[]{context, profile});
            } catch (EvalError evalError) {
                L.e(evalError);
            }
        }

        @Override
        public Interpreter getInterpreter() {
            return interpreter;
        }
    }

    private String nickName;

    private String devicePath;
    private String fileSystemType = "ext4";

    private String mountPoint;
    private String diskTableFile;
    private int waitSeconds = 10;
    private boolean mountSdcard0 = true;
    private boolean mountSdcard1 = true;

    private String initProgram;
    private String initProgramArgs;
    private String terminalInfo;
    private boolean alreadyInit;

    private ProfileOml oml;

    public LinuxProfile() {
        oml = new ProfileOmlImpl();
        oml.getInterpreter().getNameSpace().importClass(LinuxProfile.class.getName());
    }

    /**
     * 加载一个oml脚本
     *
     * @param fileName 脚本名
     */
    protected void loadProfile(String fileName) {
        if (!new File(fileName).isFile()) {
            L.w("cannot load profile " + fileName + "(no such file)");
            return;
        }

        try {
            oml.getInterpreter().source(fileName);
        } catch (Exception e) {
            L.e(e);
        }
    }

    private void appendOmlCommon(StringBuilder builder, String setterName) {
        builder.append("  profile.");
        builder.append(setterName);
    }

    private void appendOml(StringBuilder builder, String setterName, String value) {
        if (!TextUtils.isEmpty(value)) {
            appendOmlCommon(builder, setterName);
            builder.append("(\"");
            builder.append(value);
            builder.append("\");\n");
        }
    }

    private void appendOml(StringBuilder builder, String setterName, int value) {
        appendOmlCommon(builder, setterName);
        builder.append("(");
        builder.append(value);
        builder.append(");\n");
    }

    private void appendOml(StringBuilder builder, String setterName, boolean value) {
        appendOmlCommon(builder, setterName);
        builder.append("(");
        builder.append(value);
        builder.append(");\n");
    }

    /**
     * 转换成对应的oml脚本
     *
     * @return oml脚本代码
     */
    public String toOml() {
        StringBuilder builder = new StringBuilder();

        builder.append("void onSetup(profile) {\n");

        appendOml(builder, "setDevicePath", devicePath);
        appendOml(builder, "setFileSystemType", fileSystemType);
        appendOml(builder, "setMountPoint", mountPoint);
        appendOml(builder, "setDiskTableFile", diskTableFile);
        appendOml(builder, "setWaitSeconds", waitSeconds);
        appendOml(builder, "setMountSdcard0", mountSdcard0);
        appendOml(builder, "setMountSdcard1", mountSdcard1);
        appendOml(builder, "setInitProgram", initProgram);
        appendOml(builder, "setInitProgramArgs", initProgramArgs);
        appendOml(builder, "setTerminalInfo", terminalInfo);

        builder.append("}\n");

        return builder.toString();
    }

    /**
     * 转换成命令行参数
     *
     * @return 命令行参数
     */
    public String toCommandlineOption() {
        if (TextUtils.isEmpty(devicePath)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(" device=");
        builder.append(LaunchUtils.quoteStringForShell(getDevicePath()));

        builder.append(" mnt=");
        builder.append(LaunchUtils.quoteStringForShell(getMountPoint()));

        builder.append(" wait=");
        builder.append(getWaitSeconds());

        builder.append(" type=");
        builder.append(LaunchUtils.quoteStringForShell(getFileSystemType()));

        if (!TextUtils.isEmpty(diskTableFile)) {
            builder.append(" disktab=");
            builder.append(LaunchUtils.quoteStringForShell(diskTableFile));
        }

        if (!isMountSdcard0()) {
            builder.append(" --no-sdcard0");
        }

        if (!isMountSdcard1()) {
            builder.append(" --no-sdcard1");
        }

        builder.append(" --");

        builder.append(" init=");
        builder.append(LaunchUtils.quoteStringForShell(getInitProgram()));

        builder.append(" initarg=");
        builder.append(LaunchUtils.quoteStringForShell(getInitProgramArgs()));

        builder.append(" term=");
        builder.append(LaunchUtils.quoteStringForShell(getTerminalInfo()));

        if (alreadyInit) {
            builder.append(" already-init");
        }

        return builder.toString();
    }

    public String getNickName() {
        return nickName;
    }

    @SuppressWarnings("unused")
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFileSystemType() {
        if (TextUtils.isEmpty(fileSystemType)) {
            return "ext4";
        }

        return fileSystemType;
    }

    @SuppressWarnings("unused")
    public void setFileSystemType(String fileSystemType) {
        this.fileSystemType = fileSystemType;
    }

    public String getDevicePath() {
        return devicePath;
    }

    @SuppressWarnings("unused")
    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public String getMountPoint() {
        if (!TextUtils.isEmpty(mountPoint)) {
            return mountPoint;
        }

        File profileDir = ProfileManager.getInstance().getProfileDir();

        if (profileDir != null) {
            File mountPointDir = new File(profileDir.getAbsolutePath() + "/" + getNickName() + "/mnt");
            return mountPointDir.getAbsolutePath();
        }

        return "/data/local/linux-" + getNickName();
    }

    @SuppressWarnings("unused")
    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getDiskTableFile() {
        return diskTableFile;
    }

    @SuppressWarnings("unused")
    public void setDiskTableFile(String diskTableFile) {
        this.diskTableFile = diskTableFile;
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    @SuppressWarnings("unused")
    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    public boolean isMountSdcard0() {
        return mountSdcard0;
    }

    @SuppressWarnings("unused")
    public void setMountSdcard0(boolean mountSdcard0) {
        this.mountSdcard0 = mountSdcard0;
    }

    public boolean isMountSdcard1() {
        return mountSdcard1;
    }

    @SuppressWarnings("unused")
    public void setMountSdcard1(boolean mountSdcard1) {
        this.mountSdcard1 = mountSdcard1;
    }

    public String getInitProgram() {
        if (TextUtils.isEmpty(initProgram)) {
            return "/bin/bash";
        }

        return initProgram;
    }

    @SuppressWarnings("unused")
    public void setInitProgram(String initProgram) {
        this.initProgram = initProgram;
    }

    public String getInitProgramArgs() {
        if (TextUtils.isEmpty(initProgramArgs)) {
            return "--login";
        }

        return initProgramArgs;
    }

    @SuppressWarnings("unused")
    public void setInitProgramArgs(String initProgramArgs) {
        this.initProgramArgs = initProgramArgs;
    }

    public String getTerminalInfo() {
        if (TextUtils.isEmpty(terminalInfo)) {
            return "linux";
        }

        return terminalInfo;
    }

    @SuppressWarnings("unused")
    public void setTerminalInfo(String terminalInfo) {

        this.terminalInfo = terminalInfo;
    }

    @SuppressWarnings("unused")
    public boolean isAlreadyInit() {
        return alreadyInit;
    }

    @SuppressWarnings("unused")
    public void setAlreadyInit(boolean alreadyInit) {
        this.alreadyInit = alreadyInit;
    }

    @SuppressWarnings("unused")
    public ProfileOml getOmlInterface() {
        return oml;
    }
}

