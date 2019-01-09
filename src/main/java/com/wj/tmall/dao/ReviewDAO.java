package com.wj.tmall.dao;

import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewDAO extends JpaRepository<Review,Integer> {
    //返回某产品对应的评价集合
    List<Review> findByProductOrderByIdDesc(Product product);
    //返回某产品对应的评价数量
    int countByProduct(Product product);

}
