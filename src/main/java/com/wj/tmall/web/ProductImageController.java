package com.wj.tmall.web;

import com.wj.tmall.pojo.Product;
import com.wj.tmall.pojo.ProductImage;
import com.wj.tmall.service.CategoryService;
import com.wj.tmall.service.ProductImageService;
import com.wj.tmall.service.ProductService;
import com.wj.tmall.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

@RestController
public class ProductImageController {
    @Autowired
    ProductImageService productImageService;
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;


    //查询图片
    @GetMapping("/products/{pid}/productImages")
    public List<ProductImage> list(@PathVariable("pid")int pid,@RequestParam("type")String type)throws Exception{
     //根据id获取product对象
       Product product=productService.get(pid);
       //如果是单个产品图片，调用productImageService的listSingleProductImages方法查询单个图片
       if(ProductImageService.type_single.equals(type)){
            List<ProductImage> singles=productImageService.listSingleProductImages(product);
            return  singles;
       }else  if(ProductImageService.type_detail.equals(type)){
          List<ProductImage> details= productImageService.listDetailProductImages(product);
            return  details;
       }else {
           return  new ArrayList<>();
       }
    }




    //增加-上传图片
    @PostMapping("/productImages")
    public Object add(@RequestParam("pid")int pid, @RequestParam("type") String type, MultipartFile image, HttpServletRequest request, ProductImage productImage)throws  Exception{
        //上面（）定义了 ProductImage productImage = new ProductImage();
        //上传到路径 productImages，并带上type和pid参数
       Product product= productService.get(pid);
       productImage.setProduct(product);
       productImage.setType(type);
       // 创建 productImage 对象，并插入数据库
       productImageService.add(productImage);
        //根据类型指定保存文件的路径：productSingle
       String folder="img/";
       //判断是单个还是详情图片
       if(ProductImageService.type_single.equals(productImage.getType()))
           folder +="productSingle";
       else
           folder +="productDetail";
        //根据类型指定保存文件的路径：productSingle
       File imageFolder=new File(request.getServletContext().getRealPath(folder));
        //接着根据产品图片对象的id，作为文件名，把图片保存到对应的位置
       File file=new File(imageFolder,productImage.getId()+".jpg");
       String fileName=file.getName();
       //如果没有创建文件夹，有就不创建
       if(!file.getParentFile().exists())
           file.getParentFile().mkdirs();
            try {
                //通过工具类 ImageUtil.change2jpg进行类型强制转换,以确保一定是jpg图片
                image.transferTo(file);
                BufferedImage img = ImageUtil.change2jpg(file);
                ImageIO.write(img, "jpg", file);
            }catch (IOException e){
                e.printStackTrace();
            }
        //如果是单个图片，用 ImageUtil.resizeImage 函数创建 small 和 middle 两种不同大小的图片，
        if(ProductImageService.type_single.equals(productImage.getType())){
            String imageFolder_small= request.getServletContext().getRealPath("img/productSingle_small");
            String imageFolder_middle= request.getServletContext().getRealPath("img/productSingle_middle");
            File f_small = new File(imageFolder_small, fileName);
            File f_middle = new File(imageFolder_middle, fileName);
            f_small.getParentFile().mkdirs();
            f_middle.getParentFile().mkdirs();
            ImageUtil.resizeImage(file, 56, 56, f_small);
            ImageUtil.resizeImage(file, 217, 190, f_middle);
        }

        return productImage;

    }

    //删除图片
    @DeleteMapping("/productImages/{id}")
    public String delete(@PathVariable("id")int id,HttpServletRequest request)throws Exception{
       // 根据id获取ProductImage 对象,借助productImageService，删除数据
        ProductImage productImage= productImageService.get(id);
        productImageService.delete(id);
        //判断是单个还是详情
        String folder="/img";
        if(ProductImageService.type_single.equals(productImage.getType()))
            folder +="productSingle";
        else
            folder +="productDetail";
        //获取图片位置，删除
       File imgFolder= new File(request.getServletContext().getRealPath(folder));
       File file=new File(imgFolder,productImage.getId()+".jpg");
       String  fileName=file.getName();
       file.delete();
        //如果是单个图片，那么删除3张之前上传保存的正常，中等，小号图片
        //如果是详情图片，那么删除一张图片
        if(ProductImageService.type_single.equals(productImage.getType())){
            String imageFolder_small= request.getServletContext().getRealPath("img/productSingle_small");
            String imageFolder_middle= request.getServletContext().getRealPath("img/productSingle_middle");
            File f_small = new File(imageFolder_small, fileName);
            File f_middle = new File(imageFolder_middle, fileName);
            f_small.delete();
            f_middle.delete();
        }

        return  null;
    }



}
