package com.wj.tmall.service;

import com.wj.tmall.dao.UserDAO;
import com.wj.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.wj.tmall.pojo.User;import org.springframework.cache.annotation.Cacheable;

@Service
@CacheConfig(cacheNames="users")
public class UserService {
        @Autowired
          UserDAO userDAO;
        //增加
        @CacheEvict(allEntries=true)
        public void add(User user){
            userDAO.save(user);
        }
        //编辑
        public User get(int id){
            return userDAO.findOne(id);
        }
        //修改
         public void update(User user) {
            userDAO.save(user);
        }
        //删除
         public void delete(int id){
            userDAO.delete(id);
         }

         //分页查询
         @Cacheable(key="'users-page-'+#p0+ '-' + #p1")
         public Page4Navigator<User> list(int start,int size,int navigatePages){
             Sort sort=new Sort(Sort.Direction.DESC,"id");
             Pageable pageable=new PageRequest(start,size,sort);
             Page page=userDAO.findAll(pageable);
             return  new Page4Navigator<>(page,navigatePages);
        }
        //判断莫个名称是否已经使用过
        public boolean isExist(String name) {
            User user = getByName(name);
            return null!=user;
        }
        //查询全部名称
        @Cacheable(key="'users-one-name-'+ #p")
        public User getByName(String name) {
            return userDAO.findByName(name);
        }

        //登陆获取账号密码
        @Cacheable(key="'users-one-name-'+ #p0 +'-password-'+ #p1")
        public User get(String name, String password) {
            return userDAO.getByNameAndPassword(name,password);
        }
}
