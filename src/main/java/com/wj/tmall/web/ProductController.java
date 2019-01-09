package com.wj.tmall.web;

import com.wj.tmall.pojo.Product;
import com.wj.tmall.service.CategoryService;
import com.wj.tmall.service.ProductImageService;
import com.wj.tmall.service.ProductService;
import com.wj.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
public class ProductController {
             @Autowired
             ProductService productService;
              @Autowired
              CategoryService categoryService;
              @Autowired
              ProductImageService productImageService;
              /*分页查询
                获取开始位置，默认为0.
                调用service方法，
                返回
               */
             @GetMapping("/categories/{cid}/products")
            public Page4Navigator<Product> list(@PathVariable("cid") int cid, @RequestParam(value="start",defaultValue = "0") int start,@RequestParam(value = "size",defaultValue = "5")int size)throws  Exception{
                 start = start<0?0:start;
                Page4Navigator<Product> page4Navigator=productService.list(cid,start,size,5);
                //设置图片
                 productImageService.setFirstProdutImages(page4Navigator.getContent());
                 return  page4Navigator;
             }


             //编辑
             @GetMapping("/products/{id}")
            public Product get(@PathVariable("id") int id)throws Exception{
                 Product product=productService.get(id);
                 return product;
             }
             //修改
            @PutMapping("/products")
            public  Object update(@RequestBody Product product)throws  Exception{
                productService.update(product);
                return  product;
            }

            /*增加
              获取创建的时间
              调用service方法添加
             */
            @PostMapping("/products")
            public Object add(@RequestBody Product product)throws Exception{
                    product.setCreateDate(new Date());
                    productService.add(product);
                    return  product;

            }
            //删除，返回 null, 会被RESTController 转换为空字符串。
            @DeleteMapping("/products/{id}")
            public String delete(@PathVariable("id")int id)throws  Exception{
                    productService.delete(id);
                    return  null;
            }


}
