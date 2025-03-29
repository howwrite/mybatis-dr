# MyBatis Dynamic Repository

MyBatis Dynamic Repository是一个基于MyBatis的增强框架，能够根据用户定义的模型类自动生成增删改查方法，大大简化数据库操作。

## 功能特性

- 自动生成基本的CRUD操作，无需编写SQL
- 支持通过注解定义表名和字段名
- 支持自动生成查询条件类
- 支持将未标记的字段自动转为JSON存储
- 自动处理created_time和updated_time字段
- 提供Spring Boot Starter，便于集成

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.howwrite</groupId>
    <artifactId>mybatis-dr-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 定义实体类

```java
import com.github.howwrite.annotation.DrColumn;
import com.github.howwrite.annotation.DrColumnIgnore;
import com.github.howwrite.annotation.DrTable;

import java.util.Date;

@Table("user")
public class User {
    
    @Field(id = true)
    private Long id;
    
    @Field
    private String username;
    
    @Field
    private String password;
    
    @Field
    private Integer age;
    
    @Field(name = "email_address")
    private String email;
    
    @Field
    private Date created_time;
    
    @Field
    private Date updated_time;
    
    // 没有@Field注解的字段会被自动存为JSON
    private String address;
    private String phoneNumber;
    
    @FieldIgnore
    private String tempField; // 被忽略的字段，不会被持久化
    
    // ... getter & setter
}
```

### 3. 创建Repository

```java
import com.github.howwrite.core.BaseDynamicRepository;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends BaseDynamicRepository<User, Long> {
    
    public UserRepository(SqlSession sqlSession) {
        super(sqlSession);
    }
}
```

### 4. 使用Repository

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    public User create(@RequestBody User user) {
        userRepository.insert(user);
        return user;
    }
    
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userRepository.findById(id);
    }
    
    @GetMapping
    public List<User> getAll() {
        return userRepository.findAll();
    }
    
    @GetMapping("/search")
    public List<User> search(@RequestParam String username, @RequestParam Integer minAge) {
        // 使用自动生成的查询类
        UserQuery query = new UserQuery();
        query.likeUsername(username)
             .geAge(minAge)
             .isNotNullEmail();
        
        return userRepository.findByCondition(query);
    }
    
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userRepository.update(user);
        return user;
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}
```

## 配置选项

在`application.properties`或`application.yml`中可以进行以下配置：

```properties
# 是否启用JSON类型处理器，默认为true
mybatis.dr.enable-json-type-handler=true
```

## 注解说明

- `@Table`: 用于指定实体类对应的数据库表名
- `@Field`: 用于指定实体类字段对应的数据库列名，可以设置是否为主键、是否可为空、是否可查询等属性
- `@FieldIgnore`: 用于标记不需要持久化的字段

## 查询条件支持

框架会在编译期间为每个实体类生成对应的Query类，支持以下条件：

- 等于(eq)/不等于(ne)
- 大于(gt)/大于等于(ge)/小于(lt)/小于等于(le)
- 包含(in)/不包含(not in)
- 模糊匹配(like)/左模糊匹配(likeLeft)/右模糊匹配(likeRight)
- 为空(isNull)/不为空(isNotNull)

## 注意事项

- 实体类必须有一个主键字段，如果没有使用`@Field(id = true)`标记，则默认使用名为"id"的字段作为主键
- JSON字段会被自动转换为JSON字符串存储在名为"feature"的列中
- created_time和updated_time字段会被自动维护，插入时自动设置，更新时自动更新 