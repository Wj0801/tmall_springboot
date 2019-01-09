package com.wj.tmall.dao;

import com.wj.tmall.pojo.OrderItem;
import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.wj.tmall.pojo.Order;

public interface OrderItemDAO extends JpaRepository<OrderItem,Integer> {
        //通过订单查询的方法
         List<OrderItem> findByOrderOrderByIdDesc(Order order);
        //根据产品获取订单项orderitem的方法
        List<OrderItem> findByProduct(Product product);
        //查询用户的订单项集合
         List<OrderItem> findByUserAndOrderIsNull(User user);
}
