/***
 * @pName management
 * @name GameRoomService
 * @user DF
 * @date 2018/8/18
 * @desc
 */
package com.kafka.sms.biz;

import com.kafka.sms.entity.db.GameRoom;
import com.kafka.sms.entity.db.Users;
import com.kafka.sms.entity.dbExt.GameRoomDetailInfo;
import com.kafka.sms.entity.dbExt.UserDetailInfo;
import com.kafka.sms.entity.resp.GameRoomCallbackResp;

import java.util.List;
import java.util.function.Consumer;

public interface IGameRoomService extends IBaseService<GameRoom> {
    /**
     * 插入数据 韦德 2018年8月18日23:01:25
     * @param token
     * @param param
     * @return
     */
    int insert(String token, GameRoom param);

    /**
     * 查询数据 韦德 2018年8月19日15:46:11
     * @param token
     * @return
     */
    List<GameRoomDetailInfo> getRoomList(String token);

    /**
     * 解散房间 韦德 2018年8月20日01:05:48
     * @param token
     * @param param
     */
    void disband(String token, GameRoom param);


    /**
     * 申请结算 韦德 2018年8月20日01:07:26
     * @param token
     * @param roomCode
     * @param grade
     */
    GameRoomCallbackResp closeAccounts(String token, Long roomCode, Double grade);

    /**
     * 申请结算 韦德 2018年8月29日20:54:47
     * @param roomCode
     * @param callback
     */
    GameRoomCallbackResp closeAccounts(Long roomCode);

    /**
     * 获取所有游戏房间 韦德 2018年8月20日21:20:09
     * @return
     */
    List<GameRoomDetailInfo> getAllRoomList();

    /**
     * 加入房间 韦德 2018年8月20日21:40:51
     * @param token
     * @param gameRoom
     */
    boolean join(String token, GameRoom gameRoom);

    /**
     * 领取房卡 韦德 2018年8月20日23:28:15
     * @param token
     * @param users
     */
    void getRoomCard(String token, Users users);

    /**
     * 根据分区匹配房间 韦德 2018年8月21日17:11:02
     * @param parentAreaId
     * @param subareasId
     * @return
     */
    GameRoomDetailInfo getLimitRoom(String parentAreaId, String subareasId);

    /**
     * 查询房间人数 韦德 2018年8月24日15:48:45
     * @param roomCode
     */
    Integer getPersonCount(String roomCode);

    /**
     * 乐观锁更新房间状态 韦德 2018年9月4日20:07:31
     * @param roomCode
     * @param status
     * @return
     */
    int updateStatusCodeVersion(Long roomCode, int status);

    /**
     * 强制结算 韦德 2018年9月2日12:58:00
     * @param roomCode
     * @return
     */
    void executeCloseAccounts(Long roomCode, Consumer<GameRoomCallbackResp> callback);

    /**
     * 根据房间号查询房间 韦德 2018年9月2日14:13:30
     * @param roomCode
     * @return
     */
    GameRoom getRoom(Long roomCode);

    /**
     * 强制解散房间 韦德 2018年9月2日20:13:45
     * @param roomCode
     */
    void disbandRoom(Long roomCode);

    /**
     * 分页加载 韦德 2018年8月30日11:29:00
     * @param page
     * @param limit
     * @param condition
     * @param state
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GameRoomDetailInfo> getLimit(Integer page, String limit, String condition, Integer state, String beginTime, String endTime);

    /**
     * 加载总记录数 韦德 2018年8月30日11:29:11
     * @return
     */
    Integer getCount();

    /**
     * 加载分页记录数 韦德 2018年8月30日11:29:22
     * @param condition
     * @param state
     * @param beginTime
     * @param endTime
     * @return
     */
    Integer getLimitCount(String condition, Integer state, String beginTime, String endTime);

    /**
     * 检查房间是否过期 韦德 2018年9月9日18:39:58
     * @param roomCode
     */
    Boolean checkRoomExpire(Long roomCode);

}