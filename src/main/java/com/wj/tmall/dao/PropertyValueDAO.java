package com.wj.tmall.dao;

import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.Property;
import java.util.List;
import com.wj.tmall.pojo.PropertyValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyValueDAO extends JpaRepository<PropertyValue,Integer> {
            //根据产品查询
            List<PropertyValue> findByProductOrderByIdDesc(Product product);
            //根据产品和属性获取PropertyValue对象
            PropertyValue getByPropertyAndProduct(Property property, Product product);


}
