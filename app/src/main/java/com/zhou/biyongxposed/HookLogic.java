package com.zhou.biyongxposed;

import android.app.Notification;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
/**
 * @author DX
 * 注意：该类不要自己写构造方法，否者可能会hook不成功
 * 开发Xposed模块完成以后，建议修改xposed_init文件，指向这个类,以提升性能
 * 所以这个类需要implements IXposedHookLoadPackage,以防修改xposed_init文件后忘记
 * Created by DX on 2017/10/4.
 */

public class HookLogic implements IXposedHookLoadPackage {
    private static final String TAG = "Xposedbiyong";
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if(!loadPackageParam.packageName.equals("org.telegram.btcchat")){
            return;
        }
        XposedHelpers.findAndHookMethod("android.app.NotificationManager", loadPackageParam.classLoader, "notify", String.class, int.class, Notification.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                //通过param拿到第三个入参notification对象
                Notification notification = (Notification) param.args[2];
                Object title = notification.extras.get("android.title");
                Object text = notification.extras.get("android.text");
                if(text!=null&&title!=null){
                    Log.i(TAG,"title:"+title.toString());
                    Log.i(TAG,"Text:"+text.toString());
                }
                if (text != null && !text.toString().contains("下载BiYong")) {
                        param.setResult(null);
                    }
                }
            });
        }
    }