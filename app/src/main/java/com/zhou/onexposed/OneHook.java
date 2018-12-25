package com.zhou.onexposed;


import android.content.ContentValues;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.log;

public class OneHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("org.telegram.btcchat")) {
            XposedHelpers.findAndHookMethod("org.telegram.btcchat.database.SQLiteDatabase",lpparam.classLoader, "insertWithOnConflict",
                    String.class, String.class, ContentValues.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    log("------------------------insert start---------------------" + "\n\n");
                    log("param args1:" + (String)param.args[0]);
                    log("param args2:" + (String)param.args[1]);
                    ContentValues contentValues = (ContentValues) param.args[2];
                    log("param args3 contentValues:");
                    for (Map.Entry<String, Object> item : contentValues.valueSet())
                    {
                        if (item.getValue() != null) {
                            log(item.getKey() + "---------" + item.getValue().toString());
                        } else {
                            log(item.getKey() + "---------" + "null");
                        }
                    }

                    log("------------------------insert over---------------------" + "\n\n");
                }
            });
        }
    }
        // 获取指定名称的类声明的类成员变量、类方法、内部类的信息
        public void dumpClass (Class < ? > actions){

            log("Dump class " + actions.getName());
            log("Methods方法");
            // 获取到指定名称类声明的所有方法的信息
            Method[] m = actions.getDeclaredMethods();
            // 打印获取到的所有的类方法的信息
            for (int i = 0; i < m.length; i++) {

                log(m[i].toString());
            }

            log("Fields变量方法");
            // 获取到指定名称类声明的所有变量的信息
            Field[] f = actions.getDeclaredFields();
            // 打印获取到的所有变量的信息
            for (int j = 0; j < f.length; j++) {

                log(f[j].toString());
            }

            log("Classes内部类");
            // 获取到指定名称类中声明的所有内部类的信息
            Class<?>[] c = actions.getDeclaredClasses();
            // 打印获取到的所有内部类的信息
            for (int k = 0; k < c.length; k++) {

                log(c[k].toString());
            }
        }
    }