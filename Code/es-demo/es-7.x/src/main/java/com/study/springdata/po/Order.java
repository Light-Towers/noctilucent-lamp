package com.study.springdata.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.Date;
import java.util.List;

/**
 * @author osmondy
 * 参考：https://blog.csdn.net/qq_24950043/article/details/125578663
 */
@Data
//@Document(indexName = "order_test", replicas = 0)
@Document(indexName = "order_test")
public class Order {

    @Id
    private String id;

    /**
     * 订单状态 0未付款 1未发货 2运输中 3待签收 4已签收
     */
    @Field(type = FieldType.Integer, name = "status")
    private Integer status;

    /**
     * 订单编号
     */
    @Field(type = FieldType.Keyword, name = "no")
    private String no;

    /**
     * 下单时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Field(type = FieldType.Date, name = "create_time", format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 订单金额
     */
    @Field(type = FieldType.Double, name = "amount")
    private Double amount;

    /**
     * 创建人
     */
    @Field(type = FieldType.Keyword, name = "creator")
    private String creator;
    /**
     * 坐标
      */
    @GeoPointField
    @Field(name = "point")
    private GeoPoint point;
    /**
     * 地址
     */
    @Field(type = FieldType.Text, name = "address", analyzer = "ik_max_word")
    private String address;
    /**
     * 商品
     */
    @Field(type = FieldType.Nested, name = "product")
    private List<Product> product;

}
