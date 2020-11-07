package com.changgou.exception;

import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 自定义的全局异常处理类
 */
@ControllerAdvice//开启全局异常处理
public class DefaultException {

    /**
     * 捕获全局异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(Exception e){
//        e.printStackTrace();
        return new Result(false, StatusCode.ERROR, e.getMessage());
    }
}
