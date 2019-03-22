package com.zhou.biyongxposed;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class OneHook implements IXposedHookLoadPackage {
    static String strClassName = "";
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("org.telegram.btcchat")) {
            XposedBridge.log("包名--:"+lpparam.packageName+"--正确.");
            XposedHelpers.findAndHookMethod(ClassLoader.class,"loadClass",String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // 获取指定名称的类加载之后的Class<?>
                    Class<?> clazz = (Class<?>) param.getResult();
                    // 获取加载的指定类的名称
                    String strClazz = clazz.getName();
                    XposedBridge.log("LoadClass : " + strClazz);
                    // 所有的类都是通过loadClass方法加载的
                    // 获取被Hook的目标类的名称
                            strClassName = strClazz;
                            //XposedBridge.log("HookedClass : "+strClazz);
                            // 获取到指定名称类声明的所有方法的信息
                            Method[] m = clazz.getDeclaredMethods();
                            // 打印获取到的所有的类方法的信息
                            for (int i = 0; i < m.length; i++) {

                                //XposedBridge.log("HOOKED CLASS-METHOD: "+strClazz+"-"+m[i].toString());
                                if (!Modifier.isAbstract(m[i].getModifiers())           // 过滤掉指定名称类中声明的抽象方法
                                        && !Modifier.isNative(m[i].getModifiers())     // 过滤掉指定名称类中声明的Native方法
                                        && !Modifier.isInterface(m[i].getModifiers())  // 过滤掉指定名称类中声明的接口方法
                                        ) {

                                    // 对指定名称类中声明的非抽象方法进行java Hook处理
                                    XposedBridge.hookMethod(m[i], new XC_MethodHook() {

                                        // 被java Hook的类方法执行完毕之后，打印log日志
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                            // 打印被java Hook的类方法的名称和参数类型等信息
                                            XposedBridge.log("HOOKED METHOD: " + strClassName + "-" + param.method.toString());
                                        }
                                    });

                                }
                            }
                        }
                    });
        }
    }
}