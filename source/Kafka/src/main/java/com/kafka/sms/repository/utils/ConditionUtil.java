/***
 * @pName proback
 * @name ConditionUtil
 * @user DF
 * @date 2018/8/4
 * @desc SQL条件工具
 */
package com.kafka.sms.repository.utils;

/**
 * SQL条件工具
 */
public class ConditionUtil {
    /**
     * 计算分页位置
     * @param page
     * @param limit
     * @return
     */
    public static Integer extractPageIndex(Integer page, String limit) {
        if(!limit.equalsIgnoreCase("-1")){
            page = page - 1;
            if (page != 0){
                page = page * Integer.valueOf(limit);
            }
        }
        return page;
    }

    /**
     * 模糊查询某个字段
     * @param column
     * @param value
     * @param isJoin
     * @param alias
     * @return
     */
    public static String like(String column, String value, boolean isJoin, String alias){
        String condition = "";
        if (!isJoin){
            alias = "";
        }else{
            alias += ".";
        }
        String tb = alias + "`" + column + "`";
        condition += tb + " LIKE '%" + value + "' OR ";
        condition += tb + " LIKE '" + value + "%' OR ";
        condition += tb + " LIKE '%" + value + "%' ";
        return condition;
    }


    /**
     * 准确匹配
     * @param column
     * @param value
     * @param isJoin
     * @param alias
     * @return
     */
    public static String match(String column, String value, boolean isJoin, String alias){
        String condition = "";
        if (!isJoin){
            alias = "";
        }else{
            alias += ".";
        }
        String tb = alias + "`" + column + "`";
        condition += tb + " = " + value + "  ";
        return condition;
    }
}
