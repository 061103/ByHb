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
    private static final String class_name = "org.telegram.messenger.NotificationCenter";
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod("android.app.NotificationManager", loadPackageParam.classLoader, "notify",String.class, int.class, Notification.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String title = "__";
                String text = "--";
                //通过param拿到第三个入参notification对象
                Notification notification = (Notification) param.args[2];
                //获得包名
                String aPackage = notification.contentView.getPackage();
                title = (String) notification.extras.get("android.title");
                text = (String) notification.extras.get("android.text");
                if ("org.telegram.btcchat".equals(aPackage)) {
                    if (text!=null&&!text.contains("下载BiYong APP")) {
                        param.setResult(null);
                    }
                }
            }
        });
        Class<?> hookclass = null;
            try {
                hookclass = loadPackageParam.classLoader.loadClass(class_name);
            } catch (Exception e) {
                XposedBridge.log("[Failed!]Can not find " + class_name);
                return;
            }
            XposedBridge.log("[Success!]Find class " + class_name);
            XposedHelpers.findAndHookMethod(hookclass, "isAnimationInProgress", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Boolean started = (Boolean) param.getResult();
                    if(started){
                        XposedBridge.log("获取到started状态:"+started);
                        param.setResult(false);
                        XposedBridge.log("设置状态为:false");
                    }
                }
            });
        }
    }