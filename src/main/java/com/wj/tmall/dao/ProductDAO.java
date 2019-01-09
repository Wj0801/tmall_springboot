package com.wj.tmall.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wj.tmall.pojo.Category;
import com.wj.tmall.pojo.Product;
import java.util.List;


public interface ProductDAO extends JpaRepository<Product,Integer>{
    //findByCategory基于Category进行查询，第二个参数传一个 Pageable ， 就支持分页了。
    Page<Product> findByCategory(Category category, Pageable pageable);
    //通过分类查询所有产品的方法，这里不需要分页。
    List<Product> findByCategoryOrderById(Category category);
    //根据名称进行模糊查询的方法
    List<Product> findByNameLike(String keyword, Pageable pageable);
}
