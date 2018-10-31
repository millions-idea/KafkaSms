/***
 * @pName management
 * @name Subareas
 * @user DF
 * @date 2018/8/18
 * @desc
 */
package com.kafka.sms.entity.db;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_subareas")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Subareas {
    @Id
    private Integer subareaId;
    private Integer parentId;
    private Integer isLeaf;
    private Integer isRelation;
    private String name;
    private String backgroundImage;
    private Date addTime;
    @Column(name = "max_person_count")
    private Integer maxPersonCount;
    private Double price;
    private Double limitPrice;
    private Double reducePrice;
    private Integer isEnable;
}