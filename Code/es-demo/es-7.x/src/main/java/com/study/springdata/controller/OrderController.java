package com.study.springdata.controller;

import com.study.springdata.po.Order;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    ElasticsearchRestTemplate restTemplate;

    @GetMapping("getByNo")
    public Order getByNo(String no) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.termsQuery("no", no));
        SearchHit<Order> searchRes = restTemplate.searchOne(queryBuilder.build(), Order.class);
        return searchRes == null ? null : searchRes.getContent();
    }

    /**
     * 查询金额大于100的订单
     *
     * @param page
     * @param size
     * @param amount
     * @return
     */
    @GetMapping("listGreaterThanAmount")
    public List<Order> listGreaterThanAmount(int page, int size, Double amount) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.rangeQuery("amount").gt(amount)).withPageable(PageRequest.of(page, size));
        SearchHits<Order> search = restTemplate.search(queryBuilder.build(), Order.class);
        return search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


    /**
     * 模糊查询
     * 地址中包含'北京'的订单
     *
     * @param page
     * @param size
     * @param address
     * @return
     */
    @GetMapping("listMatchAddress")
    public List<Order> listMatchAddress(int page, int size, String address) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("address", address)).withPageable(PageRequest.of(page, size));
        SearchHits<Order> search = restTemplate.search(queryBuilder.build(), Order.class);
        return search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    /**
     * 查询5月份下单的待签收的订单
     *
     * @param page
     * @param size
     * @param startTime
     * @param endTime
     * @param status
     * @return
     */
    @GetMapping("listRangeTimeAndStatus")
    public List<Order> listRangeTimeAndStatus(@RequestParam Integer page, @RequestParam Integer size,
                                              @RequestParam String startTime, @RequestParam String endTime, @RequestParam Integer status) {
        //构建查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder
                .must(QueryBuilders.termsQuery("status", new int[]{status}))
                // 时间范围查询
                .must(QueryBuilders.rangeQuery("create_time").gte(startTime).lt(endTime));
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 分页
        queryBuilder.withQuery(boolQueryBuilder).withPageable(PageRequest.of(page, size));
        System.out.println("1111111111 " + queryBuilder.build().getQuery());
        SearchHits<Order> search = restTemplate.search(queryBuilder.build(), Order.class);
        return search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


}
