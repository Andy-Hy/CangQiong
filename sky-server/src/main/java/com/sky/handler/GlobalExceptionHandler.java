package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        // Duplicate entry 'admin' for key 'employee.idx_username'
        String message = ex.getMessage();   //定义message，得到异常信息
        if(message.contains("Duplicate entry")){    //若信息中包含Duplicate entry这句话
            String[] split = message.split(" ");    //将读取的内容放入数组中
            String username = split[2]; //声明username存放进入数组的第三位
            String msg = username + MessageConstant.ALREADY_EXISTS; //String msg = username + "用户名已存在";使用常量定义，规范代码书写
            return Result.error(msg);   //输出报错内容
        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR); //return Result.error("未知错误");规范代码书写
        }
    }

}
