package com.wj.tmall.service;


import com.wj.tmall.dao.ProductImageDAO;
import com.wj.tmall.pojo.OrderItem;
import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.ProductImage;

import com.wj.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
@Service
@CacheConfig(cacheNames="productImages")
public class ProductImageService {
    public  static  final String type_single="single";  //单个图片
    public  static  final String type_detail="detail";   //详情图片
    @Autowired
    ProductImageDAO productImageDAO;
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;

    //增加
    @CacheEvict(allEntries=true)
    public void add(ProductImage productImage){
        productImageDAO.save(productImage);
    }

    //删除
    @CacheEvict(allEntries=true)
    public void delete(int id){
        productImageDAO.delete(id);
    }

    //编辑
    @Cacheable(key="'productImages-one-'+ #p0")
    public ProductImage get(int id){
        return productImageDAO.findOne(id);
    }

    //查询单个产品图片
    @Cacheable(key="'productImages-single-pid-'+ #p0.id")
    public List<ProductImage> listSingleProductImages(Product product){

        return  productImageDAO.findByProductAndTypeOrderByIdDesc(product,type_single);
    }

    //查询详情图片
    @Cacheable(key="'productImages-detail-pid-'+ #p0.id")
    public  List<ProductImage> listDetailProductImages(Product product){
        return  productImageDAO.findByProductAndTypeOrderByIdDesc(product,type_detail);
    }


    //设置单个图片
    public void setFirstProdutImage(Product product) {
        //缓存管理 的方法，不能够直接调用，需要通过一个工具，再拿一次 Service， 然后再调用
        ProductImageService productImageService = SpringContextUtil.getBean(ProductImageService.class);
        List<ProductImage> singleImages = productImageService.listSingleProductImages(product);
        if(!singleImages.isEmpty())
            product.setFirstProductImage(singleImages.get(0));
        else
            product.setFirstProductImage(new ProductImage()); //这样做是考虑到产品还没有来得及设置图片，但是在订单后台管理里查看订单项的对应产品图片。

    }

    //设置详情图片
    public void setFirstProdutImages(List<Product> products) {
        for (Product product : products)
            setFirstProdutImage(product);
    }

    //设置结算页订单图片
    public void setFirstProdutImagesOnOrderItems(List<OrderItem> ois) {
        for (OrderItem orderItem : ois) {
            setFirstProdutImage(orderItem.getProduct());
        }
    }

}
