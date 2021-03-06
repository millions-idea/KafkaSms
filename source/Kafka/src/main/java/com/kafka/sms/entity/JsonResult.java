/***
 * @pName management
 * @name JsonResult
 * @user DF
 * @date 2018/8/13
 * @desc
 */
package com.kafka.sms.entity;

import com.alibaba.druid.support.json.JSONUtils;
import com.kafka.sms.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JsonResult<T> {
    private Integer code;
    private Object msg;

    public JsonResult() {
    }

    public JsonResult(Integer code, Object msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 成功JSON  2018年8月13日13:03:35
     * @param msg
     * @return
     */
    public JsonResult successfulAsString(T msg){
        return new JsonResult(200, JsonUtil.getJson(msg));
    }

    public JsonResult successful(T msg){
        return new JsonResult(200, msg);
    }

    public JsonResult exceptionAsString(T msg){
        return new JsonResult(300, JsonUtil.getJson(msg));
    }

    public JsonResult normalExceptionAsString(T msg){
        return new JsonResult(400, JsonUtil.getJson(msg));
    }

    public JsonResult exception(T msg){
        return new JsonResult(300, msg);
    }

    /**
     * 成功JSON  2018年8月13日13:03:35
     * @return
     */
    public static JsonResult successful(){
        return new JsonResult(200, "success");
    }

    /**
     * 失败JSON  2018年8月13日13:03:35
     * @param msg
     * @return
     */
    public JsonResult failingAsString(T msg){
        return new JsonResult(500, JsonUtil.getJson(msg));
    }

    public JsonResult failing(T msg){
        return new JsonResult(500, msg);
    }

    /**
     * 失败JSON  2018年8月13日13:03:35
     * @return
     */
    public static JsonResult failing(){
        return new JsonResult(500, "fail");
    }
}
