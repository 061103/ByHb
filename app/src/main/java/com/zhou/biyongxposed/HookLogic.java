package com.zhou.biyongxposed;

import android.app.Notification;
import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    private static final String class_name = "org.telegram.p003ui.DialogsActivity";
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
                XposedBridge.log("Can not find class " + class_name);
                return;
            }
            XposedBridge.log("Find class " + class_name);
            XposedHelpers.findAndHookMethod(hookclass, "refreshNoticeView",new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param)throws Throwable{
                    super.beforeHookedMethod(param);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        }
    // 获取指定名称的类声明的类成员变量、类方法、内部类的信息
    public void dumpClass(Class<?> actions) {

        XposedBridge.log("Dump class " + actions.getName());
        XposedBridge.log("Methods");

        // 获取到指定名称类声明的所有方法的信息
        Method[] m = actions.getDeclaredMethods();
        // 打印获取到的所有的类方法的信息
        for (int i = 0; i < m.length; i++) {

            XposedBridge.log(m[i].toString());
        }

        XposedBridge.log("Fields");
        // 获取到指定名称类声明的所有变量的信息
        Field[] f = actions.getDeclaredFields();
        // 打印获取到的所有变量的信息
        for (int j = 0; j < f.length; j++) {

            XposedBridge.log(f[j].toString());
        }

        XposedBridge.log("Classes");
        // 获取到指定名称类中声明的所有内部类的信息
        Class<?>[] c = actions.getDeclaredClasses();
        // 打印获取到的所有内部类的信息
        for (int k = 0; k < c.length; k++) {

            XposedBridge.log(c[k].toString());
        }
    }
}