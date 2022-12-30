package com.shopping.payment.service;

import com.shopping.payment.entity.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author osmondy
 * @create 2021/4/4 19:02
 */
@RequestMapping
@FeignClient(value = "payment-service")
public interface PaymentService {

    @PostMapping(value = "/payment/create")
    Integer create(@RequestBody Payment payment);

    @GetMapping(value = "/payment/get/{id}")
    Payment getPaymentById(@PathVariable("id") Long id);
}
