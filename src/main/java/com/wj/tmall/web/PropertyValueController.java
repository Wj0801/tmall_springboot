package com.wj.tmall.web;

import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.PropertyValue;
import com.wj.tmall.service.ProductService;

import com.wj.tmall.service.PropertyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
public class PropertyValueController {
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ProductService productService;

    //查询 编辑属性值
    @GetMapping("/products/{pid}/propertyValues")
    public  List<PropertyValue> list(@PathVariable("pid")int pid)throws Exception{
       // 以pid获取product
        Product product=productService.get(pid);
        //进行初始化
        propertyValueService.init(product);
        //把这个产品对应的属性值都取出来返回的浏览器
        List<PropertyValue> propertyValues=propertyValueService.list(product);
        return propertyValues;
    }
    //修改
    @PutMapping("/propertyValues")
    public Object update(@RequestBody PropertyValue propertyValue)throws Exception{
        propertyValueService.update(propertyValue);
        return propertyValue;
    }

}
