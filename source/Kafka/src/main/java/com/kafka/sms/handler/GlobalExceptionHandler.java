/***
 * @pName mi-ocr-web-captcha
 * @name GlobalExceptionHandler
 * @user DF
 * @date 2018/6/25
 * @desc
 */
package com.kafka.sms.handler;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.kafka.sms.entity.Constant;
import com.kafka.sms.entity.JsonResult;
import com.kafka.sms.exception.InfoException;
import com.kafka.sms.exception.MsgException;
import com.kafka.sms.utils.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeoutException;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    final static Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(value = InfoException.class)
    public JsonResult preException(HttpServletRequest request,
                                   InfoException exception) throws Exception {
        logger.error(String.format("自定义异常消息: %s",JsonUtil.getJsonNotEscape(exception)));
        return  new JsonResult().exceptionAsString(exception.getMsg() == null ? "错误" : exception.getMsg());
    }

    @ExceptionHandler(value = MsgException.class)
    public JsonResult preException(HttpServletRequest request,
                                   MsgException exception) throws Exception {
        logger.error(String.format("普通异常消息: %s", JsonUtil.getJsonNotEscape(exception)));
        return  new JsonResult().normalExceptionAsString(exception.getMsg() == null ? "错误" : exception.getMsg());
    }

/*    @ExceptionHandler(value = UsernameNotFoundException.class)
    public JsonResult preException(HttpServletRequest request,
                                   UsernameNotFoundException exception) throws Exception {
        logger.error(String.format("security异常消息: %s",JsonUtil.getJsonNotEscape(exception)));
        return  new JsonResult().normalExceptionAsString(exception.getMessage() == null ? "登陆失败" : exception.getMessage());
    }*/


    @ExceptionHandler(value = TimeoutException.class)
    public JsonResult preException(HttpServletRequest request,
                                 TimeoutException exception) throws Exception {
        logger.error(String.format("超时异常消息: %s",JsonUtil.getJsonNotEscape(exception)));
        return new JsonResult().failingAsString("操作超时");
    }

    @ExceptionHandler(value = RuntimeException.class)
    public JsonResult preException(HttpServletRequest request,
                                 RuntimeException exception) throws Exception {
        logger.error(String.format("运行时异常: %s",JsonUtil.getJsonNotEscape(exception)));

        // mysql异常 com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry '123456' for key 'uq_external_room_id'
        if(exception != null  && exception.getCause() != null  && exception.getMessage() != null  && exception.getCause().getMessage().contains("Duplicate entry")){
            return  new JsonResult().normalExceptionAsString("已存在");
        }

        return new JsonResult().failingAsString("环境错误");
    }

    @ExceptionHandler(value = Exception.class)
    public JsonResult preException(HttpServletRequest request,
                                 Exception exception) throws Exception {
        logger.error(String.format("全局异常: %s",JsonUtil.getJsonNotEscape(exception)));
        return new JsonResult().failingAsString("未知错误");
        //return new JsonResult().failingAsString(JsonUtil.getJsonNotEscape(exception));
    }
}

