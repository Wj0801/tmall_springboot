package com.wj.tmall.service;
import com.wj.tmall.dao.ReviewDAO;
import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.Review;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
@Service
@CacheConfig(cacheNames="reviews")
public class ReviewService {
        @Autowired
    ReviewDAO reviewDAO;
        @Autowired
    ProductService productService;

        //增加评价
    @CacheEvict(allEntries=true)
    public  void add(Review review){
        reviewDAO.save(review);
    }

    //通过产品获取所有评价
    @Cacheable(key="'reviews-pid-'+ #p0.id")
    public List<Review> list(Product product){
        List<Review> reviews=reviewDAO.findByProductOrderByIdDesc(product);
        return  reviews;
    }
    //返回某产品对应的评价数量
    @Cacheable(key="'reviews-count-pid-'+ #p0.id")
    public int getCount(Product product){
        return reviewDAO.countByProduct(product);
    }

}
