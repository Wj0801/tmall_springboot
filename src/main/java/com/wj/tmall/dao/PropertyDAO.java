package com.wj.tmall.dao;

import com.wj.tmall.pojo.Category;
import com.wj.tmall.pojo.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// 继承了 JpaRepository。提供常见的CRUD,JPA所谓的不用写 SQL语句。。。因为需要的信息都在方法名和参数里提供了
public interface PropertyDAO extends JpaRepository<Property,Integer>{
    //findByCategory基于Category进行查询，第二个参数传一个 Pageable ， 就支持分页了。
    Page<Property> findByCategory(Category category, Pageable pageable);
    //通过分类获取所有属性集合的方法
    List<Property> findByCategory(Category category);
}
