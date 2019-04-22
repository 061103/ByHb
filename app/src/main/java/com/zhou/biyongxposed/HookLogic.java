package com.zhou.biyongxposed;

import android.app.Notification;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.content.ContentValues.TAG;
import static de.robv.android.xposed.XposedBridge.log;

/**
 * 注意：该类不要自己写构造方法，否者可能会hook不成功
 * 开发Xposed模块完成以后，建议修改xposed_init文件，并将起指向这个类,以提升性能
 * 所以这个类需要implements IXposedHookLoadPackage,以防修改xposed_init文件后忘记
 */

public class HookLogic implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if ("org.telegram.btcchat".equals(loadPackageParam.packageName)) {
            log("成功加载" + loadPackageParam.packageName);
            XposedHelpers.findAndHookMethod("android.app.NotificationManager", loadPackageParam.classLoader, "notify"
                    , String.class, int.class, Notification.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            String title = "--";
                            String text = "--";
                            //通过param拿到第三个入参notification对象
                            Notification notification = (Notification) param.args[2];
                            //获得包名
                            String aPackage = notification.contentView.getPackage();
                            title = (String) notification.extras.get("android.title");
                            text = (String) notification.extras.get("android.text");
                            if ("org.telegram.btcchat".equals(aPackage)&& !text.contains("下载BiYong APP，体验红包新功能")) {
                                param.setResult(null);
                                return;
                            }
                        }
                    });
        }
    }
}