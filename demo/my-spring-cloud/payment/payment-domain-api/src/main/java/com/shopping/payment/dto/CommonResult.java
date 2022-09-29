package com.shopping.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author osmondy
 * @create 2021/4/4 18:53
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult <T>{


    private Integer code;
    private String message;
    private T data;

    public CommonResult(Integer code,String message){
        this(code,message,null);
    }
}
