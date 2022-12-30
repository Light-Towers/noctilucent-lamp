package com.shopping.payment.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author osmondy
 * @create 2021/4/4 18:53
 */
@Data
public class Payment implements Serializable {
    private Long id;
    private String serial;
}
