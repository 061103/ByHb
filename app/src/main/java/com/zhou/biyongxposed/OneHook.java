package com.zhou.biyongxposed;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;



public class OneHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("org.telegram.btcchat")) {
            XposedBridge.log("<包名正确>:"+loadPackageParam.packageName);
            XposedHelpers.findAndHookMethod("com.netease.nis.wrapper.MyApplication", loadPackageParam.classLoader,
                    "Application a", Context.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedBridge.log("获取到的Context-----" + param.args[0]);
                            Context context = (Context) param.args[0];
                            ClassLoader classLoader = context.getClassLoader();
                        }
                    });
        }
    }
    private void dumpClass(Class actions) {
        XposedBridge.log("Dump class " + actions.getName());

        XposedBridge.log("Methods");
        Method[] m = actions.getDeclaredMethods();
        for (int i = 0; i < m.length; i++) {
            XposedBridge.log(m[i].toString());
        }
        XposedBridge.log("Fields");
        Field[] f = actions.getDeclaredFields();
        for (int j = 0; j < f.length; j++) {
            XposedBridge.log(f[j].toString());
        }
        XposedBridge.log("Classes");
        Class[] c = actions.getDeclaredClasses();
        for (int k = 0; k < c.length; k++) {
            XposedBridge.log(c[k].toString());
        }
    }
}
