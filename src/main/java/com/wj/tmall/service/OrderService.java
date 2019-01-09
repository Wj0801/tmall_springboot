package com.wj.tmall.service;

import com.wj.tmall.dao.OrderDAO;
import com.wj.tmall.pojo.OrderItem;
import com.wj.tmall.util.Page4Navigator;
import com.wj.tmall.pojo.User;
import com.wj.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.wj.tmall.pojo.Order;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;import org.springframework.cache.annotation.Cacheable;


@Service
@CacheConfig(cacheNames="orders")
public class OrderService {
    //订单状态的常量
    public static final String waitPay = "waitPay";
    public static final String waitDelivery = "waitDelivery";
    public static final String waitConfirm = "waitConfirm";
    public static final String waitReview = "waitReview";
    public static final String finish = "finish";
    public static final String delete = "delete";

    @Autowired
    OrderDAO orderDAO;
    @Autowired
    OrderItemService orderItemService;

    //分页查询
    @Cacheable(key="'orders-page-'+#p0+ '-' + #p1")
    public Page4Navigator<Order> list(int start, int size, int navigatePages) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size,sort);
        Page pageFromJPA =orderDAO.findAll(pageable);
        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }


    //遍历订单
    public void removeOrderFromOrderItem(List<Order> orders) {
        for (Order order : orders) {
            removeOrderFromOrderItem(order);
        }
    }
    //把订单里的订单项的订单属性设置为空
    public void removeOrderFromOrderItem(Order order) {
        List<OrderItem> orderItems= order.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(null);
        }
    }


    //编辑
    @Cacheable(key="'orders-one-'+ #p0")
    public Order get(int oid) {
        return orderDAO.findOne(oid);
    }
    //修改
    @CacheEvict(allEntries=true)
    public void update(Order bean) {
        orderDAO.save(bean);
    }


    //增加订单，该方法通过注解进行事务管理
    @CacheEvict(allEntries=true)
    @Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
    public float add(Order order,List<OrderItem> ois){
        float total= 0 ;
        add(order);
    //故意抛出异常代码用来模拟当增加订单后出现异常，观察事务管理是否预期发生。（需要把false修改为true才能观察到）
        if(false)
            throw  new RuntimeException();
        for(OrderItem oi:ois){
            oi.setOrder(order);
            orderItemService.update(oi);
            total+=oi.getProduct().getPromotePrice()*oi.getNumber();
        }
        return total;
    }

    @CacheEvict(allEntries=true)
    public void add(Order order){
        orderDAO.save(order);
    }

    //获取某个用户的订单，但是状态又不是 "delete" 的订单。 "delete" 是作为状态调用的时候传进来的
    public List<Order> listByUserWithoutDelete(User user) {
        //缓存管理 的方法，不能够直接调用，需要通过一个工具，再拿一次 Service， 然后再调用
        OrderService orderService = SpringContextUtil.getBean(OrderService.class);
        List<Order> orders = orderService.listByUserAndNotDeleted(user);
        orderItemService.fill(orders);
        return orders;
    }

    @Cacheable(key="'orders-uid-'+ #p0.id")
    public  List<Order> listByUserAndNotDeleted(User user){
         return    orderDAO.findByUserAndStatusNotOrderByIdDesc(user,OrderService.delete);
    }




}
