package com.wj.tmall.service;

import com.wj.tmall.dao.PropertyValueDAO;
import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.Property;
import com.wj.tmall.pojo.PropertyValue;
import com.wj.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
@Service
@CacheConfig(cacheNames="propertyValues")
public class PropertyValueService {
    @Autowired
     PropertyValueDAO propertyValueDAO;
    @Autowired
    PropertyService propertyService;

    //修改
    @CacheEvict(allEntries=true)
    public void update(PropertyValue propertyValue){
        propertyValueDAO.save(propertyValue);
    }

    //查询
    @Cacheable(key="'propertyValues-pid-'+ #p0.id")
    public List<PropertyValue> list(Product product){
        return propertyValueDAO.findByProductOrderByIdDesc(product);
    }

    /*初始化属性
    PropertyValue的管理，没有增加，只有修改。 所以需要通过初始化来进行自动地增加，以便于后面的修改。
     然后用属性id和产品id去查询，看看这个属性和这个产品，是否已经存在属性值了
    如果不存在，那么就创建一个属性值，并设置其属性和产品，接着插入到数据库中。
这样就完成了属性值的初始化*/
    public void init(Product product){
        //首先根据产品获取分类，然后获取这个分类下的所有属性集合
        //缓存管理 的方法，不能够直接调用，需要通过一个工具，再拿一次 Service， 然后再调用
        PropertyValueService propertyValueService = SpringContextUtil.getBean(PropertyValueService.class);
        List<Property> propertys= propertyService.listByCategory(product.getCategory());
        for (Property property: propertys) {
            PropertyValue propertyValue = propertyValueService.getByPropertyAndProduct(product, property);
            if(null==propertyValue){
                propertyValue = new PropertyValue();
                propertyValue.setProduct(product);
                propertyValue.setProperty(property);
                propertyValueDAO.save(propertyValue);
            }
        }
    }

    @Cacheable(key="'propertyValues-one-pid-'+#p0.id+ '-ptid-' + #p1.id")
    public PropertyValue getByPropertyAndProduct(Product product, Property property) {
        return propertyValueDAO.getByPropertyAndProduct(property,product);
    }

}
