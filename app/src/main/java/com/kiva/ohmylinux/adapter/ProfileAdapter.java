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

package com.kiva.ohmylinux.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kiva.ohmylinux.R;
import com.kiva.ohmylinux.profile.LinuxProfile;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Kiva
 * @date 2015/12/10
 */
public class ProfileAdapter extends BaseAdapter {
    private Context context;
    private List<LinuxProfile> profiles;

    public ProfileAdapter(Context context, List<LinuxProfile> profiles) {
        this.context = context;
        this.profiles = profiles;
    }

    @Override
    public int getCount() {
        return profiles.size();
    }

    @Override
    public Object getItem(int position) {
        return profiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_item, null, false);
            holder = new ViewHolder();
            convertView.setTag(holder);

            holder.nickName = (TextView) convertView.findViewById(R.id.profileItemNickname);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LinuxProfile profile = (LinuxProfile) getItem(position);

        holder.nickName.setText(profile.getNickName());

        profile.getOmlInterface().onShow(context, profile);
        return convertView;
    }

    public void update(LinuxProfile profile) {
        remove(profile);

        profiles.add(profile);
        sortProfile();
        notifyDataSetChanged();
    }

    public void remove(LinuxProfile profile) {
        if (profiles.contains(profile)) {
            profiles.remove(profile);
        }

        notifyDataSetChanged();
    }

    private void sortProfile() {
        Collections.sort(profiles, new Comparator<LinuxProfile>() {
            @Override
            public int compare(LinuxProfile lhs, LinuxProfile rhs) {
                return lhs.getNickName().compareToIgnoreCase(rhs.getNickName());
            }
        });
    }

    class ViewHolder {
        TextView nickName;
    }
}
