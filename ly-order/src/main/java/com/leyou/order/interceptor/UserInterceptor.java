package com.leyou.order.interceptor;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j

public class UserInterceptor implements HandlerInterceptor {
    private JwtProperties properties;

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取cookie
        String token = CookieUtils.getCookieValue(request, properties.getCookieName());
        try {
            UserInfo userInfo = JwtUtils.getUserInfo(properties.getPublicKey(), token);
            if (userInfo == null) {
                return false;
            }
            tl.set(userInfo);
            return true;
        } catch (Exception e) {
            log.error("【购物车身份认证失败】", e);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    public static UserInfo getUserInfo(){
        return tl.get();
    }
}
