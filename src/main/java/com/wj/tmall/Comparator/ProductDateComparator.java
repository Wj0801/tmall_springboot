package com.wj.tmall.Comparator;

import com.wj.tmall.pojo.Product;

import java.util.Comparator;

//新品比较器 把 创建日期晚的放前面
public class ProductDateComparator implements Comparator<Product> {

    @Override
    public int compare(Product p1, Product p2) {
        return p2.getCreateDate().compareTo(p1.getCreateDate());
    }

}