package com.wj.tmall.web;

import com.wj.tmall.service.OrderItemService;
import com.wj.tmall.service.OrderService;
import com.wj.tmall.util.Page4Navigator;
import com.wj.tmall.pojo.Order;
import com.wj.tmall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
public class OrderController {

        @Autowired
    OrderService orderService;
        @Autowired
    OrderItemService orderItemService;

    @GetMapping("/orders")
    public Page4Navigator<Order> list(@RequestParam(value = "start",defaultValue = "0") int start,@RequestParam(value = "size",defaultValue = "5")int size)throws  Exception{
            start=start<0?0:start;
            Page4Navigator<Order> orderPage4Navigator=orderService.list(start,size,5);
            orderItemService.fill(orderPage4Navigator.getContent());
            orderService.removeOrderFromOrderItem(orderPage4Navigator.getContent());
            return orderPage4Navigator;
    }

    @PutMapping("/deliveryOrder/{oid}")
    public Object deleteOrder(@PathVariable(value = "oid")int oid)throws  Exception{
        Order order=orderService.get(oid);
        order.setDeliveryDate(new Date());
        order.setStatus(OrderService.waitConfirm);
        orderService.update(order);
        return Result.success();
    }

}
