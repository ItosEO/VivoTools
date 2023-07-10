package com.itos.vivotools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
    static String apkPath;
    static String apkName;
    boolean b = true, c = false;
    private SharedPreferences sharedPreferences;

    private final String PREFERENCE_NAME = "MFPF"; //  SharedPreferences 的常量
    private final String IS_FIRST_TIME = "isFirstTime";
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = (requestCode, grantResults) -> check();

    public static String getAppVersion(Context context, String packageName) {
        String versionName = null;
        Log.d("get app ver：", "调用获取" + packageName + "版本");
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = pInfo.versionName;
            Log.d("get app ver：", "获取成功");
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("get app ver：", "获取失败");
            e.printStackTrace();
        }
        return versionName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            apkPath = uri.getPath(); // 获取APK文件路径
            apkName = uri.getLastPathSegment(); // 获取APK文件名
        }
    }

    public void CreateupdataDialog() {
        try {
            InputStream is = getAssets().open("updata");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            reader.close();
            String message = sb.toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
            // 设置标题
            builder.setTitle("更新日志");
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CreatejieshaoDialog() {
        try {
            InputStream is = getAssets().open("jieshao");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            reader.close();
            String message = sb.toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("明白了", (dialog, id) -> dialog.dismiss());
            // 设置标题
            builder.setTitle("使用简介");
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        // setContentView(R.layout.title_bar);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        // 设置自定义标题布局

        Log.d("onCreate：", "yes");
        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

//限定一下横屏时的窗口宽度,让其不铺满屏幕。否则太丑
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getWindow().getAttributes().width = (getWindowManager().getDefaultDisplay().getHeight());
        TextView textView1 = findViewById(R.id.t1);
        TextView textView2 = findViewById(R.id.t2);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView ti = findViewById(R.id.ti);

        String g_version = getAppVersion(this, "com.vivo.gamewatch");
        String f_version = getAppVersion(this, "com.vivo.fuelsummary");
        if (g_version != null) {
            textView1.setText(String.format("当前GameWatch版本：%s", g_version));
            textView1.setTextColor(Color.GREEN);
        } else {
            textView1.setText("获取GameWatch版本失败");
        }
        if (f_version != null) {
            textView2.setText(String.format("当前电源信息版本：%s", f_version));
            textView2.setTextColor(Color.GREEN);
        } else {
            textView2.setText("获取电源信息版本失败");
        }
        Log.d("onCreate：", "请求自身版本开始");
        String s_version = getAppVersion(this, getPackageName());
        ti.setText(String.format("蓝厂工具盒(%s)", s_version));
        Log.d("onCreate：", "请求自身版本完成");

        ti.setOnClickListener(view -> CreatejieshaoDialog());
        // 设置点击标题事件

        ti.setOnLongClickListener(view -> {
            CreateupdataDialog();
            return false;
        });
        // 设置长按标题事件

        sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        // 当首次打开应用程序时，展示自定义的对话框
        if (!sharedPreferences.getBoolean(IS_FIRST_TIME, false)) {
            CreatejieshaoDialog();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(IS_FIRST_TIME, true);
            editor.apply();
        }
        check();
    } // onCreate到这里结束

    public void start_factivity(View view) {
        startActivity(new Intent(this, FActivity.class));
    }

    public void start_sactivity(View view) {
        startActivity(new Intent(this, SActivity.class));
    }

    public void show_author(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("coolmarket://u/3287595"));
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "打开酷安失败，请检查是否已安装", Toast.LENGTH_SHORT).show();
            // 处理ActivityNotFoundException异常，例如提示用户下载应用或打开其他应用商店
        }
    }

    private void showImageDialog(String imageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 创建一个 ImageView 并添加到对话框中
        ImageView imageView = new ImageView(this);
        try {
            InputStream is = getAssets().open(imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        builder.setView(imageView);  // 将 ImageView 加到对话框中
        builder.setNegativeButton("OK", (dialog, which) -> {
            // 点击 OK 按钮后的操作
            dialog.dismiss();
        });
        builder.show();  // 显示对话框
    }

    public void zanzhu(View view) {
        String show_text = """
                您可以通过微信或支付宝来赞助我们
                这个纯属自愿!!!
                这个纯属自愿!!!
                这个纯属自愿!!!

                添加这个功能主要是因为服务器成本高昂
                一是需要回回血
                二是计划升级服务器需要资金
                                
                如果您有条件的话,希望可以捐赠一点
                不求多少,支持一下我们,非常感谢
                                
                您可以选择在捐赠时备注您的酷安账号
                我们将在网站上显示感谢名单
                本工具永久免费!!!
                """;
        new AlertDialog.Builder(this)
                .setTitle("捐赠开发者")
                .setMessage(show_text)
                .setPositiveButton("支付宝", (dialog, which) -> {
                    // 点击支付宝按钮后的操作
                    dialog.dismiss();
                    showImageDialog("zfb.jpg");
                })
                .setNegativeButton("微信", (dialog, which) -> {
                    // 点击微信按钮后的操作
                    dialog.dismiss();
                    showImageDialog("wx.png");
                })
                .setNeutralButton("捐赠列表", (dialog, which) -> {
                    // 点击捐赠列表按钮后的操作
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse("https://itos.codegang.top/share/thanks.html"));
                    startActivity(intent);
                })
                .show();
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
        if(b == false || c == false){
            Toast.makeText(this, "Shizuku " + (b ? "已运行" : "未运行") + (c ? " 已授权" : " 未授权"), Toast.LENGTH_SHORT).show();
        }
    }
    //检查Shizuku权限，申请Shizuku权限的函数


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);


        try {
            Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            String file1 = "/data/local/tmp/";
            out.write(("rm -rf " + file1 + "30058.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "30056.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "30046.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "20123.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "30039.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "GW_KPL.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "30033.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "12.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "16.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "17.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "18.apk" + "\n").getBytes());
            out.write(("rm -rf " + file1 + "21.apk" + "\n").getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {

        }

    }

    @Override
    protected void onPause() {
        Log.d("MainActivity", "onPause:");
        // finish();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity", "onStop:");
        finish();
        super.onStop();
    }
}