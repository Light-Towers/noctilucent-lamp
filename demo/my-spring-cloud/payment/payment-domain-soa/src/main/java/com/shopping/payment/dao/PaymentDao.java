package com.shopping.payment.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopping.payment.entity.PaymentPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author osmondy
 * @create 2021/4/4 18:55
 */
@Mapper
public interface PaymentDao extends BaseMapper<PaymentPO> {
}
