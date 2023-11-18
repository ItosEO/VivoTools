package com.itos.vivotools;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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


public class FActivity extends Activity {
    boolean b = true, c = false;
    private boolean isInstalling = false;

    Context context = this;
    TextView ftextView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f);
        ftextView = findViewById(R.id.ft);
        setTitle("满血充电");
        check();
        // 限定一下横屏时的窗口宽度,让其不铺满屏幕。否则太丑
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getWindow().getAttributes().width = (getWindowManager().getDefaultDisplay().getHeight());
        getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);

        String g_version = MainActivity.getAppVersion(context, "com.vivo.fuelsummary");
        // String g_version = com.itos.vivotools.MainActivity.getAppVersion(context,"com.itos.vivotools");

        if (g_version != null) {
            double g_version_num;
            String[] numberParts = g_version.split("\\.");

// 必须包含整数部分和最后一个小数部分
            if (numberParts.length >= 2) {
                boolean isValid = true;
                StringBuilder versionBuilder = new StringBuilder();

                for (int i = 0; i < numberParts.length; i++) {
                    String part = numberParts[i];

                    if (i == numberParts.length - 1) {
                        // 最后一部分是小数部分
                        if (!part.matches("\\d+")) {
                            isValid = false;
                            break;
                        }

                        versionBuilder.append(".").append(part);
                    } else {
                        // 其他部分是整数部分
                        if (!part.matches("\\d+")) {
                            isValid = false;
                            break;
                        }

                        versionBuilder.append(part);
                    }
                }

                if (isValid) {
                    String g_version_new = versionBuilder.toString();
                    g_version_num = Double.parseDouble(g_version_new);
                } else {
                    // 版本号不可解析
                    g_version_num = 0;
                    Log.d("", "解析失败");
                }
            } else {
                // 版本号不可解析
                g_version_num = 0;
                Log.d("", "解析失败");
            }            //System.out.println("g_version_num: " + g_version_num);
//            Log.d("版本号：", g_version);
            String temp = Double.toString(g_version_num);
            Log.d("转换后版本号：", temp);
            if (g_version_num >= 1.7) {
                ftextView.setText(String.format("当前电源信息版本：%s(不支持满血充电)", g_version));
            } else {
                ftextView.setText(String.format("当前电源信息版本：%s", g_version));
                Log.d("", "支持");
            }
            ftextView.setTextColor(Color.GREEN);
        } else ftextView.setText("获取电源信息版本失败");



    } // onCreate结束


    private synchronized void install_apk(String filename) {
        if (!b || !c) {
            Toast.makeText(FActivity.this, "shizuku状态异常", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isInstalling) {
            Toast.makeText(FActivity.this, "正在进行其他安装，请稍后点击", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(FActivity.this, "正在安装"+filename+",请稍后...", Toast.LENGTH_SHORT).show();

        Thread installThread = new Thread() {
            @Override
            public void run() {
                isInstalling = true;

                String file1 = getExternalFilesDir(null).getPath() + "/" + filename;
                if (!new File(file1).exists())
                    assets_to_data(filename, file1);
                Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
                OutputStream out = p.getOutputStream();
                try {
                    out.write(("cp " + file1 + " /data/local/tmp" + ";chmod 777 /data/local/tmp/" + filename + ";rm -rf " + file1 + "\n").getBytes());
                    file1 = "/data/local/tmp/" + filename;
                    out.write(("pm install -r -d " + file1 + "\nexit\n").getBytes());
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
                                    case "12.apk" ->
                                            Toast.makeText(FActivity.this, "降级完成,当前为1.2,充电速度快,但x系列和neo7不支持满血充电", Toast.LENGTH_SHORT).show();
                                    case "16.apk" ->
                                            Toast.makeText(FActivity.this, "降级完成,当前为1.6,比1.2慢点,都支持满血充电", Toast.LENGTH_SHORT).show();
                                    case "17.apk" ->
                                            Toast.makeText(FActivity.this, "降级完成,当前为1.7,充电最快但不支持满血充电", Toast.LENGTH_SHORT).show();
                                    case "18.apk" ->
                                            Toast.makeText(FActivity.this, "降级完成,当前为1.8,此版本对电池健康不错", Toast.LENGTH_SHORT).show();
                                    case "23.apk" ->
                                            Toast.makeText(FActivity.this, "降级完成,当前为2.3.0.0,搭配30033 GW以获取极致触控", Toast.LENGTH_SHORT).show();
                                    case "20.apk" ->
                                            Toast.makeText(FActivity.this, "降级完成,当前为2.0.0.0,触控和充电速度都不错", Toast.LENGTH_SHORT).show();
                                    default ->
                                            Toast.makeText(FActivity.this, "降级完成,当前为" + filename, Toast.LENGTH_SHORT).show();
                                }
                                String g_version = MainActivity.getAppVersion(context, "com.vivo.fuelsummary");
                                if (g_version != null) {
                                    double g_version_num;
                                    String[] numberParts = g_version.split("\\.");

                                    // 必须包含整数部分和最后一个小数部分
                                    if (numberParts.length >= 2) {
                                        boolean isValid = true;
                                        StringBuilder versionBuilder = new StringBuilder();

                                        for (int i = 0; i < numberParts.length; i++) {
                                            String part = numberParts[i];

                                            if (i == numberParts.length - 1) {
                                                // 最后一部分是小数部分
                                                if (!part.matches("\\d+")) {
                                                    isValid = false;
                                                    break;
                                                }

                                                versionBuilder.append(".").append(part);
                                            } else {
                                                // 其他部分是整数部分
                                                if (!part.matches("\\d+")) {
                                                    isValid = false;
                                                    break;
                                                }

                                                versionBuilder.append(part);
                                            }
                                        }

                                        if (isValid) {
                                            String g_version_new = versionBuilder.toString();
                                            g_version_num = Double.parseDouble(g_version_new);
                                        } else {
                                            // 版本号不可解析
                                            g_version_num = 0;
                                            Log.d("", "解析失败");
                                        }
                                    } else {
                                        // 版本号不可解析
                                        g_version_num = 0;
                                        Log.d("", "解析失败");
                                    }
                                    //System.out.println("g_version_num: " + g_version_num);
                                    //Log.d("版本号：", g_version);
                                    String temp = Double.toString(g_version_num);
                                    Log.d("转换后版本号：", temp);
                                    if (g_version_num >= 1.7) {
                                        ftextView.setText(String.format("当前电源信息版本：%s(不支持满血充电)", g_version));
                                    } else {
                                        ftextView.setText(String.format("当前电源信息版本：%s", g_version));
                                        Log.d("", "支持");
                                    }
                                    ftextView.setTextColor(Color.GREEN);
                                } else {
                                    ftextView.setText("获取电源信息版本失败");
                                }
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("label", errorMsg.toString());
                                clipboard.setPrimaryClip(clipData);

                                //在 toast 中显示错误消息以便提示用户
                                Toast.makeText(FActivity.this, "安装" + filename + "失败，报错信息复制到剪贴板:\n"+ errorMsg.toString(), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(FActivity.this, "安装"+filename+"失败,报错信息：\n" + e.getMessage()+"已复制到剪切板", Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    // 重置 isInstalling 标记为 false
                    isInstalling = false;
                }

            }
        };
        installThread.start();
    }

    public synchronized void install_12(View view) {
        install_apk("12.apk");
    }

    public synchronized void install_16(View view) {
        install_apk("16.apk");
    }

    public synchronized void install_17(View view) {
        install_apk("17.apk");
    }

    public synchronized void install_18(View view) {
        install_apk("18.apk");
    }

    public synchronized void install_23(View view) {
        install_apk("23.apk");
    }

    public synchronized void install_20(View view) {
        install_apk("20.apk");
    }

    public void open_fuelsummary(View view) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.vivo.fuelsummary", "com.vivo.fuelsummary.FuelSummary");
            startActivity(intent);
            Toast.makeText(FActivity.this, "点击循环充电，留空点确定，插充电器后点开始", Toast.LENGTH_SHORT).show();
            Toast.makeText(FActivity.this, "自动息屏后手动点亮屏幕，点充放即可亮屏快充", Toast.LENGTH_SHORT).show();
            Toast.makeText(FActivity.this, "若因为高温降功率，可以打开工厂测试里的“关闭高温保护”", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(FActivity.this, "打开活动失败，请检查是否已安装受支持的版本", Toast.LENGTH_SHORT).show();
            // 处理ActivityNotFoundException异常，例如提示用户下载应用或打开其他应用商店
        }
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
        Log.d("FActivity", "onPause:");
        // finish();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("FActivity", "onStop:");
        finish();
        super.onStop();
    }
}