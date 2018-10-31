/***
 * @pName management
 * @name AreaController
 * @user DF
 * @date 2018/9/4
 * @desc
 */
package com.kafka.sms.controller;

import com.kafka.sms.biz.ISubareaService;
import com.kafka.sms.entity.JsonArrayResult;
import com.kafka.sms.entity.JsonResult;
import com.kafka.sms.entity.db.Subareas;
import com.kafka.sms.entity.dbExt.UserDetailInfo;
import com.kafka.sms.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/management/area")
public class AreaController extends BaseController{
    @Autowired
    private ISubareaService subareaService;

    @GetMapping("/index")
    public String index(){
        return "area/index";
    }

    /**
     * 分区列表 韦德 2018年9月4日09:47:35
     * @return
     */
    @GetMapping("/getAreaLimit")
    @ResponseBody
    public JsonArrayResult<Subareas> getAreaLimit(Integer page, String limit, String condition, Integer state, String beginTime, String endTime){
        Integer count = 0;
        List<Subareas> list = subareaService.getLimit(page, limit, condition, state, beginTime, endTime);
        JsonArrayResult jsonArrayResult = new JsonArrayResult(0, list);
        if (StringUtil.isBlank(condition)
                && StringUtil.isBlank(beginTime)
                && StringUtil.isBlank(endTime)
                && (state == null || state == 0)){
            count = subareaService.getCount();
        }else{
            count = subareaService.getLimitCount(condition, state, beginTime, endTime);
        }
        jsonArrayResult.setCount(count);
        return jsonArrayResult;
    }



    /**
     * 修改
     * @return
     */
    @PostMapping("/update")
    @ResponseBody
    public JsonResult update(Subareas subareas){
        subareaService.update(subareas);
        return JsonResult.successful();
    }
}
