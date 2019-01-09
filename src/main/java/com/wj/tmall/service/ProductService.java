package com.wj.tmall.service;

import com.wj.tmall.dao.ProductDAO;
import com.wj.tmall.es.ProductESDAO;
import com.wj.tmall.pojo.Category;
import com.wj.tmall.pojo.Product;
import com.wj.tmall.util.Page4Navigator;
import com.wj.tmall.util.SpringContextUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;



@Service
@CacheConfig(cacheNames="products")
public class ProductService  {

    @Autowired
    ProductDAO productDAO;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    ProductESDAO productESDAO;

    @CacheEvict(allEntries=true)
    public void add(Product product) {
        productDAO.save(product);
        productESDAO.save(product);
    }

    @CacheEvict(allEntries=true)
    public void delete(int id) {
        productDAO.delete(id);
        productESDAO.delete(id);
    }

    //编辑
    @Cacheable(key="'products-one-'+ #p0")
    public Product get(int id) {
        return productDAO.findOne(id);
    }

    //修改
    @CacheEvict(allEntries=true)
    public void update(Product product) {
        productDAO.save(product);
        productESDAO.save(product);
    }

    /*分页查询 方法（需要的参数）
    1.获取是那个分类下的（cid）
    2.排序
    3.求pageable
    4.调用dao中findBycategory查询方法
    5.调用分页类的Page4Navigator方法带navigatepage参数。
    6.返回到页面
     */
    //带上cid,表示是某个分类下的产品
    @Cacheable(key="'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public Page4Navigator<Product> list(int cid, int start, int size,int navigatePages) {
        Category category = categoryService.get(cid);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        Page<Product> pageFromJPA =productDAO.findByCategory(category,pageable);
        return new Page4Navigator<>(pageFromJPA,navigatePages);
    }

    //查询某个分类下的所有产品
    @Cacheable(key="'products-cid-'+ #p0.id")
    public List<Product> listByCategory(Category category){
        return productDAO.findByCategoryOrderById(category);
    }


    //为分类填充产品集合
    //在ProductService的一个方法里，调用另一个 缓存管理 的方法，不能够直接调用，
    // 需要通过一个工具，再拿一次 ProductService， 然后再调用。
    public void fill(Category category){
        ProductService productService = SpringContextUtil.getBean(ProductService.class);
        List<Product> products = productService.listByCategory(category);
        productImageService.setFirstProdutImages(products);
        category.setProducts(products);
        /*
        List<Product> products=listByCategory(category);
        productImageService.setFirstProdutImages(products);
        category.setProducts(products);
        */
    }
    //为多个分类填充产品集合
    public void fill(List<Category> categorys) {
        for (Category category : categorys) {
            fill(category);
        }
    }

    //为多个分类填充推荐产品集合，即把分类下的产品集合，按照8个为一行，拆成多行，以利于后续页面上进行显示
    public void fillByRow(List<Category> categorys) {
        int productNumberEachRow = 8;
        for (Category category : categorys) {
            List<Product> products =  category.getProducts();
            List<List<Product>> productsByRow =  new ArrayList<>();
            for (int i = 0; i < products.size(); i+=productNumberEachRow) {
                int size = i+productNumberEachRow;
                size= size>products.size()?products.size():size;
                List<Product> productsOfEachRow =products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }

    //为产品设置销量和评价数量
    public void setSaleAndReviewNumber(List<Product> products) {
        for (Product product : products)
            setSaleAndReviewNumber(product);
    }
    public  void setSaleAndReviewNumber(Product product){
        //获取销量数
        int saleCount=orderItemService.getSaleCount(product);
        //销量数量保存到product
        product.setSaleCount(saleCount);
        //获取评价
        int reviewCount= reviewService.getCount(product);
        //评价数量保存到product
        product.setReviewCount(reviewCount);

    }

    /*搜索框模糊查询
    public List<Product> search(String keyword, int start, int size) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        List<Product> products =productDAO.findByNameLike("%"+keyword+"%",pageable);
        return products;
    }
    */


    //通过 ProductESDAO 到 elasticsearch 中进行查询
    public List<Product> search(String keyword,int start,int size){
        //调用方法
        initDatabase2ES();
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
                .add(QueryBuilders.matchPhraseQuery("name", keyword),
                        ScoreFunctionBuilders.weightFactorFunction(100))
                .scoreMode("sum")
                .setMinScore(10);
        Sort sort  = new Sort(Sort.Direction.DESC,"id");
        Pageable pageable = new PageRequest(start, size,sort);
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(functionScoreQueryBuilder).build();
        Page<Product> page = productESDAO.search(searchQuery);
        return page.getContent();
    }
    //初始化数据到es，数据刚开始在数据库中，不在es中，所以刚开始查询，
    // 先看看es有没有数据，如果没有，就把数据从数据库同步到es中。
    private void initDatabase2ES(){
        Pageable pageable=new PageRequest(0,5);
        Page<Product> page=productESDAO.findAll(pageable);
        if(page.getContent().isEmpty()){
            List<Product> products=productDAO.findAll();
            for (Product product : products) {
                productESDAO.save(product);
            }
        }
    }

}
