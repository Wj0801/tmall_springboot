package com.wj.tmall.service;

import java.util.List;

import com.wj.tmall.pojo.Product;
import com.wj.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.wj.tmall.dao.CategoryDAO;
import com.wj.tmall.pojo.Category;

//标记这个类是 Service类
@Service
//表示分类在缓存里的keys，都是归 "categories" 这个管理的
@CacheConfig(cacheNames = "categories")
public class CategoryService {
	@Autowired CategoryDAO categoryDAO;

	//分页查询
	//获取一个集合， categories-page-0-5 就是第一页数据
	@Cacheable(key="'categories-page-'+#p0+ '-' + #p1")
	public Page4Navigator<Category> list(int start, int size, int navigatePages){
		Sort sort=new Sort(Sort.Direction.DESC,"id");
		//JPA分页对象
		Pageable pageable=new PageRequest(start,size,sort);
		Page pageFromJPA=categoryDAO.findAll(pageable);
		return  new Page4Navigator<>(pageFromJPA,navigatePages);
	}

	@Cacheable(key="'categories-all'")
	public List<Category> list() {
		//表示通过 id 倒排序， 然后通过 categoryDAO进行查询。
		Sort sort = new Sort(Sort.Direction.DESC, "id");
		return categoryDAO.findAll(sort);
	}

	/*增加种类
	删除 categories~keys 里的所有的keys.
	以 category-one-id 的方式增加到 Redis中去
	它并不能更新分页缓存 categories-page-0-5 里的数据，
	这样就会出现数据不一致的问题，所以一旦增加了某个分类数据，
	那么就把缓存里所有分类相关的数据，都清除掉，下一次再访问的时候，
	缓存里没数据，那么就会从数据库中读出来，读出来之后，再放在缓存里
	@CachePut(key="'category-one-'+ #p0")
	*/
	@CacheEvict(allEntries=true)
	public void add(Category category){
		categoryDAO.save(category);
	}

	//删除种类
	@CacheEvict(allEntries=true)
	//  @CacheEvict(key="'category-one-'+ #p0")
	public void delete(int id){
		categoryDAO.delete(id);
	}

	//编辑种类
	// 获取一个
	@Cacheable(key="'categories-one-'+ #p0")
	public Category get(int id){
		Category c=categoryDAO.findOne(id);
		return c;
	}

	//修改种类
	@CacheEvict(allEntries=true)
	//  @CachePut(key="'category-one-'+ #p0")
	public void update(Category category){
		categoryDAO.save(category);
	}



	//删除Product对象上的 分类
	public void removeCategoryFromProduct(List<Category> cs) {
		for (Category category : cs) {
			removeCategoryFromProduct(category);
		}
	}
	public void removeCategoryFromProduct(Category category) {
		List<Product> products = category.getProducts();
		if (null != products) {
			for (Product product : products) {
				product.setCategory(null);
			}
		}
		List<List<Product>> productsByRow =category.getProductsByRow();
			if(null!=productsByRow) {
			for (List<Product> ps : productsByRow) {
				for (Product p: ps) {
					p.setCategory(null);
				}
			}
		}
	}




}
