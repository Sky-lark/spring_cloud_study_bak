package com.leyou.auth.web;


import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private JwtProperties properties;

    @PostMapping("login")
    public ResponseEntity<Void> accredit(@RequestParam("username") String username,
                                         @RequestParam("password") String password,
                                         HttpServletRequest request, HttpServletResponse response) {
        String token = service.accredit(username, password);
        if (StringUtils.isBlank(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CookieUtils.setCookie(request, response,
                properties.getCookieName(), token, properties.getExpire() * 60);

        return ResponseEntity.ok(null);
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletRequest request, HttpServletResponse response) {
        try {
            UserInfo userInfo = JwtUtils.getUserInfo(properties.getPublicKey(), token);
            // 刷新token
            String token1 = JwtUtils.generateToken(userInfo, properties.getPrivateKey(), properties.getExpire());
            CookieUtils.setCookie(request, response,
                    properties.getCookieName(), token1, properties.getExpire() * 60);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // 过期或者无效
            throw new LyException(ExceptionEnum.VERIFY_TOKEN_FAIL);
        }

    }
}

