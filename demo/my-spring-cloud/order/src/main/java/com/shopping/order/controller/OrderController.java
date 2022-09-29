package com.shopping.order.controller;

import com.shopping.payment.dto.CommonResult;
import com.shopping.payment.entity.Payment;
import com.shopping.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author osmondy
 * @create 2021/4/4 19:10
 */
@Slf4j
@RestController
public class OrderController {

    @Resource
    PaymentService paymentService;

    @GetMapping(value = "/order/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id){
        Payment payment = paymentService.getPaymentById(id);
        log.info("*****查询结果："+payment);
        if (payment!=null){
            //说明有数据，能查询成功
            return new CommonResult(200,"查询成功",payment);
        }else {
            return new CommonResult(444,"没有对应记录，查询ID："+id,null);
        }
    }

}
