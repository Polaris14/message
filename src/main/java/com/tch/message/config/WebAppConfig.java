package com.tch.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebAppConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                List<String> list = new ArrayList<>();
                list.add("/login");
                list.add("/css/**");
                list.add("/dist/**");
                list.add("/fonts/**");
                list.add("/images/**");
                list.add("/js/**");
                list.add("/lib/**");
                list.add("/src/**");
                list.add("/webjars/**");
                list.add("/");
                registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                        HttpSession session = request.getSession();
                        if(session.getAttribute("user") == null){
                            response.sendRedirect("/");
                            return false;
                        }
                        return true;
                    }
                }).addPathPatterns("/**").excludePathPatterns(list);
            }
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("login");
            }
        };
    }

}
