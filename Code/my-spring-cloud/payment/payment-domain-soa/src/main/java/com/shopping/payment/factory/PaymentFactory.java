package com.shopping.payment.factory;

import com.shopping.payment.entity.Payment;
import com.shopping.payment.entity.PaymentPO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author osmondy
 * @create 2021/4/5 8:20
 */
@Component
public class PaymentFactory {

    public Payment buildPayment(PaymentPO source){
        Payment target = new Payment();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public PaymentPO buildPaymentPo(Payment source){
        PaymentPO target = new PaymentPO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
