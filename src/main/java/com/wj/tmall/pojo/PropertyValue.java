package com.wj.tmall.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "propertyvalue")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class PropertyValue {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name="id")
        private  int id;

        //对应product的pid
        @ManyToOne
        @JoinColumn(name="pid")
        private Product product;
         //对应property的pid
        @ManyToOne
        @JoinColumn(name="ptid")
        private Property property;

        private String value;

        //重构
        @Override
        public  String toString(){
            return   "PropertyValue [id=" + id + ", product=" + product + ", property=" + property + ", value=" + value + "]";
        }


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public Property getProperty() {
            return property;
        }

        public void setProperty(Property property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
}
