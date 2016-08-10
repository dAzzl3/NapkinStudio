package com.napkinstudio.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.sql.DataSource;

/**
 * Created by User1 on 22.07.2016.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
//        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .inMemoryAuthentication()
//                .withUser("user").password("password").authorities("ROLE_USER");
//    }
//
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/orders").authenticated()
                .and().formLogin()
                .loginPage("/login").defaultSuccessUrl("/orders").failureUrl("/login?error")
                .and()
                .logout().logoutSuccessUrl("/");

    }


    @Autowired
    public void configureGlobal(DataSource dataSource, AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select login,password,enabled from user where login = ?")
                .authoritiesByUsernameQuery("select user.Login, role.name from user " +
                        "join user_role on user.userId = user_role.users_userId join role on user_role.roles_id = role.id where user.login = ?");
    }

}
