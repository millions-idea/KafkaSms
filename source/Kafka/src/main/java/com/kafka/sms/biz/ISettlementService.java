/***
 * @pName management
 * @name ISettlementService
 * @user DF
 * @date 2018/8/28
 * @desc
 */
package com.kafka.sms.biz;

import com.kafka.sms.entity.db.Accounts;
import com.kafka.sms.entity.db.Settlement;
import com.kafka.sms.entity.dbExt.SettlementDetailInfo;
import com.kafka.sms.entity.resp.GameRoomCallbackResp;

import java.util.List;
import java.util.function.Consumer;

public interface ISettlementService extends IBaseService<Settlement> {
    /**
     * 加载房间成员列表 韦德 2018年8月28日22:10:25
     * @param roomCode
     * @return
     */
    List<SettlementDetailInfo> getMemberList(Long roomCode);


    /**
     * 分页加载结算申请数据列表 韦德 2018年8月27日00:20:54
     */
    List<SettlementDetailInfo> getSettlementLimit(Integer page, String limit, String condition, Integer state, String beginTime, String endTime);

    /**
     * 统计分页加载结算申请数据列表
     */
    int getSettlementLimitCount(String condition, Integer state, String beginTime, String endTime);

    /**
     * 加载结算申请数据总条数 韦德 2018年8月27日00:21:16
     * @return
     */
    int getSettlementCount();

    /**
     * 修改成绩 韦德 2018年8月29日20:31:57
     * @param userId
     * @param grade
     * @param roomCode
     */
    void editGrade(Integer userId, Double grade, Long roomCode);

    /**
     * 更新房间内成员状态 韦德 2018年8月29日22:11:17
     * @param roomCode
     * @param status
     * @return
     */
    int updateStatusByRoomCode(Long roomCode, int status);

    /**
     * 根据房间id和用户id查询成绩 韦德 2018年9月2日13:26:24
     * @param userId
     * @param roomCode
     */
    Settlement getMemberGrade(Integer userId, Long roomCode);
}