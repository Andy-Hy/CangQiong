package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.time.LocalDateTime;


/**
 * 自定义切面。实现公共字段自动填充处理逻辑
 * @author admin
 * @date 2025/3/15 17:50
 */
@Aspect //这是切面的注解
@Component  //交给Spring容器管理
@Slf4j  //记录日志
public class AutoFillAspect {
    /**
     * 切入点
     */       //使用通配指定拦截的内容，拦截mapper下的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     */
    @Before("autoFillPointCut()")   //@Before:前置通知，此注解标注的通知方法在目标方法前被执行
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();  //方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);    //获得方法上的注解对象
        OperationType operationType = autoFill.value(); //获得数据库操作类型

        //获取到当前被拦截的方法上的参数--实体对象
        Object[] args = joinPoint.getArgs();    //使用数组存放获取到的实体对象
        if(args == null || args.length == 0){   //特殊情况
            return;
        }

        Object entity = args[0];    //不返回，就用Object接收第一个实体对象

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();    //获取当前登录的时间
        Long currentId = BaseContext.getCurrentId();    //获取当前登录用户的ID

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType == operationType.INSERT){
            //为4个公共字段赋值
            try {
                /* Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime",LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser",Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime",LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser",Long.class);
                */  //使用已经设置在common包中设置的常量，更加规范
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                //通过反射为对象属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
