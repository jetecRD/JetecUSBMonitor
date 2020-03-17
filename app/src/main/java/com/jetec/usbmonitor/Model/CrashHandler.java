package com.jetec.usbmonitor.Model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;

import androidx.annotation.NonNull;

import com.jetec.usbmonitor.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler{

    private static final String       PATH             = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String       FILE_NAME_SUFFIX = ".trace";
    private static       CrashHandler sInstance        = new CrashHandler();
    private Thread.UncaughtExceptionHandler  mDefaultCrashHandler;
    private Context                          mContext;


    private CrashHandler()
    {
    }

    public static CrashHandler getInstance()
    {
        return sInstance;
    }

    public void init(@NonNull Context context)
    {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try
        {
            //保存到本地
            exportExceptionToSDCard(e);
            //下面也可以写上传的服务器的代码
        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
        e.printStackTrace();
        //如果系统提供了默认的异常处理器，则交给系统去结束程序，否则就自己结束自己
        if (mDefaultCrashHandler != null)
        {
            mDefaultCrashHandler.uncaughtException(t, e);
        } else
        {
            Process.killProcess(Process.myPid());
        }
    }
    /**
     * 导出异常信息到SD卡
     *
     * @param e
     */
    private void exportExceptionToSDCard(@NonNull Throwable e)
    {
        //判断SD卡是否存在
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            return;
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        File file = new File(PATH + File.separator + time +mContext.getString(R.string.app_name)+ FILE_NAME_SUFFIX);

        try
        {
            //往文件中写入数据
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            pw.println(appendPhoneInfo());
            e.printStackTrace(pw);
            pw.close();
        } catch (IOException e1)
        {
            e1.printStackTrace();
        } catch (PackageManager.NameNotFoundException e1)
        {
            e1.printStackTrace();
        }
    }

    /**
     * 获取手机信息
     */
    private String appendPhoneInfo() throws PackageManager.NameNotFoundException
    {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        StringBuilder sb = new StringBuilder();
        //App版本
        sb.append("APP版本: ");
        sb.append(pi.versionName);
        sb.append("_");
        sb.append(pi.versionCode + "\n");

        //Android版本号
        sb.append("APP系統版本號: ");
        sb.append(Build.VERSION.RELEASE);
        sb.append("_");
        sb.append(Build.VERSION.SDK_INT + "\n");

        //手机制造商
        sb.append("手機廠牌: ");
        sb.append(Build.MANUFACTURER + "\n");

        //手机型号
        sb.append("手機型號: ");
        sb.append(Build.MODEL + "\n");

        //CPU架构
        sb.append("CPU架構: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            sb.append(Arrays.toString(Build.SUPPORTED_ABIS));
        } else
        {
            sb.append(Build.CPU_ABI);
        }
        return sb.toString();
    }

}
