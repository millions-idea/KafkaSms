/***
 * @pName management
 * @name GameRoomMapper
 * @user DF
 * @date 2018/8/18
 * @desc
 */
package com.kafka.sms.repository;

import com.kafka.sms.entity.db.GameRoom;
import com.kafka.sms.entity.dbExt.GameRoomDetailInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GameRoomMapper extends MyMapper<GameRoom> {
    @Update("UPDATE tb_game_room SET `status`=#{status} WHERE room_code=#{roomCode}")
    /**
     * 解散房间 韦德 2018年9月4日15:47:09
     */
    int updateStatusByRoomCode(@Param("roomCode") Long roomCode, @Param("status") Integer status);


    @Select("SELECT t1.* " +
            ",(SELECT COUNT(*) FROM tb_game_member_group WHERE room_code = t1.room_code AND is_confirm = 0) AS personCount " +
            ",(SELECT is_owner FROM tb_game_member_group WHERE user_id = #{userId} AND room_code = t1.room_code) AS isOwner " +
            "FROM tb_game_room t1 LEFT JOIN tb_game_member_group t2 ON t1.room_code = t2.room_code WHERE t2.user_id=#{userId} AND (`status` != 6 AND `status` != 5) ORDER BY t1.add_time DESC")
    /**
     * 根据userId查询房间列表 韦德 2018年8月19日15:54:57
     * @param userId
     * @return
     */
    List<GameRoomDetailInfo> selectByUid(@Param("userId") Integer userId);

    @Select("SELECT * FROM tb_game_room WHERE room_code = #{roomCode}")
    /**
     * 查询 韦德 2018年8月20日10:42:28
     * @param roomCode
     * @return
     */
    GameRoom selectByRoomCode(@Param("roomCode") Long roomCode);


    @Select("SELECT t1.* " +
            ",(SELECT max_person_count FROM tb_subareas WHERE subarea_id = t1.subarea_id) AS maxPersonCount " +
            ",(SELECT COUNT(*) FROM tb_game_member_group WHERE room_code = t1.room_code AND is_confirm = 0) AS personCount " +
            ",(SELECT is_owner FROM tb_game_member_group WHERE user_id = t1.owner_id AND room_code = t1.room_code) AS isOwner  " +
            ",(SELECT `name` FROM tb_subareas WHERE subarea_id = t1.parent_area_id) AS parentName " +
            ",(SELECT `price` FROM tb_subareas WHERE subarea_id = t1.parent_area_id) AS parentPrice " +
            "FROM tb_game_room t1 WHERE `status` = 0 ORDER BY t1.add_time DESC")
    /**
     * 查询游戏大厅内显示的房间列表 韦德 2018年8月20日21:21:04
     * @return
     */
    List<GameRoomDetailInfo> selectSalaRoomList();


    @Select("SELECT COUNT(*) FROM tb_game_member_group WHERE room_code = #{roomCode}")
    /**
     * 查询房间在线人数 韦德 2018年8月20日22:18:54
     * @param roomCode
     * @return
     */
    int selectRoomOnLineCount(@Param("roomCode") Long roomCode);

    @Select("SELECT * FROM tb_game_room WHERE `status` = 0 AND is_enable = 1 AND parent_area_id = #{parentAreaId} AND subarea_id = #{subareasId} LIMIT 1;")
    /**
     * 根据分区匹配合适的房间 韦德 2018年8月21日17:11:31
     * @param parentAreaId
     * @param subareasId
     * @return
     */
    GameRoomDetailInfo getLimitRoom(@Param("parentAreaId") String parentAreaId , @Param("subareasId") String subareasId);


    @Select("SELECT * FROM tb_game_room WHERE `status` = 0 AND is_enable = 1  LIMIT 1;")
    /**
     * 根据分区匹配合适的房间 韦德 2018年10月18日21:36:14
     * @param parentAreaId
     * @param subareasId
     * @return
     */
    GameRoomDetailInfo getFirstRoom();

    @Update("UPDATE tb_game_room SET `status`=#{status},`is_enable`=#{isEnable}, version = version + 1 WHERE room_code=#{roomCode} AND version = #{version}")
    /**
     * 解散房间 韦德 2018年9月4日15:47:09
     */
    int update(GameRoom room);




    @Select("SELECT t1.*,t2.name AS parentAreaName,t3.name AS subareaName FROM tb_game_room t1 " +
            "LEFT JOIN tb_subareas t2 ON t1.parent_area_id = t2.subarea_id " +
            "LEFT JOIN tb_subareas t3 ON t1.subarea_id = t3.subarea_id " +
            "WHERE ${condition} GROUP BY t1.room_id ORDER BY t1.add_time DESC LIMIT #{page},${limit}")
    /**
     * 分页查询 韦德 2018年8月30日11:33:22
     * @param page
     * @param limit
     * @param state
     * @param beginTime
     * @param endTime
     * @param where
     * @return
     */
    List<GameRoomDetailInfo> selectLimit(@Param("page") Integer page, @Param("limit") String limit
            , @Param("isEnable") Integer isEnable
            , @Param("beginTime") String beginTime
            , @Param("endTime") String endTime
            , @Param("condition") String condition);

    @Select("SELECT COUNT(t1.room_id) FROM tb_game_room t1 " +
            "WHERE ${condition}")
    /**
     * 分页查询记录数 韦德 2018年8月30日11:33:30
     * @param state
     * @param beginTime
     * @param endTime
     * @param where
     * @return
     */
    Integer selectLimitCount(@Param("isEnable") Integer isEnable
            , @Param("beginTime") String beginTime
            , @Param("endTime") String endTime
            , @Param("condition") String condition);
}
