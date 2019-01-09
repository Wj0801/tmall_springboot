package com.wj.tmall.interceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.wj.tmall.pojo.User;

import org.apache.shiro.subject.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//拦截器。拦截需要登陆的才能够访问的
public class LoginInterceptor  implements HandlerInterceptor{

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,Object o)throws Exception{
        //返回与这个请求关联的当前的有效的session,如果带入的参数为假，而且没有session与这个请求关联。这个方法会返回空值
        HttpSession session=httpServletRequest.getSession();
        //获取项目的根路径
        String contextPath=session.getServletContext().getContextPath();
        //准备字符串数组 requireAuthPages，存放需要登录才能访问的路径
        String[] requireAuthPages= new String[]{
                "buy",
                "alipay",
                "payed",
                "cart",
                "bought",
                "confirmPay",
                "orderConfirmed",

                "forebuyone",
                "forebuy",
                "foreaddCart",
                "forecart",
                "forechangeOrderItem",
                "foredeleteOrderItem",
                "forecreateOrder",
                "forepayed",
                "forebought",
                "foreconfirmPay",
                "foreorderConfirmed",
                "foredeleteOrder",
                "forereview",
                "foredoreview"

        };
        // 获取uri,去掉前缀/tmall_springboot
        String uri=httpServletRequest.getRequestURI();
        uri= StringUtils.remove(uri,contextPath+"/");
        String page=uri;

        //判断是否是以 requireAuthPages 里的开头的
        if(begingWith(page,requireAuthPages)){
            // Shiro 方式,如果是就判断是否登陆，未登陆就跳转到 login 页面
            Subject subject= SecurityUtils.getSubject();
            if(!subject.isAuthenticated()){
                httpServletResponse.sendRedirect("login");
                return false;
            }
        }
        /* 判断是否是以 requireAuthPages 里的开头的
        if(begingWith(page, requireAuthPages)){
            //如果是就判断是否登陆，未登陆就跳转到 login 页面
            User user = (User) session.getAttribute("user");
            if(user==null) {
                httpServletResponse.sendRedirect("login");
                return false;
            }
        }
        */
        // 如果不是就放行
        return true;
    }

    private boolean begingWith(String page, String[] requiredAuthPages) {
        boolean result = false;
        for (String requiredAuthPage : requiredAuthPages) {
            if(StringUtils.startsWith(page, requiredAuthPage)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}