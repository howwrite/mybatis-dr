package com.github.howwrite.mybatis.dr.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis动态仓库自动配置类
 *
 * @author mybatis-dr
 */
@Configuration
@ComponentScan
@MapperScan("com.github.howwrite.mapper")
public class MyBatisDrConfiguration {

} 