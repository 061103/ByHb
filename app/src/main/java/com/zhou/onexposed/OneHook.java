package com.zhou.onexposed;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class OneHook implements IXposedHookLoadPackage {

    static String strClassName = "";
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("oneapp.onechain.androidapp")) {
            return;
        }
        findAndHookMethod(ClassLoader.class,"loadClass", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // Hook函数之前执行的代码
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Class<?> clazz = (Class<?>) param.getResult();
                        String strClazz = clazz.getName();
                        XposedBridge.log("LoadClass : " + strClazz);
                        // 所有的类都是通过loadClass方法加载的
                        // 过滤掉Android系统的类以及一些常见的java类库
                        if (!strClazz.contains("xposed")) {
                            // 或者只Hook加密算法类、网络数据传输类、按钮事件类等协议分析的重要类
                            // 同步处理一下
                            synchronized (this.getClass()) {
                                // 获取被Hook的目标类的名称
                                strClassName = strClazz;
                                //XposedBridge.log("HookedClass : "+strClazz);
                                // 获取到指定名称类声明的所有方法的信息
                                Method[] m = clazz.getDeclaredMethods();
                                // 打印获取到的所有的类方法的信息
                                for (int i = 0; i < m.length; i++) {
                                    //XposedBridge.log("HOOKED CLASS-METHOD: "+strClazz+"-"+m[i].toString());
                                    if (!Modifier.isAbstract(m[i].getModifiers())            // 过滤掉指定名称类中声明的抽象方法
                                            && !Modifier.isNative(m[i].getModifiers())            // 过滤掉指定名称类中声明的Native方法
                                            && !Modifier.isInterface(m[i].getModifiers())        // 过滤掉指定名称类中声明的接口方法
                                            ) {

                                        // Hook处理类方法findViewById
                                        // public final View findViewById(int id)
                                        if ((m[i].getName()).contains("findViewById")) {

                                            // 对指定名称类中声明的非抽象方法进行java Hook处理
                                            XposedBridge.hookMethod(m[i], new XC_MethodHook() {

                                                // 被java Hook的类方法执行完毕之后，打印log日志
                                                @Override
                                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                                    // 打印被java Hook的类方法的名称和参数类型等信息
                                                    XposedBridge.log("HOOKED METHOD: " + strClassName + "-" + param.method.toString() + "findViewById: " + param.args[0].toString());

                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
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
