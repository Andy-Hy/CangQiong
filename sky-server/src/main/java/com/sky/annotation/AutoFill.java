package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某方法需进行功能字段自动填充处理
 * @author admin
 * @date 2025/3/15 17:36
 */
@Target(ElementType.METHOD) //指定注解只能加在ElementType.METHOD上
@Retention(RetentionPolicy.RUNTIME) //固定写法，AOP使用。保留时间(生命周期)
public @interface AutoFill {
    //指定数据库操作类型：UPDATE、INSERT
    OperationType value();
}
