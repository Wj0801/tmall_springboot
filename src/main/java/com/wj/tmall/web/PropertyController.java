package com.wj.tmall.web;


import com.wj.tmall.pojo.Property;
import com.wj.tmall.service.PropertyService;
import com.wj.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



//属性控制器类
@RestController
public class PropertyController {
    @Autowired
    PropertyService propertyService;

    /*查询功能
    categories/cid/properties 地址对应 PropertyController的list方法，在这个方法里通过 propertyService 获取分页数据，拿到数据之后，通过 vue.js 的 v-for 遍历到 table中
     */
    @GetMapping("/categories/{cid}/properties")
    public Page4Navigator<Property> list(@PathVariable("cid") int cid, @RequestParam(value = "start", defaultValue = "0") int start,@RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        start = start<0?0:start;
        Page4Navigator<Property> page =propertyService.list(cid, start, size,5);
        return page;
    }


    /*增加功能
      add 方法里 把property数据通过 axios 提交到 properties路径, PropertyController 的 add 方法接受数据，并调用 PropertyService 插入数据库
     */
        @PostMapping("/properties")
        public Object add(@RequestBody Property property ) throws Exception {
            propertyService.add(property);
            return property;
        }

    /*编辑功能-
    通过 propertyService 获取数据， html 拿到数据后，通过Vue 显示出来
     */
        @GetMapping("/properties/{id}")
        public Property get(@PathVariable("id")int id) throws  Exception{
            Property property=propertyService.get(id);
            return  property;
        }
    /*修改功能
    update 接受到数据后，通过 propertyService 把数据更新到数据库
     */
    @PutMapping("/properties")
    public  Object update(@RequestBody Property property) throws Exception{
        propertyService.update(property);
        return  property;
    }

    /*删除功能
    调用propertiService 进行数据删除
     */
    @DeleteMapping("/properties/{id}")
    public  String delete(@PathVariable("id") int id)throws  Exception{
        propertyService.delete(id);
        return null;
    }

}
