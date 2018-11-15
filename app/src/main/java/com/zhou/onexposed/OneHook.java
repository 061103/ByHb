package com.zhou.onexposed;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class OneHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 将包名不是 oneapp.onechain.androidapp 的应用剔除掉
        if (!lpparam.packageName.equals("oneapp.onechain.androidapp")) {
            return;
        }
        findAndHookMethod("android.database.sqlite.SQLiteDatabase",
                    lpparam.classLoader,
                    "insertWithOnConflict", //被Hook函数的名称
                    String.class,//被Hook函数的第一个参数String(视具体的函数而定)
                    String.class,//被Hook函数的第二个参数String(视具体的函数而定)
                    ContentValues.class,//被Hook函数的第三个参数String(视具体的函数而定)
                    int.class,//被Hook函数的第四个参数String(视具体的函数而定)
                    new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // Hook函数之前执行的代码
                        }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // Hook函数之后执行的代码
                XposedBridge.log("------------------------insert start---------------------" + "\n\n");
                XposedBridge.log("param args0:" + (String)param.args[0]);
                XposedBridge.log("param args1:" + (String)param.args[1]);
                ContentValues contentValues = (ContentValues) param.args[2];
                int  shu=(int) param.args[3];
                XposedBridge.log("param args3 contentValues:"+contentValues);
                XposedBridge.log("获取的整数值"+shu);
                for (Map.Entry<String, Object> item : contentValues.valueSet())
                {
                    if (item.getValue() != null) {
                        XposedBridge.log(item.getKey() + "---------" + item.getValue().toString());
                    } else {
                        XposedBridge.log(item.getKey() + "---------" + "null");
                    }
                }

                XposedBridge.log("------------------------insert over---------------------" + "\n\n");
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
