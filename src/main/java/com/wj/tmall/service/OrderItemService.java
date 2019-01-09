package com.wj.tmall.service;

import com.wj.tmall.dao.OrderItemDAO;
import com.wj.tmall.pojo.OrderItem;
import com.wj.tmall.pojo.Order;
import java.util.List;

import com.wj.tmall.util.SpringContextUtil;
import org.springframework.cache.annotation.Cacheable;
import com.wj.tmall.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import com.wj.tmall.pojo.User;

@Service
@CacheConfig(cacheNames="orderItems")
public class OrderItemService {
        @Autowired
    OrderItemDAO orderItemDAO;
        @Autowired
    ProductImageService productImageService;

    //增加
    @CacheEvict(allEntries=true)
    public  void add(OrderItem orderItem){
        orderItemDAO.save(orderItem);
    }

    //修改
    @CacheEvict(allEntries=true)
    public  void update(OrderItem orderItem){
        orderItemDAO.save(orderItem);
    }

    //编辑
    @Cacheable(key="'orderItems-one-'+ #p0")
    public OrderItem get(int id){
        return orderItemDAO.findOne(id);
    }

    //删除
    @CacheEvict(allEntries=true)
    public void delete(int id){
        orderItemDAO.delete(id);
    }


    public void fill(List<Order> orders) {
        for (Order order : orders)
            fill(order);
    }

    //计算订单总数，总金额等等，
    public void fill(Order order) {
        //缓存管理 的方法，不能够直接调用，需要通过一个工具，再拿一次 Service， 然后再调用
        OrderItemService orderItemService = SpringContextUtil.getBean(OrderItemService.class);
        List<OrderItem> orderItems = orderItemService.listByOrder(order);
        float total = 0;
        int totalNumber = 0;
        for (OrderItem oi :orderItems) {
            total+=oi.getNumber()*oi.getProduct().getPromotePrice();
            totalNumber+=oi.getNumber();
            productImageService.setFirstProdutImage(oi.getProduct());
        }
        order.setTotal(total);
        order.setOrderItems(orderItems);
        order.setTotalNumber(totalNumber);
        order.setOrderItems(orderItems);
    }
    //查询
    @Cacheable(key="'orderItems-oid-'+ #p0.id")
    public List<OrderItem> listByOrder(Order order) {
        return orderItemDAO.findByOrderOrderByIdDesc(order);
    }


    //根据产品获取订单项orderitem的方法
    public int getSaleCount(Product product) {
        //缓存管理 的方法，不能够直接调用，需要通过一个工具，再拿一次 Service， 然后再调用
        OrderItemService orderItemService = SpringContextUtil.getBean(OrderItemService.class);
        List<OrderItem> ois =orderItemService.listByProduct(product);
        int result =0;
        for (OrderItem oi : ois) {
            if(null!=oi.getOrder())
                if(null!= oi.getOrder() && null!=oi.getOrder().getPayDate())
                    result+=oi.getNumber();
        }
        return result;
    }

    @Cacheable(key="'orderItems-pid-'+ #p0.id")
    public List<OrderItem> listByProduct(Product product) {
        return orderItemDAO.findByProduct(product);
    }

    //查询用户的订单项集合
    @Cacheable(key="'orderItems-uid-'+ #p0.id")
    public List<OrderItem> listByUser(User user){
        return orderItemDAO.findByUserAndOrderIsNull(user);
    }


}