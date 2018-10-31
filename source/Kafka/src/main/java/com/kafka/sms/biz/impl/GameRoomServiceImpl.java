/***
 * @pName management
 * @name GameRoomServiceImpl
 * @user DF
 * @date 2018/8/18
 * @desc
 */
package com.kafka.sms.biz.impl;

import com.kafka.sms.biz.IGameRoomService;
import com.kafka.sms.entity.db.*;
import com.kafka.sms.entity.dbExt.GameRoomDetailInfo;
import com.kafka.sms.entity.resp.GameRoomCallbackResp;
import com.kafka.sms.exception.InfoException;
import com.kafka.sms.exception.MsgException;
import com.kafka.sms.repository.*;
import com.kafka.sms.repository.utils.ConditionUtil;
import com.kafka.sms.utils.*;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class GameRoomServiceImpl extends BaseServiceImpl<GameRoom> implements IGameRoomService {
    private final GameRoomMapper gameRoomMapper;
    private final SubareaMapper subareaMapper;
    private final GameMemberGroupMapper gameMemberGroupMapper;
    private final SettlementMapper settlementMapper;
    private final RoomCardMapper roomCardMapper;
    private final WalletMapper walletMapper;
    private final UserMapper userMapper;
    private static Integer lock = 103697367;


    @Autowired
    public GameRoomServiceImpl(GameRoomMapper gameRoomMapper, SubareaMapper subareaMapper, GameMemberGroupMapper gameMemberGroupMapper, SettlementMapper settlementMapper, RoomCardMapper roomCardMapper, WalletMapper walletMapper, UserMapper userMapper) {
        this.gameRoomMapper = gameRoomMapper;
        this.subareaMapper = subareaMapper;
        this.gameMemberGroupMapper = gameMemberGroupMapper;
        this.settlementMapper = settlementMapper;
        this.roomCardMapper = roomCardMapper;
        this.walletMapper = walletMapper;
        this.userMapper = userMapper;
    }

    /**
     * 插入数据 韦德 2018年8月18日23:01:25
     *
     * @param param
     * @return
     */
    @Override
    @Transactional
    public int insert(String token, GameRoom param){
        // 加载用户信息
        Map<String, String> map = TokenUtil.validate(token);
        if(map.isEmpty()) return 0;

        String userId = map.get("userId");
        if(userId == null || userId.isEmpty()) throw new MsgException("身份校验失败");
        //Users users = userMapper.selectByPrimaryKey(userId);

        // 每位用户只能同时创建1个房间
        int playingCount = gameMemberGroupMapper.selectRoomCount(Integer.valueOf(userId));
        if(playingCount > 0) throw new MsgException("存在未结束的游戏，创建新房间失败！");

        // 加载游戏分区
        Subareas subareas = subareaMapper.selectByPrimaryKey(param.getSubareaId());
        if(subareas == null || subareas.getIsRelation() == 1) throw new MsgException("游戏分区不存在");
        if(subareas.getIsEnable() == 0) throw new MsgException("此游戏区暂未对外开放");

        // 加载游戏大区
        Subareas area = subareaMapper.selectByPrimaryKey(param.getParentAreaId());
        if(area == null) throw new MsgException("游戏大区不存在");

        Wallets wallets = walletMapper.selectByUid(Integer.valueOf(userId));
        if(wallets == null) throw new MsgException("查询钱包数据异常");
        Double balance = wallets.getBalance();
        if(balance < area.getLimitPrice()) throw new MsgException("金币" + area.getLimitPrice().intValue() + "枚以上才可以创建哟~");

        // 创建房间
        param.setOwnerId(Integer.valueOf(userId));
        param.setStatus(0);
        param.setAddTime(new Date());
        param.setUpdateTime(new Date());
        param.setIsEnable(1);
        param.setVersion(0);
        long roomCode = 0;
        try {
            String beforeNum = String.valueOf(IdWorker.getFlowIdWorkerInstance().nextId(4)).substring(0,4);
            roomCode = Long.valueOf(beforeNum + "" + String.valueOf(RandomUtils.nextLong()).substring(0,2));
        } catch (Exception e) {
            throw new MsgException("生成房间ID失败");
        }
        param.setRoomCode(roomCode);
        param.setName(subareas.getName());

        int count = gameRoomMapper.insert(param);
        if(count == 0) throw new MsgException("创建房间失败");


        // 加入成员
        GameMemberGroup gameMemberGroup = new GameMemberGroup();
        gameMemberGroup.setRoomCode(roomCode);
        gameMemberGroup.setUserId(Integer.valueOf(userId));
        gameMemberGroup.setIsOwner(1);
        gameMemberGroup.setIsConfirm(0);
        gameMemberGroup.setAddTime(new Date());
        //gameMemberGroup.setParentId(users.getParentId());
        count = 0;
        count = gameMemberGroupMapper.insert(gameMemberGroup);
        if(count == 0) throw new MsgException("加入房间失败");
        return count;
    }

    /**
     * 查询数据 韦德 2018年8月19日15:46:11
     *
     * @param token
     * @return
     */
    @Override
    public List<GameRoomDetailInfo> getRoomList(String token) {
        Map<String, String> map = TokenUtil.validate(token);
        if(map.isEmpty()) return null;

        String userId = map.get("userId");
        if(userId == null || userId.isEmpty()) throw new MsgException("身份校验失败");

        return gameRoomMapper.selectByUid(Integer.valueOf(userId));
    }

    /**
     * 解散房间 韦德 2018年8月20日01:05:48
     *
     * @param token
     * @param param
     */
    @Override
    @Transactional
    public void disband(String token, GameRoom param) {
        /*
            1、判断房间内是否有别人加入
            2、将房间status改为解散，前台不再显示
            3、更新member的退出时间
         */
        Map<String, String> map = TokenUtil.validate(token);
        if(map.isEmpty()) return;

        String userId = map.get("userId");
        if(userId == null || userId.isEmpty()) throw new MsgException("身份校验失败");

        GameRoom gameRoom = gameRoomMapper.selectByRoomCode(param.getRoomCode());
        if(gameRoom == null) throw new MsgException("房间不存在");

        // 普通成员退出房间
        if(!gameRoom.getOwnerId().equals(Integer.valueOf(userId))) {
            // 如果游戏房间正处于游戏中，此时用户想要退出房间，必须要结算后才能退出去
            if(gameRoom.getStatus()  == 2 || gameRoom.getStatus()  == 4) {
                throw new MsgException("正在游戏中，请结算后再退出房间！");
            }else if(gameRoom.getStatus() != 0){
                int isConfirm = gameMemberGroupMapper.selectConfirm(Integer.valueOf(userId), param.getRoomCode());
                if(isConfirm == 0) throw new MsgException("正在游戏中，请结算后再退出房间！");
            }

            int count = gameMemberGroupMapper.deleteMember(Integer.valueOf(userId),param.getRoomCode());
            if(count == 0) throw new MsgException("退出房间失败");

            // 加载游戏分区
            Subareas subareas = subareaMapper.selectByPrimaryKey(gameRoom.getSubareaId());
            if(subareas == null) throw new MsgException("游戏大区不存在");

            // 如果退出后房间人数不够数，把状态再改成未开始
            int onLineCount = gameRoomMapper.selectRoomOnLineCount(gameRoom.getRoomCode());
            onLineCount -= 1;
            if(onLineCount < subareas.getMaxPersonCount()) {
                gameRoom.setStatus(0);
                count = 0;
                count = gameRoomMapper.update(gameRoom);
                if(count == 0) throw new MsgException("更改显示状态失败");
            }
            return;
        }

        List<GameMemberGroup> memberGroupList = gameMemberGroupMapper.selectByRoomCode(gameRoom.getRoomCode());
        if(!(memberGroupList == null || memberGroupList.isEmpty() )) throw new MsgException("请等房间内的成员全部退出后再解散");

        int count = gameMemberGroupMapper.updateMemberExit(gameRoom.getRoomCode());
        if(count == 0) throw new MsgException("清空成员失败");


        gameRoom.setStatus(6);
        gameRoom.setIsEnable(0);
        count = 0;
        count = gameRoomMapper.update(gameRoom);
        if(count == 0) throw new MsgException("解散房间失败");

    }


    /**
     * 申请结算 韦德 2018年8月20日01:07:26
     *
     * @param token
     * @param roomCode
     * @param grade
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public GameRoomCallbackResp closeAccounts(String token, Long roomCode, Double grade) {
        Map<String, String> map = TokenUtil.validate(token);
        if(map.isEmpty()) return null;

        String userId = map.get("userId");
        if(userId == null || userId.isEmpty()) throw new MsgException("身份校验失败");

        GameRoom room = gameRoomMapper.selectByRoomCode(roomCode);
        if(room == null) throw new MsgException("房间不存在");
        if(room.getStatus() == 0) throw new MsgException("游戏未开始");
        // if(room.getStatus() == 5) throw new MsgException("该房间已结算");

        Subareas subareas = subareaMapper.selectByPrimaryKey(room.getParentAreaId());
        if(subareas == null) throw new MsgException("游戏分区不存在");

        int count = gameMemberGroupMapper.updateConfirm(Integer.valueOf(userId), roomCode);
        if(count == 0) throw new MsgException("更新结算状态失败[A01]");

        Settlement settlement = new Settlement();
        settlement.setUserId(Integer.valueOf(userId));
        settlement.setGrade(grade);
        settlement.setRoomCode(roomCode);
        settlement.setState(0);
        settlement.setAddTime(new Date());
        settlement.setUpdateTime(new Date());
        count = settlementMapper.insert(settlement);
        if(count == 0) throw new MsgException("申请结算失败");

        // TODO 加锁
        synchronized (lock){
            List<GameMemberGroup> memberGroupList = gameMemberGroupMapper.selectNotSettlementByRoomCode(roomCode);
            if(memberGroupList == null || memberGroupList.isEmpty()){
                room.setStatus(4);
                room.setIsEnable(0);
                count = gameRoomMapper.update(room);
                if(count == 0) throw new MsgException("解散房间失败");
                GameRoomCallbackResp gameRoomCallbackResp = new GameRoomCallbackResp(Integer.valueOf(userId), room, subareas);
                return gameRoomCallbackResp;
            }
        }


        return new GameRoomCallbackResp(-1, null, null);
    }

    /**
     * 申请结算 韦德 2018年8月29日20:54:47
     *
     * @param roomCode
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public GameRoomCallbackResp closeAccounts(Long roomCode) {
        GameRoom room = gameRoomMapper.selectByRoomCode(roomCode);
        if(room == null) throw new MsgException("房间不存在");
        if(room.getStatus() == 0) throw new MsgException("游戏未开始");
        if(room.getStatus() == 5) throw new MsgException("该房间已结算");

        Subareas subareas = subareaMapper.selectByPrimaryKey(room.getParentAreaId());
        if(subareas == null) throw new MsgException("游戏分区不存在");

        GameMemberGroup gameMemberGroup = new GameMemberGroup();
        gameMemberGroup.setRoomCode(roomCode);
        gameMemberGroup.setIsConfirm(1);
        int count = gameMemberGroupMapper.updateByRoomCode(gameMemberGroup);
        if(count == 0) throw new MsgException("更新结算状态失败[A01]");

        Settlement settlement = new Settlement();
        settlement.setRoomCode(roomCode);
        settlement.setState(1);
        count = 0;
        count = settlementMapper.updateByRoomCode(settlement);
        if(count == 0) throw new MsgException("更新结算状态失败[A02]");

        // TODO 加锁
        synchronized (lock) {
            List<GameMemberGroup> memberGroupList = gameMemberGroupMapper.selectNotSettlementByRoomCode(roomCode);
            if(memberGroupList == null || memberGroupList.isEmpty()){
                room.setStatus(4);
                room.setIsEnable(0);
                count = gameRoomMapper.update(room);
                if(count == 0) throw new MsgException("解散房间失败");
                GameRoomCallbackResp gameRoomCallbackResp = new GameRoomCallbackResp(0, room, subareas);
                return gameRoomCallbackResp;
            }
        }


        return new GameRoomCallbackResp(-1, null, null);
    }


    /**
     * 获取所有游戏房间 韦德 2018年8月20日21:20:09
     *
     * @return
     */
    @Override
    public List<GameRoomDetailInfo> getAllRoomList() {
        return gameRoomMapper.selectSalaRoomList();
    }

    /**
     * 加入房间 韦德 2018年8月20日21:40:51
     *
     * @param token
     * @param gameRoom
     */
    @Override
    public boolean join(String token, GameRoom gameRoom) {
        // 加载用户信息
        Map<String, String> map = TokenUtil.validate(token);
        if(map.isEmpty()) return false;

        String userId = map.get("userId");
        if(userId == null || userId.isEmpty()) throw new MsgException("身份校验失败");

        GameRoom room = gameRoomMapper.selectByRoomCode(gameRoom.getRoomCode());
        if(room == null) throw new MsgException("房间不存在");

        if(room.getOwnerId().equals(Integer.valueOf(userId))) throw new MsgException("您是此房间的房主！");

        // 每位用户只能同时加入1个房间
        int roomCount = gameMemberGroupMapper.selectRoomCount(Integer.valueOf(userId));
        if(roomCount > 0) throw new MsgException("存在未结束的游戏，加入房间失败！");

        // 加载游戏大区
        Subareas area = subareaMapper.selectByPrimaryKey(room.getParentAreaId());
        if(area == null) throw new MsgException("游戏大区不存在");
        if(area.getIsEnable() == 0) throw new MsgException("此游戏区暂未对外开放");

        // 加载钱包信息
        Wallets wallets = walletMapper.selectByUid(Integer.valueOf(userId));
        if(wallets == null) throw new MsgException("查询钱包数据异常");
        Double balance = wallets.getBalance();
        if(balance < area.getLimitPrice()) throw new MsgException("金币" + area.getLimitPrice().intValue() + "枚以上才可以加入哟~");

        // 加载游戏分区
        Subareas subareas = subareaMapper.selectByPrimaryKey(room.getSubareaId());
        if(subareas == null) throw new MsgException("游戏大区不存在");
        int onLineCount = gameRoomMapper.selectRoomOnLineCount(gameRoom.getRoomCode());
        onLineCount += 1;
        if(onLineCount > subareas.getMaxPersonCount()) throw new MsgException("该房间人数已满！");


        // 更改状态为已开始
        if(onLineCount == subareas.getMaxPersonCount()){
            int count = gameRoomMapper.updateStatusByRoomCode(gameRoom.getRoomCode(), 2);
            if(count == 0) throw new MsgException("更改显示状态失败");
        }

        // 加入成员
        GameMemberGroup gameMemberGroup = new GameMemberGroup();
        gameMemberGroup.setRoomCode(gameRoom.getRoomCode());
        gameMemberGroup.setUserId(Integer.valueOf(userId));
        gameMemberGroup.setIsOwner(0);
        gameMemberGroup.setIsConfirm(0);
        gameMemberGroup.setAddTime(new Date());
        int count = gameMemberGroupMapper.insert(gameMemberGroup);
        if(count == 0) throw new MsgException("加入房间失败");
        return true;
    }

    /**
     * 领取房卡 韦德 2018年8月20日23:28:15
     *
     * @param token
     * @param users
     */
    @Override
    public void getRoomCard(String token, Users users) {
        // 加载用户信息
        Map<String, String> map = TokenUtil.validate(token);
        if(map.isEmpty()) return;

        String userId = map.get("userId");
        if(userId == null || userId.isEmpty()) throw new MsgException("身份校验失败");

        // 查询此用户是否绑定了熊猫id
        Users user = userMapper.selectByPrimaryKey(Integer.valueOf(userId));
        if(user == null) throw new MsgException("查询用户信息失败");
        System.out.println("数据埋点:" + JsonUtil.getJsonNotEscape(user));


        if(user.getPandaId() == null || user.getPandaId().isEmpty()) throw new MsgException("未绑定熊猫麻将账户id");

        RoomCard roomCard = roomCardMapper.selectLast(Integer.valueOf(userId), user.getPandaId());
        if(roomCard != null){
            long nd = 1000 * 24 * 60 * 60;
            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;

            Long currentTime = new Date().getTime();
            Long addTime = roomCard.getAddTime().getTime();
            // 获得两个时间的毫秒时间差异
            Long diff = currentTime - addTime;
            // 计算差多少天
            long day = diff / nd;
            // 计算差多少小时
            long hour = diff % nd / nh;
            // 计算差多少分钟
            long min = diff % nd % nh / nm;

            if(hour <= 3) throw new MsgException("限隔180分钟领取一次~");
        }else{
            Wallets wallets = walletMapper.selectByUid(Integer.valueOf(userId));
            if(wallets == null) throw new MsgException("查询钱包数据异常");
            Double balance = wallets.getBalance();
            if(balance < 100) throw new MsgException("金币100枚以上才可以领取哟~");

            // TODO 这里要推送熊猫服务器才能决定此次是否成功
            roomCard = new RoomCard();
            roomCard.setUserId(Integer.valueOf(userId));
            roomCard.setState(0);
            roomCard.setAddTime(new Date());
            roomCard.setPandaId(user.getPandaId());
            int count = roomCardMapper.insert(roomCard);
            if(count == 0) throw new MsgException("领取失败");
        }
    }

    /**
     * 根据分区匹配房间 韦德 2018年8月21日17:11:02
     *
     * @param parentAreaId
     * @param subareasId
     * @return
     */
    @Override
    public GameRoomDetailInfo getLimitRoom(String parentAreaId, String subareasId) {
        if(StringUtil.isBlank(parentAreaId) || StringUtil.isBlank(subareasId))
            return gameRoomMapper.getFirstRoom();
        return gameRoomMapper.getLimitRoom(parentAreaId,subareasId);
    }

    /**
     * 查询房间人数 韦德 2018年8月24日15:48:45
     *
     * @param roomCode
     */
    @Override
    public Integer getPersonCount(String roomCode) {
        return gameMemberGroupMapper.selectPersonCount(roomCode);
    }

    /**
     * 乐观锁更新房间状态 韦德 2018年9月4日20:07:31
     * @param roomCode
     * @param status
     * @return
     */
    @Override
    public int updateStatusCodeVersion(Long roomCode, int status) {
        return gameRoomMapper.updateStatusByRoomCode(roomCode,status);
    }

    /**
     * 强制结算 韦德 2018年9月2日12:58:00
     *
     * @param roomCode
     * @return
     */
    @Override
    @Transactional
    public void executeCloseAccounts(Long roomCode, Consumer<GameRoomCallbackResp> callback) {
        // 验证数据有效性
        GameRoom room = gameRoomMapper.selectByRoomCode(roomCode);
        if(room == null) throw new MsgException("房间不存在");
        if(room.getStatus() == 0) throw new MsgException("游戏未开始");
        Subareas subareas = subareaMapper.selectByPrimaryKey(room.getParentAreaId());
        if(subareas == null) throw new MsgException("游戏分区不存在");


        // 修改游戏房间状态
        GameMemberGroup gameMemberGroup = new GameMemberGroup();
        gameMemberGroup.setRoomCode(roomCode);
        gameMemberGroup.setIsConfirm(1);
        int count = gameMemberGroupMapper.updateByRoomCode(gameMemberGroup);
        if(count == 0) throw new MsgException("更新结算状态失败[A01]");

        // 修改票单状态
        Settlement settlement = new Settlement();
        settlement.setRoomCode(roomCode);
        settlement.setState(1);
        count = 0;
        count = settlementMapper.updateByRoomCode(settlement);
        if(count == 0) throw new MsgException("更新结算状态失败[A02]");

        // 解散房间
        count = 0;
        room.setStatus(5);
        room.setIsEnable(0);
        count = gameRoomMapper.update(room);
        if(count == 0) throw new MsgException("更新结算状态失败[A03]");

        GameRoomCallbackResp gameRoomCallback = new GameRoomCallbackResp();
        gameRoomCallback.setGameRoom(room);
        gameRoomCallback.setSubareas(subareas);
        callback.accept(gameRoomCallback);
    }

    /**
     * 根据房间号查询房间 韦德 2018年9月2日14:13:30
     *
     * @param roomCode
     * @return
     */
    @Override
    public GameRoom getRoom(Long roomCode) {
        return gameRoomMapper.selectByRoomCode(roomCode);
    }

    /**
     * 强制解散房间 韦德 2018年9月2日20:13:45
     *
     * @param roomCode
     */
    @Override
    @Transactional
    public void disbandRoom(Long roomCode) {
        int count = gameMemberGroupMapper.updateMemberExit(roomCode);

        GameRoom gameRoom = gameRoomMapper.selectByRoomCode(roomCode);

        gameRoom.setStatus(6);
        gameRoom.setIsEnable(0);
        count = 0;
        count = gameRoomMapper.update(gameRoom);
        if(count == 0) throw new MsgException("解散房间失败");
    }


    /**
     * 分页加载 韦德 2018年8月30日11:29:00
     *
     * @param page
     * @param limit
     * @param condition
     * @param state
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public List<GameRoomDetailInfo> getLimit(Integer page, String limit, String condition, Integer state, String beginTime, String endTime) {
        // 计算分页位置
        page = ConditionUtil.extractPageIndex(page, limit);
        String where = extractLimitWhere(condition, state, beginTime, endTime);
        List<GameRoomDetailInfo> list = gameRoomMapper.selectLimit(page, limit, state, beginTime, endTime, where);
        return list;
    }

    /**
     * 加载总记录数 韦德 2018年8月30日11:29:11
     *
     * @return
     */
    @Override
    public Integer getCount() {
        return gameRoomMapper.selectCount(new GameRoom());
    }

    /**
     * 加载分页记录数 韦德 2018年8月30日11:29:22
     *
     * @param condition
     * @param state
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public Integer getLimitCount(String condition, Integer state, String beginTime, String endTime) {
        String where = extractLimitWhere(condition, state, beginTime, endTime);
        return gameRoomMapper.selectLimitCount(state, beginTime, endTime, where);
    }

    /**
     * 检查房间是否过期 韦德 2018年9月9日18:39:58
     *
     * @param roomCode
     */
    @Override
    public Boolean checkRoomExpire(Long roomCode) {
        GameRoom gameRoom = gameRoomMapper.selectByRoomCode(roomCode);
        if(gameRoom == null) throw new MsgException("查询房间失败");

        int onLineCount = gameRoomMapper.selectRoomOnLineCount(gameRoom.getRoomCode());

        // 判断房间是否已经过期（每个房间的存活期只有5分钟，5分钟后房间如果还是只有房主，系统自动解散并且不扣金币）
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = DateUtil.getDiff(gameRoom.getAddTime());
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        if((day > 0 || hour > 0 || min > 30) && onLineCount <= 1) {
            gameMemberGroupMapper.updateMemberExit(gameRoom.getRoomCode());
            gameRoom.setStatus(6);
            gameRoom.setIsEnable(0);
            int count = gameRoomMapper.update(gameRoom);
            if(count == 0) throw new InfoException("解散房间失败");
            throw new InfoException("由于5分钟内未拼桌成功，系统已自动将该房间解散！");
        }

        return true;
    }

    /**
     * 提取分页条件
     * @return
     */
    private String extractLimitWhere(String condition, Integer isEnable,  String beginTime, String endTime) {
        // 查询模糊条件
        String where = " 1=1";
        if(condition != null) {
            condition = condition.trim();
            where += " AND (" + ConditionUtil.like("room_id", condition, true, "t1");
            if (condition.split("-").length == 2){
                where += " OR " + ConditionUtil.like("add_time", condition, true, "t1");
                where += " OR " + ConditionUtil.like("update_time", condition, true, "t1");
            }
            where += " OR " + ConditionUtil.like("name", condition, true, "t1");
            where += " OR " + ConditionUtil.like("external_room_id", condition, true, "t1") + ")";
        }

        // 查询全部数据或者只有一类数据
        // where = extractQueryAllOrOne(isEnable, where);

        // 取两个日期之间或查询指定日期
        where = extractBetweenTime(beginTime, endTime, where);
        return where.trim();
    }


    /**
     * 提取两个日期之间的条件
     * @return
     */
    private String extractBetweenTime(String beginTime, String endTime, String where) {
        if ((beginTime != null && beginTime.contains("-")) &&
                endTime != null && endTime.contains("-")){
            where += " AND t1.add_date BETWEEN #{beginTime} AND #{endTime}";
        }else if (beginTime != null && beginTime.contains("-")){
            where += " AND t1.add_date BETWEEN #{beginTime} AND #{endTime}";
        }else if (endTime != null && endTime.contains("-")){
            where += " AND t1.add_date BETWEEN #{beginTime} AND #{endTime}";
        }
        return where;
    }


    /**
     * 查询全部数据或者只有一类数据
     * @return
     */
    private String extractQueryAllOrOne(Integer isEnable, String where) {
        if (isEnable != null && isEnable != 0){
            where += " AND t1.is_enable = #{isEnable}";
        }
        return where;
    }
}