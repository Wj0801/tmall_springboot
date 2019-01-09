package com.wj.tmall;
import com.wj.tmall.util.PortUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//启动类，@EnableCaching
@SpringBootApplication
//用于启动缓存
@EnableCaching
//为es和jpa指定不同的包名
//jpa 的dao 做了 链接 redis 的，如果放在同一个包下，会彼此影响，出现启动异常。
@EnableElasticsearchRepositories(basePackages = "com.wj.tmall.es")
@EnableJpaRepositories(basePackages = {"com.wj.tmall.dao", "com.wj.tmall.pojo"})
//打包，加@ServletComponentScan注解，继承SpringBootServletInitializer
@ServletComponentScan
public class Application extends SpringBootServletInitializer {
    static {
        // 检查端口是否启动,如果未启动，那么就会退出 springboot。
        PortUtil.checkPort(6379,"Redis 服务端",true);
        PortUtil.checkPort(9300,"ElasticSearch 服务端",true);
        PortUtil.checkPort(5601,"Kibana 工具", true);
    }
    //重写configure方法，打war包
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
    	SpringApplication.run(Application.class, args);    	
    }
}
