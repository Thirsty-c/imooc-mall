package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.model.request.CreateOrderReq;
import com.imooc.mall.model.vo.CartVo;
import com.imooc.mall.model.vo.OrderVo;

import java.util.List;

/**
 *   订单Service
 */
public interface OrderService {


    String create(CreateOrderReq createOrderReq);

    OrderVo detail(String orderNo);

    PageInfo listForCustomer(Integer pageNum, Integer pageSize);

    void cancel(String orderNo);

    String qrcode(String orderNo);

    void pay(String orderNo);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    void deliver(String orderNo);

    //完结订单
    void finish(String orderNo);
}
