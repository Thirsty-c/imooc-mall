package com.imooc.mall.service.Impl;


import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.filter.UserFilter;
import com.imooc.mall.model.dao.CartMapper;
import com.imooc.mall.model.dao.OrderItemMapper;
import com.imooc.mall.model.dao.OrderMapper;
import com.imooc.mall.model.dao.ProductMapper;
import com.imooc.mall.model.pojo.Order;
import com.imooc.mall.model.pojo.OrderItem;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.request.CreateOrderReq;
import com.imooc.mall.model.vo.CartVo;
import com.imooc.mall.service.CartService;
import com.imooc.mall.service.OrderService;
import com.imooc.mall.util.OrderCodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：    订单Service实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartService cartService;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CartMapper cartMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Override
    public String create(CreateOrderReq createOrderReq){
        //拿到用户ID
        Integer userId = UserFilter.currentUser.getId();

        //从购物车查找已经勾选的商品
        List<CartVo> cartVoList = cartService.list(userId);
        ArrayList<CartVo> cartVoListTemp = new ArrayList<>();
        for (int i = 0; i < cartVoList.size(); i++) {
            CartVo cartVo =  cartVoList.get(i);
            if (cartVo.getSelected().equals(Constant.Cart.CHECKED)){
                cartVoListTemp.add(cartVo);
            }
        }
        cartVoList = cartVoListTemp;
        //如果购物车已勾选的为空，报错
        if (CollectionUtils.isEmpty(cartVoList)){
            throw new ImoocMallException(ImoocMallExceptionEnum.CREATE_FAILED);
        }
        //判断商品是否存在、上下架状态、库存
        validSaleStatusAndStock(cartVoList);
        //把购物车对象转为订单item对象
        List<OrderItem> orderItemList = cartVoListToOrderItemList(cartVoList);
        //扣库存
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            int stock = product.getStock() - orderItem.getQuantity();
            if (stock < 0){
                throw new ImoocMallException(ImoocMallExceptionEnum.NOT_ENOUGH);
            }
            product.setStock(stock);
            productMapper.updateByPrimaryKeySelective(product);
        }
        //把购物车中的已勾选商品删除
        cleanCart(cartVoList);
        //生成订单
        Order order = new Order();
        //生成订单号，有独立的规律
        String orderNo = OrderCodeFactory.getOrderCode(Long.valueOf(userId));
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalPrice(totalPrice(orderItemList));
        order.setReceiverName(createOrderReq.getReceiverName());
        order.setReceiverMobile(createOrderReq.getReceiverMobile());
        order.setReceiverAddress(createOrderReq.getReceiverAddress());
        order.setOrderStatus(Constant.OrderStatusEnum.NOT_PAID.getCode());
        order.setPostage(0);
        order.setPaymentType(1);
        //插入到Order表
        orderMapper.insertSelective(order);
        //循环保存每个商品到order_item表
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            orderItem.setOrderNo(order.getOrderNo());
            orderItemMapper.insertSelective(orderItem);
        }
        //把结果返回
        return orderNo;
    }

    private Integer totalPrice(List<OrderItem> orderItemList) {
        Integer totalPrice = 0;
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    private void cleanCart(List<CartVo> cartVoList) {
        for (int i = 0; i < cartVoList.size(); i++) {
            CartVo cartVo =  cartVoList.get(i);
            cartMapper.deleteByPrimaryKey(cartVo.getId());
        }
    }

    private List<OrderItem> cartVoListToOrderItemList(List<CartVo> cartVoList) {
        List<OrderItem> orderItemList = new ArrayList<>();
        for (int i = 0; i < cartVoList.size(); i++) {
            CartVo cartVo = cartVoList.get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartVo.getProductId());
            //记录商品快照信息
            orderItem.setProductName(cartVo.getProductName());
            orderItem.setProductImg(cartVo.getProductImage());
            orderItem.setUnitPrice(cartVo.getPrice());
            orderItem.setQuantity(cartVo.getQuantity());
            orderItem.setTotalPrice(cartVo.getTotalPrice());
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

    private void validSaleStatusAndStock(List<CartVo> cartVoList) {
        for (int i = 0; i < cartVoList.size(); i++) {
            CartVo cartVo =  cartVoList.get(i);
            Product product = productMapper.selectByPrimaryKey(cartVo.getProductId());
            //判断商品是否存在，商品是否上架
            if (product == null || product.getStatus().equals(Constant.SaleStatus.NOT_SALE)){
                throw new ImoocMallException(ImoocMallExceptionEnum.NOT_SALE);
            }
            //判断商品库存
            if (cartVo.getQuantity() > product.getStock()){
                throw new ImoocMallException(ImoocMallExceptionEnum.NOT_ENOUGH);
            }
        }
    }


}
