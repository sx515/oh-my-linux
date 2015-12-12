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

package com.kiva.ohmylinux;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.kiva.ohmylinux.adapter.ProfileAdapter;
import com.kiva.ohmylinux.profile.LinuxProfile;
import com.kiva.ohmylinux.profile.ProfileManager;
import com.kiva.ohmylinux.utils.FileUtils;
import com.kiva.ohmylinux.utils.L;
import com.kiva.ohmylinux.utils.LaunchUtils;
import com.kiva.ohmylinux.utils.ResourceUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * @author Kiva
 * @date 2015/12/8
 */
public class MainActivity extends AppCompatActivity implements L.LogCallback {

    private static final int REQ_FIND_DEVICE_PATH = 0;
    private static final int REQ_FIND_DISK_TABLE = 1;

    private static final int STATUS_EDITING = 0;
    private static final int STATUS_NORMAL = 1;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionButton floatingButton;

    private ListView profileListView;
    private ProfileAdapter profileListAdapter;

    private ScrollView logScrollView;
    private AppCompatTextView logTextView;

    private AppCompatEditText editNickName, editDevicePath, editMountPoint, editDiskTableFile,
            editInitProgram, editInitProgramArgs;
    private AppCompatSpinner editFsType;
    private AppCompatCheckBox editMountSd0, editMountSd1;
    private AppCompatButton editFindDevicePath, editFindDiskTableFile;
    private LinearLayout editLayout, defaultLayout;

    private LinuxProfile currentProfile;
    private int status = STATUS_NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        loadProfile();
        ResourceUtils.initResources(this);
    }

    /**
     * 加载所有存在的配置文件
     */
    private void loadProfile() {
        List<LinuxProfile> list = ProfileManager.getInstance().scanProfile();
        L.d("found " + list.size() + " profile(s)");

        profileListAdapter = new ProfileAdapter(this, list);
        profileListView.setAdapter(profileListAdapter);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        logScrollView = (ScrollView) findViewById(R.id.mainLogScrollView);
        logTextView = (AppCompatTextView) findViewById(R.id.mainLogText);
        L.setLogCallback(this);

        floatingButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == STATUS_NORMAL) {
                    doCreateNewProfile();
                } else if (status == STATUS_EDITING) {
                    doClose();
                }
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
        drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                drawerToggle.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerToggle.onDrawerOpened(drawerView);
                floatingButton.hide();

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    if (profileListAdapter.getCount() == 0) {
                        setTitle(R.string.no_profile);
                    } else {
                        setTitle(R.string.profiles);
                    }
                } else if (drawer.isDrawerOpen(GravityCompat.END)) {
                    setTitle(R.string.log_console);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                drawerToggle.onDrawerClosed(drawerView);
                floatingButton.show();

                if (!drawer.isDrawerOpen(GravityCompat.START) &&
                        !drawer.isDrawerOpen(GravityCompat.END)) {
                    setTitle(R.string.app_name);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                drawerToggle.onDrawerStateChanged(newState);
            }
        });
        drawerToggle.syncState();

        profileListView = (ListView) findViewById(R.id.mainProfileListView);
        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawer.closeDrawer(GravityCompat.START);
                LinuxProfile profile = (LinuxProfile) profileListAdapter.getItem(position);
                doSelectProfile(profile);
            }
        });
        profileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                LinuxProfile profile = (LinuxProfile) profileListAdapter.getItem(position);
                doShowOptions(profile);
                return true;
            }
        });

        editLayout = (LinearLayout) findViewById(R.id.mainEditLayout);
        defaultLayout = (LinearLayout) findViewById(R.id.mainDefaultLayout);

        editNickName = (AppCompatEditText) findViewById(R.id.mainEditNickName);
        editDevicePath = (AppCompatEditText) findViewById(R.id.mainEditDevicePath);
        editMountPoint = (AppCompatEditText) findViewById(R.id.mainEditMountPoint);
        editDiskTableFile = (AppCompatEditText) findViewById(R.id.mainEditDiskTableFile);
        editInitProgram = (AppCompatEditText) findViewById(R.id.mainEditInitProgram);
        editInitProgramArgs = (AppCompatEditText) findViewById(R.id.mainEditInitProgramArgs);
        editFindDevicePath = (AppCompatButton) findViewById(R.id.mainEditFindDevicePath);
        editFindDiskTableFile = (AppCompatButton) findViewById(R.id.mainEditFindDiskTableFile);
        editMountSd0 = (AppCompatCheckBox) findViewById(R.id.mainEditMountSd0);
        editMountSd1 = (AppCompatCheckBox) findViewById(R.id.mainEditMountSd1);
        editFsType = (AppCompatSpinner) findViewById(R.id.mainEditFsType);

        editNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (currentProfile == null) {
                    return;
                }
                String newName = editNickName.getText().toString();
                if (newName.isEmpty()) {
                    return;
                }

                currentProfile.setNickName(newName);
                editMountPoint.setText(currentProfile.getMountPoint());
            }
        });

        editFindDevicePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doOpenFileBrowser(REQ_FIND_DEVICE_PATH);
            }
        });
        editFindDiskTableFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doOpenFileBrowser(REQ_FIND_DISK_TABLE);
            }
        });
    }

    private void initData() {
        String[] fsTypes = getResources().getStringArray(R.array.fs_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, fsTypes);
        editFsType.setAdapter(adapter);
    }

    /**
     * 选择一个配置文件作为编辑或者启动的对象
     *
     * @param profile 选择的对象
     */
    private void doSelectProfile(LinuxProfile profile) {
        doClose();
        currentProfile = profile;
        showEditLayout(true);

        editNickName.setText(profile.getNickName());
        editDevicePath.setText(profile.getDevicePath());
        editMountPoint.setText(profile.getMountPoint());
        editDiskTableFile.setText(profile.getDiskTableFile());
        editInitProgram.setText(profile.getInitProgram());
        editInitProgramArgs.setText(profile.getInitProgramArgs());

        String fsType = profile.getFileSystemType();
        switch (fsType) {
            case "ext4":
                editFsType.setSelection(0);
                break;
            case "ext3":
                editFsType.setSelection(1);
                break;
            case "ext2":
                editFsType.setSelection(2);
                break;
        }

        editMountSd0.setChecked(profile.isMountSdcard0());
        editMountSd1.setChecked(profile.isMountSdcard1());

        setStatus(STATUS_EDITING);
    }

    /**
     * 创建新的配置文件
     */
    private void doCreateNewProfile() {
        LinuxProfile profile = new LinuxProfile();
        profile.setNickName("linux_" + UUID.randomUUID().toString().substring(0, 6));

        doSelectProfile(profile);
    }

    /**
     * 关闭当前编辑的对象，不保存
     */
    private void doClose() {
        resetEditWidgets();
        showEditLayout(false);
        setStatus(STATUS_NORMAL);
        currentProfile = null;
    }

    /**
     * 保存当前编辑中的对象
     *
     * @param closeAfterSave 保存成功后是否关闭
     * @return 是否成功保存
     */
    private boolean doSave(boolean closeAfterSave) {
        if (currentProfile == null) {
            return false;
        }

        if (editNickName.getText().toString().isEmpty()) {
            String err = getString(R.string.nick_name_cannot_be_null);
            Snackbar.make(floatingButton, err, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if (editDevicePath.getText().toString().isEmpty()) {
            String err = getString(R.string.device_path_cannot_be_null);
            Snackbar.make(floatingButton, err, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        LinuxProfile pro = currentProfile;

        pro.setNickName(editNickName.getText().toString());
        pro.setDevicePath(editDevicePath.getText().toString());
        pro.setMountPoint(editMountPoint.getText().toString());
        pro.setDiskTableFile(editDiskTableFile.getText().toString());
        pro.setInitProgram(editInitProgram.getText().toString());
        pro.setInitProgramArgs(editInitProgramArgs.getText().toString());
        pro.setFileSystemType((String) editFsType.getSelectedItem());
        pro.setMountSdcard0(editMountSd0.isChecked());
        pro.setMountSdcard1(editMountSd1.isChecked());

        if (!ProfileManager.getInstance().saveProfile(pro)) {
            Snackbar.make(floatingButton, R.string.save_failed, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        profileListAdapter.update(pro);
        Snackbar.make(floatingButton, R.string.save_successfully, Snackbar.LENGTH_SHORT).show();
        if (closeAfterSave) {
            doClose();
        }
        return true;
    }

    /**
     * 为配置文件弹出选择框，进行启动或者删除的操作
     *
     * @param profile 操作的配置文件
     */
    private void doShowOptions(final LinuxProfile profile) {
        String[] items = getResources().getStringArray(R.array.profile_options);

        new AlertDialog.Builder(this)
                .setTitle(profile.getNickName())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            // launch
                            case 0:
                                doLaunch(profile);
                                break;
                            // delete
                            case 1:
                                doDelete(profile);
                                break;
                        }
                    }
                })
                .setPositiveButton(android.R.string.no, null)
                .show();
    }

    /**
     * 直接启动一个配置文件
     *
     * @param profile 需要启动的配置文件
     */
    private void doLaunch(LinuxProfile profile) {
        String command = LaunchUtils.createTerminalCommand(this, profile);
        Intent i = LaunchUtils.createTerminalIntent(command);

        try {
            profile.getOmlInterface().onLaunch(this, profile);
            startActivity(i);
            profile.getOmlInterface().onPostLaunch(this, profile);
        } catch (Exception e) {
            L.e(e);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.launch_field)
                    .setMessage(R.string.no_terminal_found)
                    .setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            byte[] termBuffer = ResourceUtils.readAssetsFile(MainActivity.this, "terminal");
                            File saveTo = new File(MainActivity.this.getExternalCacheDir(), "term.apk");
                            if (!FileUtils.writeFile(saveTo, termBuffer)) {
                                L.e("cannot extract terminal from apk archive!");
                            }

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(saveTo), "application/vnd.android.package-archive");
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    /**
     * 保存并启动当前选择的配置文件
     */
    private void doLaunch() {
        if (!doSave(false)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.launch_field)
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        }

        doLaunch(currentProfile);
    }

    /**
     * 删除配置文件
     *
     * @param profile 需要删除的配置文件
     */
    private void doDelete(final LinuxProfile profile) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProfileManager.getInstance().deleteProfile(profile);
                        profileListAdapter.remove(profile);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void doOpenFileBrowser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 控制编辑视图的可见性
     *
     * @param en 是否可见
     */
    private void showEditLayout(boolean en) {
        if (en) {
            defaultLayout.setVisibility(View.GONE);
            editLayout.setVisibility(View.VISIBLE);
        } else {
            defaultLayout.setVisibility(View.VISIBLE);
            editLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 重置编辑控件的状态
     */
    private void resetEditWidgets() {
        editNickName.setText("");
        editDevicePath.setText("");
        editMountPoint.setText("");
        editDiskTableFile.setText("");
        editInitProgram.setText("");
        editInitProgramArgs.setText("");
        editFsType.setSelection(0);
        editMountSd0.setChecked(false);
        editMountSd1.setChecked(false);
    }

    /**
     * 设置当前的状态(编辑中或者正常)
     *
     * @param status 新状态
     */
    private void setStatus(int status) {
        this.status = status;

        switch (status) {
            case STATUS_NORMAL:
                floatingButton.setImageResource(R.drawable.ic_add);
                break;
            case STATUS_EDITING:
                floatingButton.setImageResource(R.drawable.ic_close);
                break;
        }
    }

    /**
     * 添加一条log信息到日志控制台
     *
     * @param msg log信息
     */
    private void log(String msg) {
        logTextView.append(">>> ");
        logTextView.append(msg);
        logTextView.append("\n");

        logScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            doSave(false);
            return true;
        } else if (id == R.id.action_launch) {
            doLaunch();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Uri uri = data.getData();
        if (uri == null) {
            return;
        }

        String path = FileUtils.getPathFromUri(this, uri);

        switch (requestCode) {
            case REQ_FIND_DEVICE_PATH:
                editDevicePath.setText(path);
                break;
            case REQ_FIND_DISK_TABLE:
                editDiskTableFile.setText(path);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.setLogCallback(null);
    }

    @Override
    public void onE(String errorMsg) {
        log("[ERROR] " + errorMsg);
    }

    @Override
    public void onD(String debugMsg) {
        log("[DEBUG] " + debugMsg);
    }

    @Override
    public void onI(String infoMsg) {
        log("[INFO] " + infoMsg);
    }

    @Override
    public void onW(String warnMsg) {
        log("[WARNING] " + warnMsg);
    }
}
