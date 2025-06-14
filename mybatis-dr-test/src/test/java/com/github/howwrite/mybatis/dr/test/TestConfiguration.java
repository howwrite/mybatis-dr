package com.github.howwrite.mybatis.dr.test;

import com.github.howwrite.mybatis.dr.starter.MyBatisDrConfiguration;
import com.github.howwrite.treasure.spring.BagSpringConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@SpringBootConfiguration

@ImportAutoConfiguration({MybatisAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class, BagSpringConfiguration.class, MyBatisDrConfiguration.class})
@ActiveProfiles("test")
public class TestConfiguration {

}
