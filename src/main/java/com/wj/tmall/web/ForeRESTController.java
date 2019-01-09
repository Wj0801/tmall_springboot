package com.wj.tmall.web;


import org.apache.commons.lang.math.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.wj.tmall.Comparator.*;
import com.wj.tmall.pojo.*;
import com.wj.tmall.service.*;
import com.wj.tmall.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.apache.shiro.subject.Subject;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

//对应前台页面的路径
@RestController
public class ForeRESTController {

    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;

    //查询所有分类
    @GetMapping("/forehome")
    public Object home() {
        List<Category> cs = categoryService.list();
        productService.fill(cs);
        productService.fillByRow(cs);
        categoryService.removeCategoryFromProduct(cs);
        return cs;
    }

    //通过参数User获取浏览器提交的账号密码
    @PostMapping("/foreregister")
    public Object register(@RequestBody User user) {
        String name = user.getName();
        String password = user.getPassword();
        //把账号里的特殊符号进行转义
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        boolean exist = userService.isExist(name);
        if (exist) {
            String message = "用户名已经被注册了哦~请重新注册";
            return Result.fail(message);
        }
        //注册时候，通过随机方式创建盐salt， 并且加密算法采用 "md5", 除此之外还会进行 2次加密
        String salt=new SecureRandomNumberGenerator().nextBytes().toString();
        int    times = 2 ;
        String algorithmName="md5";
        String encodedPassword=new SimpleHash(algorithmName,password,salt,times).toString();
        //数据库保存盐salt，和加密后的密码
        user.setSalt(salt);
        user.setPassword(encodedPassword);
        //user.setPassword(password);

        userService.add(user);
        return Result.success();
    }

    //登陆
    @PostMapping("/forelogin")
    public Object login(@RequestBody User userParam, HttpSession session) {
        String name = userParam.getName();
        name = HtmlUtils.htmlEscape(name);

        //通过shiro方式进行效验
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token=new UsernamePasswordToken(name,userParam.getPassword());
        try{
            subject.login(token);
            User user=userService.getByName(name);
            session.setAttribute("user",user);
            return Result.success();
        }catch (AuthenticationException e){
            String message="账号或者密码错误哦~看好在输入哦~";
            return Result.fail(message);
        }

        /*
        User user=userService.get(name, userParam.getPassword());
        if (null == user) {
            String message = "账号或者密码有误，请重新输入哦~";
            return Result.fail(message);
        } else {
            session.setAttribute("user", user);
            return Result.success();
        }
        */
    }

    //设置产品的销量和评价数量
    @GetMapping("/foreproduct/{pid}")
    public Object product(@PathVariable("pid") int pid) {
        //根据pid获取Product 对象product
        Product product = productService.get(pid);
        // 根据对象product，获取这个产品对应的单个和详情图片集合并保存到product
        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setproductDetailImages(productDetailImages);
        //获取并设置产品的所有属性值,对应产品的所有评价,和一张图片
        List<PropertyValue> pvs = propertyValueService.list(product);
        List<Review> reviews = reviewService.list(product);
        productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProdutImage(product);
        //把上述取值放在 map 中
        Map<String,Object> map=new HashMap<>();
        map.put("product",product);
        map.put("pvs",pvs);
        map.put("reviews",reviews);
        //通过 Result 把这个 map 返回到浏览器去
        return  Result.success(map);
    }

    //模态登陆,产品页面点击立即购买或者加入购物车的时候，需要判断是否登录，,改为Shiro 方式
    @GetMapping("forecheckLogin")
    public Object checkLogin(){
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated())
            return Result.success();
        else
            return Result.fail("亲你还没有登陆呢~先去登陆吧~~");
    }
    /*
    public Object checkLogin(HttpSession session){
         User user=(User) session.getAttribute("user");
         if(null!=user)
             return  Result.success();
        return  Result.fail("亲你还没有登陆呢~先去登陆吧~~");
    }

    */

    //从5个Comparator比较器中选择一个对应的排序器进行排序进行产品分类
    @GetMapping("forecategory/{cid}")
    public Object category(@PathVariable int cid,String sort){
       Category c= categoryService.get(cid);
       //填充产品，销量和评价数据
        productService.fill(c);
        productService.setSaleAndReviewNumber(c.getProducts());
        //删除product产品的分类
        categoryService.removeCategoryFromProduct(c);
        //获取参数sort，进行比较
        if(null!=sort){
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }

        return c;
    }

        //搜索的模糊查询
    @PostMapping("foresearch")
    public  Object search(String keyword){
        if(null==keyword)
            keyword="";
        List<Product> ps= productService.search(keyword,0,20);
        productImageService.setFirstProdutImages(ps);
        productService.setSaleAndReviewNumber(ps);
        return ps;
        }

    /*点击立即购买跳转 /forebuyone，然后调用 buyoneAndAddCart 方法。
    a. 如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量
    a.1 基于用户对象user，查询没有生成订单的订单项集合
    a.2 遍历这个集合
    a.3 如果产品是一样的话，就进行数量追加
    a.4 获取这个订单项的 id

    b. 如果不存在对应的OrderItem,那么就新增一个订单项OrderItem
    b.1 生成新的订单项
    b.2 设置数量，用户和产品
    b.3 插入到数据库
    b.4 获取这个订单项的 id
     */
    @GetMapping("/forebuyone")
    public Object buyone(int pid,int num, HttpSession session){
        return buyoneAndAddCart(pid,num,session);
    }
    private int buyoneAndAddCart(int pid,int num,HttpSession session){
          Product product=  productService.get(pid);
          int oiid =0;
        //获取用户对象user
         User user= (User) session.getAttribute("user");
         boolean found = false;
         //ois=订单项集合
         List<OrderItem> ois=orderItemService.listByUser(user);
        //遍历集合
         for(OrderItem oi:ois){
             //如果产品一样
             if(oi.getProduct().getId()==product.getId()){
                 oi.setNumber(oi.getNumber()+num);
                 orderItemService.update(oi);
                 found = true;
                 oiid = oi.getId();
                 break;
             }
         }
         //不为真，b：不存在对应的OrderItem,那么就新增一个订单项OrderItem
        if(!found){
            OrderItem oi=new OrderItem();
            oi.setUser(user);
            oi.setProduct(product);
            oi.setNumber(num);
            orderItemService.add(oi);
            oiid = oi.getId();
        }
        //oiid为当前订单项的id
        return  oiid;
    }

    /*购买结算
    为了兼容从购物车页面跳转过来的需求，要用字符串数组获取多个oiid
     */
    @GetMapping("/forebuy")
    public  Object buy(String[] oiid,HttpSession session){
        List<OrderItem> orderItems=new ArrayList<>();
        float total =0;

        for(String strid:oiid){
            int id=Integer.parseInt(strid);
            //从数据库中取出OrderItem对象，并放入ois集合中
            OrderItem oi=orderItemService.get(id);
            //累计这些ois的价格总数，赋值在total上
            total +=oi.getProduct().getPromotePrice()*oi.getNumber();
            orderItems.add(oi);
        }
        productImageService.setFirstProdutImagesOnOrderItems(orderItems);
        //把订单项集合放在session的属性 "ois" 上
        session.setAttribute("ois", orderItems);
        //. 把订单集合和total 放在map里
        Map<String,Object> map = new HashMap<>();
        map.put("orderItems", orderItems);
        map.put("total", total);

        return Result.success(map);
    }

    //加入跳转购物车
    @GetMapping("/foreaddCart")
    public  Object addCart(int pid,int num,HttpSession session){
        buyoneAndAddCart(pid,num,session);
        return  Result.success();
    }

    //查看购物车
    @GetMapping("/forecart")
    public Object cart(HttpSession session){
       User user=(User) session.getAttribute("user");
       List<OrderItem> ois= orderItemService.listByUser(user);
       productImageService.setFirstProdutImagesOnOrderItems(ois);
       return ois;
    }

    //购物车调整订单数量
    @GetMapping("forechangeOrderItem")
    public Object forechangeOrderItem(HttpSession session,int pid,int num) {
        User user = (User) session.getAttribute("user");
        if (null == user) {
            return Result.fail("亲，你还没登陆哦~先去登陆吧~");
        }
        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if (oi.getProduct().getId() == pid) {
                oi.setNumber(num);
                orderItemService.update(oi);
                break;
            }
        }
        return Result.success();
    }

    //购物车删除订单项
    @GetMapping("foredeleteOrderItem")
    public  Object delete( HttpSession  session,int oiid){
        User user=(User) session.getAttribute("user");
            if(null==user)
                return Result.fail("亲，你还没登陆哦~先去登陆吧~");
            orderItemService.delete(oiid);
        return Result.success();
    }


    //结算订单
    @PostMapping("forecreateOrder")
    public Object createOrder(@RequestBody Order order,HttpSession session){
       User user=(User)session.getAttribute("user");
            if(null==user)
                     return Result.fail("亲，你还没登陆哦~先去登陆吧~");
            //根据当前时间加上一个4位随机数生成订单号
            String orderCode=new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
           //根据上述参数，创建订单对象，把订单状态设置为等待支付
            order.setOrderCode(orderCode);
            order.setCreateDate(new Date());
            order.setUser(user);
            order.setStatus(OrderService.waitPay);
            // 从session中获取订单项集合
            List<OrderItem> ois=(List<OrderItem>) session.getAttribute("ois");
            //把订单加入到数据库，并且遍历订单项集合，设置每个订单项的order，更新到数据库
            float total=orderService.add(order,ois);
            //统计本次订单的总金额
            Map<String,Object> map = new HashMap<>();
            map.put("oid", order.getId());
            map.put("total", total);
            //返回总金额
            return Result.success(map);
    }


    //支付成功页
    @GetMapping("forepayed")
    public Object payed(int oid){
        //根据获取的oid获取order
        Order order=orderService.get(oid);
        //设置订单状态，和时间
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        //修改订单
        orderService.update(order);
        return order;
    }


    //我的订单页，查询订单集合
    @GetMapping("/forebought")
    public  Object bought(HttpSession session){
       User user=(User) session.getAttribute("user");
        if(null==user)
            return Result.fail("亲，你还没有登陆哦。快去登陆吧~");
        //查询user所有的状态不是"delete" 的订单集合os
        List<Order> os=orderService.listByUserWithoutDelete(user);
        // 为这些订单填充订单项
        orderService.removeOrderFromOrderItem(os);
        return os;
    }

    //我的订单页 确认收获
    @GetMapping("foreconfirmPay")
    public  Object confirmPay(int oid){
        Order o=orderService.get(oid);
        // 为订单对象填充订单项
        orderItemService.fill(o);
        //把订单项上的订单对象移除，否则会导致重复递归
        orderService.removeOrderFromOrderItem(o);
        return  o;
    }

    //我的订单页，确认收获成功
    @GetMapping("foreorderConfirmed")
    public Object orderConfirmed(int oid){
      Order o=  orderService.get(oid);
      //修改对象o的状态为等待评价，修改其确认支付时间
      o.setStatus(OrderService.waitReview);
      o.setConfirmDate(new Date());
      //更新到数据库
      orderService.update(o);
      return Result.success();
    }

    //我的订单页，删除
    @PutMapping("foredeleteOrder")
    public Object deleteOrder(int oid){
        Order o=  orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return Result.success();
    }



    //我的订单，评价产品页面
    @GetMapping("forereview")
    public  Object review(int oid){
       Order o =orderService.get(oid);
       //为订单对象填充订单项
       orderItemService.fill(o);
       orderService.removeOrderFromOrderItem(o);
       // 获取第一个订单项对应的产品,因为在评价页面需要显示一个产品图片
       Product p=o.getOrderItems().get(0).getProduct();
       //获取这个产品的评价集合
       List<Review> reviews=reviewService.list(p);
       // 为产品设置评价数量和销量
       productService.setSaleAndReviewNumber(p);
       //把产品，订单和评价集合放在map上
        Map<String,Object> map=new HashMap<>();
        map.put("p",p);
        map.put("o",o);
        map.put("reviews",reviews);
        return  Result.success(map);
    }

    //我的订单。提交评价
    @PostMapping("foredoreview")
    public  Object doreview(HttpSession session,int oid,int pid,String content){
        Order o=orderService.get(oid);
        //设置订单对象状态
        o.setStatus(OrderService.finish);
        //更新订单对象到数据库
        orderService.update(o);
        Product p=productService.get(pid);
        //获取参数content (评价信息)，对评价信息进行转义（把评价信息里的特殊符号进行转义）
        content=HtmlUtils.htmlEscape(content);
        User user=(User) session.getAttribute("user");
        //创建评价对象
        Review review=new Review();
        //为评价对象review设置 评价信息，产品，时间，用户
        review.setContent(content);
        review.setProduct(p);
        review.setCreateDate(new Date());
        review.setUser(user);
        //增加到数据库
        reviewService.add(review);
        return Result.success();
    }


}
