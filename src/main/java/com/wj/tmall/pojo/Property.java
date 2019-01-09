package com.wj.tmall.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

//属性实体类
@Entity
@Table(name="property")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自增长方式
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @ManyToOne                  //多对一，表中多个属性对应一个种类id
    @JoinColumn(name = "cid")     //另一个表指向本表的外键

    private Category category;

    @Override
    public String toString() {
        return "Property [id=" + id + ", name=" + name + ", category=" + category + "]";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}