package com.wj.tmall.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wj.tmall.pojo.Order;
import com.wj.tmall.pojo.User;
import java.util.List;

public interface OrderDAO extends JpaRepository<Order,Integer> {
    //获取某个用户的订单，但是状态又不是 "delete" 的订单。 "delete" 是作为状态调用的时候传进来的
    public List<Order> findByUserAndStatusNotOrderByIdDesc(User user, String status);
}
