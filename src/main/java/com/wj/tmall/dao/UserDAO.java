package com.wj.tmall.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wj.tmall.pojo.User;

public interface UserDAO extends JpaRepository<User,Integer> {
    //查询全部名称，供判断使用
    User findByName(String name);
    //登陆获取账号密码
    User getByNameAndPassword(String name, String password);
}
