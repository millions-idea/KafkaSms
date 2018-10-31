/***
 * @pName management
 * @name IRoomCardService
 * @user DF
 * @date 2018/8/30
 * @desc
 */
package com.kafka.sms.biz;

import com.kafka.sms.entity.db.RoomCard;
import com.kafka.sms.entity.dbExt.RoomCardDetailInfo;
import com.kafka.sms.entity.dbExt.SettlementDetailInfo;

import java.util.List;

public interface IRoomCardService extends IBaseService<RoomCard> {

    /**
     * 分页加载数据列表 韦德 2018年8月27日00:20:54
     */
    List<RoomCardDetailInfo> getRoomCardLimit(Integer page, String limit, String condition, Integer state, String beginTime, String endTime);

    /**
     * 统计分页加载数据列表
     */
    int getRoomCardLimitCount(String condition, Integer state, String beginTime, String endTime);

    /**
     * 加载数据总条数 韦德 2018年8月27日00:21:16
     * @return
     */
    int getRoomCardCount();

    /**
     * 充值
     * @param roomCard
     */
    void recharge(RoomCard roomCard);

    /**
     * 拒绝
     * @param roomCard
     */
    void pass(RoomCard roomCard);

    /**
     * 获取最新未充值的房卡记录
     * @return
     */
    RoomCard getNewRoomCard();
}