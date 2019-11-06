package ru.sasha77.spring.pepsbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class ConfigSecurity extends WebSecurityConfigurerAdapter {
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    @Qualifier("MyUserService")
    UserDetailsService userDetailsService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
//                new PasswordEncoder() {
//            @Override
//            public String encode(CharSequence charSequence) {
//                return charSequence.toString();
//            }
//
//            @Override
//            public boolean matches(CharSequence charSequence, String s) {
//                return charSequence.toString().equals(s);
//            }
//        };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
//
//        auth
//            .inMemoryAuthentication().passwordEncoder(passwordEncoder())
//                .withUser("porky").password("pig").authorities("ROLE_USER")
//            .and()
//                .withUser("pluto").password("dog").authorities("ROLE_USER")
//            .and()
//                .withUser("masha")
//                .password("child")
//                .authorities("ROLE_USER")
//            .and()
//                .withUser("luntik")
//                .password("alien")
//                .authorities("ROLE_USER");
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
        .and()
            .authorizeRequests()
                .antMatchers("/login","/register").access("permitAll")
                .antMatchers("/**").hasRole("USER")
                .antMatchers("/rest/**").hasRole("USER")
        .and()
            .formLogin().loginPage("/login")
        /*.and()
            .csrf().disable()*/;
    }
}
