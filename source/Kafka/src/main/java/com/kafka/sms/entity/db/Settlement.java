/***
 * @pName management
 * @name Settlement
 * @user DF
 * @date 2018/8/20
 * @desc
 */
package com.kafka.sms.entity.db;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_settlement")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Settlement {
    @Id
    private Integer settlementId;
    private Integer userId;
    private Long roomCode;
    private Double grade;
    private Integer state;
    private Date addTime;
    private Date updateTime;
    private String remark;
}