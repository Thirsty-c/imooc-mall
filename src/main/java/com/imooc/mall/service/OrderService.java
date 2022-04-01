package com.imooc.mall.service;

import com.imooc.mall.model.request.CreateOrderReq;
import com.imooc.mall.model.vo.CartVo;

import java.util.List;

/**
 *   订单Service
 */
public interface OrderService {


    String create(CreateOrderReq createOrderReq);

}
