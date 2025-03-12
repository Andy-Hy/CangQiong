package com.sky.context;

//封装ThreadLocal方法
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static void setCurrentId(Long id) {
        threadLocal.set(id);    //设置当前线程的局部变量值
    }

    public static Long getCurrentId() {
        return threadLocal.get();   //返回当前线程对应的局部变量值
    }

    public static void removeCurrentId() {
        threadLocal.remove();   //移除当前线程的局部变量值
    }

}
