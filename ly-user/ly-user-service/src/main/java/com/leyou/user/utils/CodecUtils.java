package com.leyou.user.utils;
 
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

/**
 * @Author: 98050
 * @Time: 2018-10-23 10:49
 * @Feature: 密码加密
 */
public class CodecUtils {
 
    public static String passwordBcryptEncode(String username,String password){
 
        return new BCryptPasswordEncoder().encode(username + password);
    }
 
    public static Boolean passwordBcryptDecode(String rawPassword,String encodePassword){
        return new BCryptPasswordEncoder().matches(rawPassword,encodePassword);
    }

    public static String md5Hex(String date, String salt) {
        if(StringUtils.isBlank(salt)){
            salt = date.hashCode()+"";
        }
        return DigestUtils.md5Hex(salt + DigestUtils.md5Hex(date));
    }
    public static String shaHex(String date, String salt) {
        if(StringUtils.isBlank(salt)){
            salt = date.hashCode()+"";
        }
        return DigestUtils.sha512Hex(salt + DigestUtils.sha512Hex(date));
    }

    public static String generateSalt(){
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "");
    }
}