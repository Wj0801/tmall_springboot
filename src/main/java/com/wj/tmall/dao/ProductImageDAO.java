package com.wj.tmall.dao;

import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageDAO extends JpaRepository<ProductImage,Integer> {
    //重写的查询方法
    public List<ProductImage> findByProductAndTypeOrderByIdDesc(Product product, String type);

}
