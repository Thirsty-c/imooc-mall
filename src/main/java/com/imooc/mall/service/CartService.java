package com.imooc.mall.service;

import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.model.pojo.User;
import com.imooc.mall.model.vo.CartVo;

import java.util.List;

/**
 *   购物车Service
 */
public interface CartService {


    List<CartVo> list(Integer userId);

    List<CartVo> add(Integer userId, Integer productId, Integer count);

    List<CartVo> update(Integer userId, Integer productId, Integer count);

    List<CartVo> delete(Integer userId, Integer productId);

    List<CartVo> selectOrNot(Integer userId, Integer productId, Integer selected);

    List<CartVo> selectAllOrNot(Integer userId, Integer selected);
}
