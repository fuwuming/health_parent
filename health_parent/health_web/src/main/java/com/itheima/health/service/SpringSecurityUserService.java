package com.itheima.health.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.health.pojo.Permission;
import com.itheima.health.pojo.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SpringSecurityUserService
 * @Description TODO
 * @Author ly
 * @Company 深圳黑马程序员
 * @Date 2019/12/8 8:54
 * @Version V1.0
 */
@Component
public class SpringSecurityUserService implements UserDetailsService {

    // 订阅
    @Reference
    UserService userService;


    // 当使用SpringSecurity进行认证和授权的时候，一定会执行
    // 传递参数：String username，表示登录页面传递的登录名
    // 返回值：表示认证成功指定对象UserDetails，存放用户信息和权限的信息<security:user name="admin" password="{noop}admin" authorities="ROLE_ADMIN"
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1：完成认证，使用登录名作为查询条件，查询数据库，获取当前登录名对应的用户信息
        com.itheima.health.pojo.User user = userService.findUserByUsername(username);
        // 表示认证失败（登录名的校验）,return null;表示认证失败，此时页面会抛出异常（org.springframework.security.authentication.InternalAuthenticationServiceException:用户名输入有误）
        if(user==null){
            return null;
        }
        //2：完成授权，使用登录名作为查询条件，查询数据库，获取当前登录名对应的角色（keyword）和权限（keyword）
        List<GrantedAuthority> list = new ArrayList<>();
        if(user.getRoles()!=null && user.getRoles().size()>0){
            for (Role role : user.getRoles()) {
                // 权限控制（按照角色分配）
                // list.add(new SimpleGrantedAuthority(role.getKeyword()));// 具有角色
                // 权限控制（按照权限分配）
                if(role.getPermissions()!=null && role.getPermissions().size()>0){
                    for (Permission permission : role.getPermissions()) {
                        list.add(new SimpleGrantedAuthority(permission.getKeyword()));// 具有权限
                    }
                }
            }
        }

        //3：将用户信息和权限信息，存放到UserDetails的对象中
        // String password = "{noop}"+user.getPassword(); // 不需要任何加密机制（明文）
        String password = user.getPassword(); // 直接使用从数据库查询的密码
        UserDetails userDetails = new User(user.getUsername(),password,list);
        /*
           表示认证失败（密码的校验）,SpringSecurity底层会使用页面输入的密码，和UserDetails对象中存放的密码进行比对
           如果密码不一致，此时跳转到登录页面，抛出异常，表示密码输入有误（org.springframework.security.authentication.BadCredentialsException:表示密码输入有误）
         */
        return userDetails;
    }
}
