package com.shopping.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author osmondy
 * @create 2021/4/4 18:53
 */
@Data
@TableName("payment")
public class PaymentPO implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serial;
}
