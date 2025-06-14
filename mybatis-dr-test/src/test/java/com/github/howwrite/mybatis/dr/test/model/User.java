package com.github.howwrite.mybatis.dr.test.model;


import com.github.howwrite.annotation.DrColumn;
import com.github.howwrite.annotation.DrTable;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@DrTable("user_test")
public class User {

    @DrColumn("id")
    private Long id;

    @DrColumn("name")
    private String name;

    @DrColumn("birthday")
    private LocalDate birthday;

    @DrColumn("last_login_time")
    private LocalDateTime lastLoginTime;

    @DrColumn(value = "address", query = false)
    private String address;

    @DrColumn("created_time")
    private LocalDateTime createdTime;

    @DrColumn("updated_time")
    private LocalDateTime updatedTime;
}
