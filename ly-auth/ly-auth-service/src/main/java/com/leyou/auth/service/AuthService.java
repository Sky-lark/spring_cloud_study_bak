package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserClient client;

    @Autowired
    private JwtProperties properties;

    public String accredit(String username, String password) {
        // 根据用户名和密码查询
        User user = client.query(username, password);
        if (user == null) {
            throw  new LyException(ExceptionEnum.USERNAME_OR_PASSWORD_INVALID);
        }
        // 生成token
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            String token = JwtUtils.generateToken(userInfo, properties.getPrivateKey(), properties.getExpire());
            return token;
        } catch (Exception e) {
            throw  new LyException(ExceptionEnum.CREATE_TOKEN_FAIL);
        }
    }
}
