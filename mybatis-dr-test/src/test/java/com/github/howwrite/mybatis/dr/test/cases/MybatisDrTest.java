package com.github.howwrite.mybatis.dr.test.cases;

import com.github.howwrite.mybatis.dr.starter.DrRepository;
import com.github.howwrite.mybatis.dr.test.BaseTest;
import com.github.howwrite.mybatis.dr.test.model.User;
import com.github.howwrite.mybatis.dr.test.model.UserQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;


public class MybatisDrTest extends BaseTest {
    @Test
    public void test_insert_and_findOne() {
        User entity = new User();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        entity.setName("v");
        entity.setBirthday(today);
        entity.setLastLoginTime(now);
        entity.setAddress("Pompeii");
        DrRepository.insert(entity);


        Optional<User> user = DrRepository.findOne(new UserQuery().eqName("v"));
        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals(entity.getName(), user.get().getName());
        Assertions.assertEquals(entity.getBirthday(), user.get().getBirthday());
        Assertions.assertTrue(Math.abs(entity.getLastLoginTime().getNano() - user.get().getLastLoginTime().getNano()) <= 1000);
        Assertions.assertEquals(entity.getAddress(), user.get().getAddress());
    }
}
