package com.wj.tmall.es;

import com.wj.tmall.pojo.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

//用于链接 ElasticSearch 的DAO。
public interface ProductESDAO extends ElasticsearchRepository<Product,Integer> {

}