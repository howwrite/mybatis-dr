package com.github.howwrite.mybatis.dr.starter.autoconfigure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

/**
 * MyBatis动态仓库自动配置类
 *
 * @author mybatis-dr
 */
@Configuration
@ConditionalOnClass({SqlSessionFactory.class})
@ComponentScan
@MapperScan("com.github.howwrite.mapper")
public class MyBatisDynamicRepositoryAutoConfiguration {

} 