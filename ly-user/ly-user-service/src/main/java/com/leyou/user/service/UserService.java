package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper mapper;

    @Autowired
    private AmqpTemplate template;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String KEY_PREFIX = "user:verify:";


    /**
     * 校验
     *
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {
        // 判断类型
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }

        int count = mapper.selectCount(user);
        return count == 0;
    }

    /**
     * 发送短信验证码
     *
     * @param phone
     */
    public void sendCode(String phone) {
        Map<String, String> msg = new HashMap<>();
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        msg.put("phone", phone);
        msg.put("code", code);
        template.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);
        // 保存验证码
        String key = KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
    }


    public void register(User user, String code) {
        String key = KEY_PREFIX + user.getPhone();
        String res = redisTemplate.opsForValue().get(key);
        // 检验验证码
        if (!StringUtils.equals(code, res)) {
            throw  new LyException(ExceptionEnum.VERIFY_CODE_ERROR);
        }
        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // 加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        // 新增用户
        user.setId(null);
        user.setCreated(new Date());
        mapper.insertSelective(user);
        redisTemplate.delete(key);
    }

    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = mapper.selectOne(record);
        if (user == null) {
            return null;
        }
        password = CodecUtils.md5Hex(password, user.getSalt());
        if (StringUtils.equals(password, user.getPassword())) {
            return user;
        }else{
            return null;
        }
    }
}
