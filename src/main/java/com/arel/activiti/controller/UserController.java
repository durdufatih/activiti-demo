package com.arel.activiti.controller;

import com.arel.activiti.model.entity.UserEntity;
import com.arel.activiti.service.UserService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("signup")
    public ResponseEntity<?> save(@RequestBody UserEntity userEntity) {
        User user = new org.activiti.engine.impl.persistence.entity.UserEntity();
        user.setEmail(userEntity.getEmail());
        user.setFirstName(userEntity.getName());
        user.setLastName(userEntity.getSurname());
        user.setPassword(userEntity.getPassword());
        identityService.saveUser(user);
        userEntity.setPassword(bCryptPasswordEncoder.encode(userEntity.getPassword()));
        return ResponseEntity.ok(userService.save(userEntity));
    }
}
