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

import com.kiva.ohmylinux.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Kiva
 * @date 2015/12/9
 */
public class ProfileManager {
    private static ProfileManager ourInstance = new ProfileManager();
    private static FileFilter profilesFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                File profileOml = new File(pathname, "profile.oml");
                return profileOml.isFile();
            }

            return false;
        }
    };

    public static ProfileManager getInstance() {
        return ourInstance;
    }

    private ProfileManager() {
    }

    /**
     * 得到配置文件存放的根目录
     *
     * @return 配置文件存放的根目录
     */
    public File getProfileDir() {
        String sdcard = FileUtils.getSdcardPath();
        if (sdcard == null) {
            return null;
        }

        File profilesDirectory = new File(sdcard + "/Kiva/oh-my-linux");
        if (!profilesDirectory.exists()) {
            if (!profilesDirectory.mkdirs()) {
                return null;
            }
        }

        return profilesDirectory;
    }

    /**
     * 得到指定配置文件的根目录
     *
     * @param profile 指定的配置文件
     * @return 配置文件的根目录
     */
    public File getProfileDir(LinuxProfile profile) {
        File profileDirectory = getProfileDir();

        if (profileDirectory == null) {
            return null;
        }

        return new File(profileDirectory, profile.getNickName());
    }

    /**
     * 保存配置文件
     *
     * @param profile 需要保存的配置文件
     * @return 是否成功
     */
    public boolean saveProfile(LinuxProfile profile) {
        File profileDirectory = getProfileDir();

        if (profileDirectory == null) {
            return false;
        }

        File profileDir = new File(profileDirectory, profile.getNickName());
        if (!FileUtils.mkdir(profileDir)) {
            return false;
        }

        File profileOml = new File(profileDir, "profile.oml");

        return FileUtils.writeFile(profileOml, profile.toOml().getBytes());
    }

    /**
     * 删除配置文件
     *
     * @param profile 需要删除的配置文件
     * @return 是否成功
     */
    public boolean deleteProfile(LinuxProfile profile) {
        File profileDir = getProfileDir(profile);
        return FileUtils.rmrf(profileDir);
    }

    /**
     * 扫描全部配置文件
     *
     * @return 扫描结果
     */
    public List<LinuxProfile> scanProfile() {
        List<LinuxProfile> list = new ArrayList<>();
        File profileDirectory = getProfileDir();

        if (profileDirectory == null) {
            // nothing to scan, return a empty list
            return list;
        }

        for (File dir : profileDirectory.listFiles(profilesFilter)) {
            LinuxProfile profile = new LinuxProfile();

            profile.setNickName(dir.getName());
            profile.loadProfile(dir.getAbsolutePath() + "/profile.oml");
            profile.loadProfile(dir.getAbsolutePath() + "/event.oml");
            profile.getOmlInterface().onSetup(profile);

            list.add(profile);
        }

        Collections.sort(list, new Comparator<LinuxProfile>() {
            @Override
            public int compare(LinuxProfile lhs, LinuxProfile rhs) {
                return lhs.getNickName().compareToIgnoreCase(rhs.getNickName());
            }
        });
        return list;
    }
}
