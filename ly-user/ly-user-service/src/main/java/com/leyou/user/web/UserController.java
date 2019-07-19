package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService service;


    /**
     * 校验数据
     * @param data 校验数据
     * @param type 1，用户名；2，手机
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> check(
            @PathVariable("data") String data, @PathVariable("type") Integer type) {
        return ResponseEntity.ok(service.checkData(data, type));
    }
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone")String phone){
            service.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("register")
    public ResponseEntity<Void> register(@RequestParam("code")String code, @Valid User user){
        service.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("query")
    public ResponseEntity<User> query(@RequestParam("username")String username,@RequestParam("password")String password){
        User user = service.queryUser(username, password);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }
}
