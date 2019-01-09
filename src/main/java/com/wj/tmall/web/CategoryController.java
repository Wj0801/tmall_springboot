package com.wj.tmall.web;

import com.wj.tmall.pojo.Category;
import com.wj.tmall.service.CategoryService;
import com.wj.tmall.util.ImageUtil;
import com.wj.tmall.util.Page4Navigator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

 //表示这是一个控制器，并且对每个方法的返回值都会直接转换为 json 数据格式。
@RestController
public class CategoryController {
	@Autowired
    CategoryService categoryService;

	/*
1. 首先浏览器上访问路径 /admin
2. 这个路径被 AdminPageController 的admin方法匹配，然后客户端跳转到 admin_category_list
3. admin_category_list 被 AdminPageController 的 listCategory 方法匹配，服务端跳转到 admin/listCategory.html
4. listCategory.html 这个html页面通过http协议传输到浏览器端
5. 浏览器根据html 上的js代码，异步调用 categories 这个地址。 CategoryController 获取捕捉到这个请求，到数据库里查出所有的分类数据，并转换为 json数组返回给浏览器。
6. 浏览器根据这个json数组，通过 vue 的v-for 方式把其遍历到 多个 tr 元素上，用户就看到了表格里的多条数据了。
    //查询种类，未分页
	@GetMapping("/categories")
    public List<Category> list() throws Exception {
    	return categoryService.list();
    }
*/


	//分页查询种类
    @GetMapping("/categories")
     public Page4Navigator<Category> list(@RequestParam(value = "start",defaultValue = "0") int start,
             @RequestParam(value = "size",defaultValue = "5")int size) throws  Exception{
                start =start<0?0:start;
                //5表示导航分页最多有5个，像 [1,2,3,4,5] 这样
                Page4Navigator<Category> page=categoryService.list(start,size,5);
                return page;
    }


    // GetMapping一个是 PostMapping，REST 规范就是通过method的区别来辨别到底是获取还是增加的。
    //增加种类
     @PostMapping("categories")
     public Object add(Category category, MultipartFile image, HttpServletRequest request) throws  Exception{
           categoryService.add(category);
           saveOrUpdateImageFile(category,image,request);
            return category;
     }
      //抽出来的上传图片方法方便后续调用
     public void saveOrUpdateImageFile(Category category,MultipartFile image,HttpServletRequest request) throws  Exception{
        //接受上传图片，并保存到 img/category目录下
        File imageFolder=new File(request.getServletContext().getRealPath("img/category"));
        //文件名使用新增分类的id
        File file= new File(imageFolder,category.getId()+".jpg");
        //如果目录不存在，需要创建
         if(!file.getParentFile().exists())file.getParentFile().mkdirs();
        // image.transferTo 进行文件复制
        image.transferTo(file);
        //调用ImageUtil的change2jpg 进行文件类型强制转换为 jpg格式
         BufferedImage img=ImageUtil.change2jpg(file);
         //保存图片
         ImageIO.write(img,"jpg",file);
     }


     //删除种类
     @DeleteMapping("/categories/{id}")
     public String delete(@PathVariable("id")int id,HttpServletRequest request)throws  Exception{
         categoryService.delete(id);
         //删除对应的文件
         File imageFolder=   new File(request.getServletContext().getRealPath("img/category"));
         File file  =    new File(imageFolder,id+".jpg");
         file.delete();
         //返回 null, 会被RESTController 转换为空字符串。
            return null;
     }


     //编辑种类
     @GetMapping("categories/{id}")
     public  Category edit(@PathVariable("id")int id)throws  Exception{
          Category category= categoryService.get(id);
        return category;
     }
     //修改种类   REST要求修改用PUT
     @PutMapping("categories/{id}")
     public void update(Category category,MultipartFile image,HttpServletRequest request)throws  Exception{
       //put映射注入不了category对象。只能注入id，所以要手动获取name，要不然不会显示分类名称
        String name =  request.getParameter("name");
        category.setName(name);
        categoryService.update(category);

        if(image!=null){
            //如果编辑的图片不为空，调用上面的上传图片方法重新上传
            saveOrUpdateImageFile(category,image,request);
        }

    }

}
