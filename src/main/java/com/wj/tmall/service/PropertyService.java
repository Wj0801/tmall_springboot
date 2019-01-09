package com.wj.tmall.service;

import com.wj.tmall.dao.PropertyDAO;
import com.wj.tmall.pojo.Category;
import com.wj.tmall.pojo.Property;
import com.wj.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
@Service
@CacheConfig(cacheNames="properties")
public class PropertyService {
    @Autowired
    PropertyDAO propertyDAO;
    @Autowired
    CategoryService categoryService;

    //增删改查单个属性
    @CacheEvict(allEntries=true)
    public void add(Property property){
        propertyDAO.save(property);
    }
    @CacheEvict(allEntries=true)
    public void delete(int id){
        propertyDAO.delete(id);
    }
    @CacheEvict(allEntries=true)
    public void  update(Property property){
        propertyDAO.save(property);
    }
    @Cacheable(key="'properties-one-'+ #p0")
    public Property get(int id){
        return   propertyDAO.findOne(id);
    }

    //分页查询全部属性
    @Cacheable(key="'properties-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public Page4Navigator<Property> list(int cid, int start, int size,int navigatePages) {
        Category category = categoryService.get(cid);

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);

        Page<Property> pageFromJPA =propertyDAO.findByCategory(category,pageable);

        return new Page4Navigator<>(pageFromJPA,navigatePages);

    }

    //通过分类获取所有属性集合的方法
    @Cacheable(key="'properties-cid-'+ #p0.id")
    public  List<Property> listByCategory(Category category){
        return propertyDAO.findByCategory(category);
    }



}
