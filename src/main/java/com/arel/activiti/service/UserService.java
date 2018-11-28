package com.arel.activiti.service;

import com.arel.activiti.config.security.AccountCredentials;
import com.arel.activiti.config.security.Authorities;
import com.arel.activiti.model.entity.UserEntity;
import com.arel.activiti.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static int ADMINISTRATOR_CODE = 0;
    private static int OFFICER = 1;
    private static int CHIEF = 2;
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
        UserEntity userEntity = userRepository.findByEmail(username);

        return getUserDetails(userEntity);
    }

    public UserEntity getUser(String username) {
        return userRepository.findByEmail(username);
    }

    public List<UserDetails> getAllUsers() {
        List<UserEntity> userList = userRepository.findAll();
        return userList.stream().map(item -> getUserDetails(item)).collect(Collectors.toList());
    }


    private UserDetails getUserDetails(UserEntity userEntity) {
        AccountCredentials accountCredentials = new AccountCredentials();
        if (userEntity != null) {
            accountCredentials.setId(userEntity.getId());
            accountCredentials.setUsername(userEntity.getEmail());
            accountCredentials.setPassword(userEntity.getPassword());
            accountCredentials.setName(userEntity.getName());
            accountCredentials.setSurname(userEntity.getSurname());
            accountCredentials.setAuthorities(getAuthority(userEntity.getRole()));
            accountCredentials.setUserGroup(userEntity.getUserGroup());
            return accountCredentials;
        } else
            return null;
    }


    private Collection<GrantedAuthority> getAuthority(int role) {
        Collection<GrantedAuthority> grantedAuthories = new ArrayList<>();

        if (role == ADMINISTRATOR_CODE)
            grantedAuthories.add(Authorities.ADMINISTRATOR);
        else if (role == CHIEF)
            grantedAuthories.add(Authorities.CHIEF);
        else if (role == OFFICER)
            grantedAuthories.add(Authorities.OFFICER);
        else throw new RuntimeException("Unexpected role: " + role);

        return grantedAuthories;
    }
}
