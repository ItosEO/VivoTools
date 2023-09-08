package com.itos.vivotools;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import rikka.shizuku.Shizuku;

public class SActivity extends Activity {
    boolean b = true, c = false;
    private boolean isInstalling = false;
    Context context = this;
    TextView stextView;

    public void assets_to_data(String filename, String filepath) {
        Log.d("ass to data：", "被调用");
        try {
            InputStream is = getAssets().open(filename);
            FileOutputStream fileOutputStream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = is.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            is.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            Log.d("ass to data：", "成功");
        } catch (IOException ignored) {
            Log.d("ass to data：", "异常");
        }
    }
    /****************
     * 返回True表示该软件包同时被暂停和停用
     * 否则返回false
     * 参数为需要检查的包名
     ******************/
    private boolean isAppPausedAndDisabled(String packageName) {
        PackageManager packageManager = getPackageManager();

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            // 获取应用程序的停用状态
            boolean isDisabled = applicationInfo.enabled;
            // 获取应用程序的暂停状态
            boolean isSuspended = (applicationInfo.flags & ApplicationInfo.FLAG_SUSPENDED) != 0;
            // 判断应用程序是否同时被暂停和停用
            Toast.makeText(this,Boolean.toString(isDisabled)+" "+Boolean.toString(isSuspended),Toast.LENGTH_LONG);
            return isSuspended && isDisabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s);
        setTitle("GameWatch降级");
        check();
        //限定一下横屏时的窗口宽度,让其不铺满屏幕。否则太丑
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getWindow().getAttributes().width = (getWindowManager().getDefaultDisplay().getHeight());

        stextView = findViewById(R.id.st);
        String g_version = MainActivity.getAppVersion(context, "com.vivo.gamewatch");
        if (g_version != null) {
            stextView.setText(String.format("当前GameWatch版本：%s", g_version));
            stextView.setTextColor(Color.GREEN);
        } else {
            stextView.setText("获取GameWatch版本失败");
        }

    }

    private synchronized void install_apk(String filename) {
        if (!b || !c) {
            Toast.makeText(SActivity.this, "Shizuku状态异常", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isInstalling) {
            Toast.makeText(SActivity.this, "正在进行其他安装，请稍后点击", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(SActivity.this, "正在安装 " + filename + ",请稍后...", Toast.LENGTH_SHORT).show();

        Thread installThread = new Thread() {
            @Override
            public void run() {
                try {
                    isInstalling = true;  // 将 isInstalling 标记设置为 true 表示正在进行安装
                    final String[] file1 = {getExternalFilesDir(null).getPath() + "/" + filename};
                    if (!new File(file1[0]).exists())
                        assets_to_data(filename, file1[0]);
                    Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
                    OutputStream out = p.getOutputStream();
                    out.write(("cp " + file1[0] + " /data/local/tmp" + ";chmod 777 /data/local/tmp/" + filename + ";rm -rf " + file1[0] + "\n").getBytes());
                    file1[0] = "/data/local/tmp/" + filename;
                    out.write(("service call package 131 s16 com.vivo.gamewatch i32 0 i32 0"+"\n").getBytes());
                    out.write(("pm install -r -d " + file1[0] + "\nexit\n").getBytes());
                    out.flush();
                    out.close();

                    StringBuffer errorMsg = new StringBuffer();
                    Thread h1=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                String inline;
                                while ((inline = mReader.readLine()) != null) {
                                    errorMsg.append(inline + "\n");
                                }
                                Log.d("Error data:", errorMsg.toString());
                                mReader.close();
                            } catch (Exception e) {
                                errorMsg.append("读取命令输出异常：" + e.getMessage());
                            }
                            // 在 UI 线程执行Toast提示时也将错误信息输出到控制台方便调试
                            Log.e("ShellError", "执行Shell命令异常, errorMsg: " + errorMsg.toString());
                        }
                    });
                    h1.start();
                    p.waitFor();
                    int ReturnValue = p.exitValue();
                    if (ReturnValue == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (filename) {
                                    case "GW_KPL.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为KPL专版,享受狂暴的调度吧,KPL版本推荐搭配游戏优化使用", Toast.LENGTH_SHORT).show();
                                    case "20123.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为2.0.1.23,老机型狂喜", Toast.LENGTH_SHORT).show();
                                    case "30033.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为3.0.0.33,享受经典的调度和触控吧", Toast.LENGTH_SHORT).show();
                                    case "30046.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为3.0.0.46,享受积极的调度吧", Toast.LENGTH_SHORT).show();
                                    case "30056.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为3.0.0.56,享受丝滑的触控吧", Toast.LENGTH_SHORT).show();
                                    case "30058.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为3.0.0.58,享受稳定的帧率吧", Toast.LENGTH_SHORT).show();
                                    case "30061.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为3.0.0.61,享受优化后的线程放置吧(主要王者原神)", Toast.LENGTH_SHORT).show();
                                    case "30062.apk" ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为3.0.0.62,享受最新的调度思路吧", Toast.LENGTH_SHORT).show();
                                    default ->
                                            Toast.makeText(SActivity.this, "降级完成,当前为" + filename, Toast.LENGTH_SHORT).show();
                                }
                                String g_version = MainActivity.getAppVersion(context, "com.vivo.gamewatch");
                                //String g_version = MainActivity.getAppVersion(context, "com.example.myapplication1");
                                stextView.setText(String.format("当前GameWatch版本：%s", g_version));
                            }
                        });

                    } else {
                        //获取异常输出
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //将错误信息复制到剪贴板
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("label", errorMsg.toString());
                                clipboard.setPrimaryClip(clipData);

                                //在 toast 中显示错误消息以便提示用户
                                Toast.makeText(SActivity.this, "安装" + filename + "失败，报错信息复制到剪贴板:\n"+ errorMsg.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("label", e.getMessage());
                            clipboard.setPrimaryClip(clipData);
                            Toast.makeText(SActivity.this, "安装" + filename + "失败,报错信息：\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }finally {
                    // 重置 isInstalling 标记为 false
                    isInstalling = false;
                }
            }
        };
        installThread.start();
    }

    public synchronized void install_20123(View view) {
        install_apk("20123.apk");
    }

    public synchronized void install_30033(View view) {
        install_apk("30033.apk");
    }



    public synchronized void install_30046(View view) {
        install_apk("30046.apk");
    }

    public synchronized void install_30056(View view) {
        install_apk("30056.apk");
    }

    public synchronized void install_30058(View view) {
        install_apk("30058.apk");
    }

    public synchronized void install_30061(View view) {
        install_apk("30061.apk");
    }

    public synchronized void install_30062(View view) {
        install_apk("30062.apk");
    }

    public synchronized void install_KPL(View view) {
        install_apk("GW_KPL.apk");
    }

    private void check() {
        b = true;
        c = false;
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                Shizuku.requestPermission(0);
            else c = true;
        } catch (Exception e) {
            if (checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED)
                c = true;
            if (e.getClass() == IllegalStateException.class) {
                b = false;

            }
        }
        Toast.makeText(this, "Shizuku " + (b ? "已运行" : "未运行") + (c ? " 已授权" : " 未授权"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        Log.d("SActivity", "onPause:");
        // finish();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("SActivity", "onStop:");
        finish();
        super.onStop();
    }
}
