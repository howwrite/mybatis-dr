# MyBatis Dynamic Repository

MyBatis Dynamic Repository（mybatis-dr）是在 MyBatis 之上提供“零 SQL”的增强层：通过注解声明实体、编译期生成 `Query` 条件类，并暴露
`DrRepository` 静态方法完成绝大多数 CRUD、批处理和条件查询逻辑。

## 核心能力

- `@DrTable` + `@DrColumn` 描述表/字段，未标记字段自动收敛到 `feature` JSON 列。
- 编译期（`mvn compile`）生成 `xxxQuery`，支持
  select/eq/ne/gt/ge/lt/le/in/notIn/like/likeLeft/likeRight/isNull/isNotNull/asc/desc、`setLimit`、`setPageInfo`、
  `selectKey`。
- `DrRepository` 提供
  insert、insertOrUpdate、batchInsert、batchInsertOrUpdate、update、delete/logicDelete、findByCondition、findOne、count，全量使用实体+Query
  即可。
- 自动补全 `created_time`、`updated_time`，可选逻辑删除（`deleted`、`deleted_time` 字段）。
- 支持 `DrColumn.converter` 自定义序列化，`DrColumnIgnore` 排除字段。
- Spring Boot Starter 自动装配 `DynamicSqlMapper`、`DrRepository` 所需依赖，避免手动配置。

## 模块概览

| 模块                               | 说明                                                                      |
|----------------------------------|-------------------------------------------------------------------------|
| `mybatis-dr-core`                | 注解、实体解析、`QueryCondition`、动态 Mapper XML。                                 |
| `mybatis-dr-core-processor`      | 注解处理器，生成 `EntityQuery`。                                                 |
| `mybatis-dr-spring-boot-starter` | 自动装配 `MyBatisDrConfiguration`、注入 `DynamicSqlMapper` 并暴露 `DrRepository`。 |

## 快速开始

### 1. 引入依赖
```xml

<dependencies>
    <dependency>
        <groupId>com.github.howwrite</groupId>
        <artifactId>mybatis-dr-spring-boot-starter</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 2. 开启编译期 Query 生成

`mybatis-dr-core-processor` 只在编译期运行，建议通过 `maven-compiler-plugin` 指定：

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.github.howwrite</groupId>
                <artifactId>mybatis-dr-core-processor</artifactId>
                <version>${mybatis-dr.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

运行 `mvn compile` 即会生成 `xxxQuery` 源码并写入 `target/generated-sources/annotations`。

### 3. Spring Boot & MyBatis 配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://host:3306/demo?useSSL=false&characterEncoding=UTF-8
    username: ***
    password: ***
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis:
  mapper-locations: classpath*:mapping/mybatis-dr-mapper.xml
```

Starter 自动注册 `com.github.howwrite.mapper.DynamicSqlMapper`，如你已有 `@MapperScan`，确保包含该包或显式排除重复配置。

## 定义实体
```java

@Data
@DrTable(
        value = "user",
        logicDelete = true,                // 开启逻辑删除，delete 自动转 update deleted/deleted_time
        featureColumnName = "feature",     // 默认即可，如需改名可在此修改
        whenDuplicateUpdateCreatedTime = false
)
public class User {
    @DrColumn("id")
    private Long id;

    @DrColumn("name")
    private String name;

    @DrColumn(value = "phone", query = false) // 存进 feature JSON
    private String phone;

    @DrColumn(value = "tags", query = false, converter = JsonListConverter.class)
    private List<String> tags;

    @DrColumn("created_time")
    private LocalDateTime createdTime;

    @DrColumn("updated_time")
    private LocalDateTime updatedTime;

    @DrColumnIgnore
    private String transientField;
}
```

- 标记了 `@DrColumn(query = true)` 的字段直接映射到列；未标记或 `query = false` 的字段或者未注明@DrColumn注解的字段最终序列化为
  `feature` JSON。
- `DrColumn.converter` 可实现 `DrConverter` 接口将数据库值与 Java 类型互转。
- `DrColumnIgnore` 完全排除字段。

## 自动生成的 `UserQuery`

编译后自动生成与实体包名一致、类名为 `UserQuery` 的条件类：

```java
UserQuery query = new UserQuery()
        .eqId(1L)
        .likeRightName("jack")
        .inId(List.of(1L, 2L, 3L))
        .descId()
        .setPageInfo(1, 20)            // 自动计算 limit/offset
        .selectKey(UserQuery.selectId, UserQuery.selectName);
```

每个查询字段自带 `eq/ne/gt/ge/lt/le/in/notIn/like/likeLeft/likeRight/isNull/isNotNull/asc/desc`，`selectKey` 控制投影列，
`setLimit`/`setOffset`/`setPageInfo` 用于分页。

## DrRepository API 速查

| 方法                                                | 说明                                           |
|---------------------------------------------------|----------------------------------------------|
| `insert(Object entity)`                           | 写入实体并回填自增 id。                                |
| `insertOrUpdate(Object entity)`                   | 单条 UPSERT，冲突字段由 `whenDuplicateUpdate` 控制。    |
| `batchInsert(List<?> entities)`                   | 批量插入并回填主键（保持同一实体类型）。                         |
| `batchInsertOrUpdate(List<?> entities)`           | 批量 UPSERT。                                   |
| `update(Object entity, QueryCondition condition)` | 条件更新指定字段（只写入非 null 字段）。                      |
| `delete(QueryCondition condition)`                | 条件删除；`@DrTable(logicDelete=true)` 时自动执行逻辑删除。 |
| `findByCondition(QueryCondition)`                 | 返回实体列表，会自动反序列化 `feature` JSON。               |
| `findOne(QueryCondition)`                         | `Optional<T>`，内部强制 `limit 1`。                |
| `count(QueryCondition)`                           | 返回 `long`，可配合 `selectKey` 指定计数字段。            |

所有方法依赖 Spring 容器中的 `DynamicSqlMapper`，确保应用启动后再调用。

## 常见用法

### 基础 CRUD

```java
User user = new User();
user.

setName("g"); user.

setPhone("23114"); user.

setTags(List.of("vip"));
        DrRepository.

insert(user);                         // 自动写入 feature={"phone":"23114","tags":["vip"]}

Optional<User> found = DrRepository.findOne(new UserQuery().eqId(user.getId()));
DrRepository.

update(new User().

setName("z"), new

UserQuery().

eqId(user.getId()));
        DrRepository.

delete(new UserQuery().

eqId(user.getId()));   // logicDelete=true 时只更新 deleted 列
```

### 条件查询与分页
```java
List<User> page = DrRepository.findByCondition(
        new UserQuery()
                .likeLeftName("jack")
                .selectKey(UserQuery.selectId, UserQuery.selectName)
                .setPageInfo(2, 10));
long total = DrRepository.count(new UserQuery().likeLeftName("jack"));
```

### 批量写入/更新
```java
List<User> list = LongStream.range(0, 100)
        .mapToObj(i -> {
            User u = new User();
            u.setId(i);                       // 有 id = upsert
            u.setName("batch_" + i);
            return u;
        }).toList();
DrRepository.

batchInsertOrUpdate(list);
```

### Insert Or Update 精细控制

```java
@DrColumn(value = "last_login_time", whenDuplicateUpdate = false)
private LocalDateTime lastLoginTime;

User u = new User();
u.

setId(1L);
u.

setName("keep created_time");
DrRepository.

insertOrUpdate(u); // 若唯一索引冲突，只按允许的列更新
```

`@DrTable(whenDuplicateUpdateCreatedTime = true)` 可在冲突时刷新 `created_time`。

### JSON 扩展字段

未声明 `@DrColumn` 或者`@DrColumn(query=false)` 的字段会自动写入 `feature`。例如：

```java

@Data
@DrTable("user")
public class SimpleUser {
    @DrColumn("id")
    private Long id;
    @DrColumn("name")
    private String name;
    private String address;    // -> feature{"address": "..."}
    private Map<String, Object> extra; // -> feature{"extra": {...}}
}
```

## 注解与表结构约定

- `DrTable`
    - `value`：表名，必填。
    - `logicDelete`：开启后要求表存在 `deleted bigint unsigned not null default 0`、`deleted_time datetime`。
    - `featureColumnName`：JSON 扩展列名（默认 `feature`，类型建议 `longtext`）。
    - `createdTimeColumnName` / `updatedTimeColumnName`：自动写入当前时间。
    - `idColumnName`：主键列名，`DrRepository` 会尝试回填。
- `DrColumn`
    - `value`：列名。
    - `query`：true=独立列，false=写入 `feature`。
    - `whenDuplicateUpdate`：`insertOrUpdate`/`batchInsertOrUpdate` 冲突时是否更新该列。
    - `converter`：实现 `DrConverter` 用于复杂类型（如 `List<Long>`、`BigDecimal`、`JSON`）。
- `DrColumnIgnore`：字段完全忽略。

表需要至少包含：`id`（或自定义主键）、`feature`、`created_time`、`updated_time`。若启用逻辑删除则额外包含 `deleted`、
`deleted_time`。

## 验证

- `mvn compile`：确保 Query 类成功生成。
- `mvn test -pl mybatis-dr-spring-boot-starter`：可运行内置 H2 集成用例快速验证 CRUD/批处理能力。
- 线上接入时优先配置唯一索引，充分利用 `insertOrUpdate`/`batchInsertOrUpdate` 带来的幂等写入能力。

完成以上配置即可在 Domain 层直接使用 `DrRepository` 与 `UserQuery` 实现高效率的 CRUD/查询逻辑，无需再维护重复 Mapper/SQL。
