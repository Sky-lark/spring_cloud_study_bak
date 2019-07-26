package com.leyou.cart.service;

import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:uid:";

    public void addCart(Cart cart) {
        // 获取登入用户
        UserInfo userInfo = UserInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        String hashKey = cart.getSkuId().toString();
        Integer num = cart.getNum();
        // 判断购物车商品是否存在
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        if (operations.hasKey(hashKey)) {
            // 是，修改数量
            String json = operations.get(hashKey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        // 写入redis
        operations.put(hashKey, JsonUtils.serialize(cart));
    }

    /**
     * 获取购物车数据
     * @return
     */
    public List<Cart> queryCartList() {
        UserInfo userInfo = UserInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        if (!redisTemplate.hasKey(key)) {
            // key不存在返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Cart> carts = operations.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class))
                .collect(Collectors.toList());
        return carts;
    }

    public void updateCartNum(Long skuId, int num) {
        UserInfo userInfo = UserInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        String hashKey = skuId.toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        if (!operations.hasKey(hashKey)) {
            // key不存在返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        Cart cart = JsonUtils.parse(operations.get(hashKey).toString(), Cart.class);
        // todo 判断库存
        cart.setNum(num);
        operations.put(hashKey,JsonUtils.serialize(cart));
    }

    public void deleteCart(Long skuId) {
        UserInfo userInfo = UserInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        String hashKey = skuId.toString();
        redisTemplate.opsForHash().delete(key, hashKey);
    }
}
