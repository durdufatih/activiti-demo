package com.arel.activiti.service;

import com.arel.activiti.config.security.AccountCredentials;
import com.arel.activiti.model.entity.UserEntity;
import com.arel.activiti.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public boolean save(UserEntity userEntity) {
        UserEntity newUserEntity = userRepository.save(userEntity);
        if (Objects.nonNull(newUserEntity)) {
            return true;
        }
        return false;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username);
        AccountCredentials accountCredentials = new AccountCredentials();
        if (userEntity != null) {
            accountCredentials.setId(userEntity.getId());
            accountCredentials.setUsername(userEntity.getUsername());
            accountCredentials.setPassword(userEntity.getPassword());
            accountCredentials.setName(userEntity.getName());
            accountCredentials.setSurname(userEntity.getSurname());
            return accountCredentials;
        } else
            return null;
    }
}
