package com.zhou.biyongxposed;

import android.app.Notification;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 注意：该类不要自己写构造方法，否者可能会hook不成功
 * 开发Xposed模块完成以后，建议修改xposed_init文件，并将起指向这个类,以提升性能
 * 所以这个类需要implements IXposedHookLoadPackage,以防修改xposed_init文件后忘记
 */

public class HookLogic implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod("android.app.NotificationManager", loadPackageParam.classLoader, "notify"
                , String.class, int.class, Notification.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        String text = "--";
                        //通过param拿到第三个入参notification对象
                        Notification notification = (Notification) param.args[2];
                        //获得包名
                        String aPackage = notification.contentView.getPackage();
                        text = (String) notification.extras.get("android.text");
                        if ("org.telegram.btcchat".equals(aPackage)){
                            if(!text.contains("下载BiYong APP,体验红包新功能")) {
                                param.setResult(null);
                                return;
                            }else XposedBridge.log("./..."+text);
                        }
                    }
                });
    }
}