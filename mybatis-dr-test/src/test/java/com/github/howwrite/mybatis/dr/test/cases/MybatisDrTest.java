package com.github.howwrite.mybatis.dr.test.cases;

import com.github.howwrite.mybatis.dr.starter.DrRepository;
import com.github.howwrite.mybatis.dr.test.BaseTest;
import com.github.howwrite.mybatis.dr.test.model.User;
import com.github.howwrite.mybatis.dr.test.model.UserQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


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

    @Test
    public void test_insert_and_verifyAutoGenId() {
        User user = new User();
        user.setName("test_insert");
        DrRepository.insert(user);

        Assertions.assertNotNull(user.getId());

        Optional<User> foundUser = DrRepository.findOne(new UserQuery().eqId(user.getId()));
        Assertions.assertTrue(foundUser.isPresent());
        Assertions.assertEquals(user.getName(), foundUser.get().getName());
    }

    @Test
    public void test_insertOrUpdate_newEntity() {
        User user = new User();
        user.setName("new_user");
        DrRepository.insertOrUpdate(user);

        Optional<User> foundUser = DrRepository.findOne(new UserQuery().eqId(user.getId()));
        Assertions.assertTrue(foundUser.isPresent());
        Assertions.assertEquals("new_user", foundUser.get().getName());
    }

    @Test
    public void test_batchInsert_multipleEntities() {
        List<User> users = IntStream.range(0, 5)
                .mapToObj(i -> {
                    User u = new User();
                    u.setName("user_" + i);
                    return u;
                }).collect(Collectors.toList());

        DrRepository.batchInsert(users);

        users.forEach(u -> {
            Optional<User> found = DrRepository.findOne(new UserQuery().eqName(u.getName()));
            Assertions.assertTrue(found.isPresent());
            Assertions.assertNotNull(found.get().getId());
        });
    }

    @Test
    public void test_batchInsertOrUpdate_mixedData() {
        // 创建一些新用户和一些已存在的用户
        UserQuery userQuery = new UserQuery();
        userQuery.setLimit(2);
        List<User> existingUsers = DrRepository.findByCondition(userQuery);
        List<User> newUsers = IntStream.range(0, 3)
                .mapToObj(i -> {
                    User u = new User();
                    u.setName("new_user_" + i);
                    return u;
                }).collect(Collectors.toList());

        existingUsers.forEach(u -> u.setName("updated_by_batch"));
        List<User> allUsers = Stream.concat(existingUsers.stream(), newUsers.stream()).collect(Collectors.toList());

        DrRepository.batchInsertOrUpdate(allUsers);

        existingUsers.forEach(u -> {
            Optional<User> updated = DrRepository.findOne(new UserQuery().eqId(u.getId()));
            Assertions.assertEquals("updated_by_batch", updated.get().getName());
        });

        newUsers.forEach(u -> {
            Optional<User> found = DrRepository.findOne(new UserQuery().eqName(u.getName()));
            Assertions.assertTrue(found.isPresent());
        });
    }

    @Test
    public void test_update_withCondition() {
        User user1 = new User();
        user1.setName("before_update_1");
        DrRepository.insert(user1);

        User user2 = new User();
        user2.setName("before_update_2");
        DrRepository.insert(user2);

        User updateEntity = new User();
        updateEntity.setName("after_update");

        DrRepository.update(updateEntity, new UserQuery().eqId(user1.getId()));

        Optional<User> check1 = DrRepository.findOne(new UserQuery().eqId(user1.getId()));
        Optional<User> check2 = DrRepository.findOne(new UserQuery().eqId(user2.getId()));

        Assertions.assertEquals("after_update", check1.get().getName());
        Assertions.assertEquals("before_update_2", check2.get().getName());
    }

    @Test
    public void test_delete_withCondition() {
        User user1 = new User();
        user1.setName("to_delete");
        DrRepository.insert(user1);

        User user2 = new User();
        user2.setName("keep");
        DrRepository.insert(user2);

        DrRepository.delete(new UserQuery().eqName("to_delete"));

        Optional<User> deleted = DrRepository.findOne(new UserQuery().eqId(user1.getId()));
        Optional<User> kept = DrRepository.findOne(new UserQuery().eqId(user2.getId()));

        Assertions.assertFalse(deleted.isPresent());
        Assertions.assertTrue(kept.isPresent());
    }

    @Test
    public void test_findByCondition_matchingResults() {
        // 插入测试数据
        User user1 = new User();
        user1.setName("search_test");
        DrRepository.insert(user1);
        User user2 = new User();
        user2.setName("search_test");
        DrRepository.insert(user2);

        List<User> results = DrRepository.findByCondition(new UserQuery().eqName("search_test"));
        Assertions.assertEquals(2, results.size());
    }

    @Test
    public void test_findOne_noMatch_returnsEmpty() {
        Optional<User> result = DrRepository.findOne(new UserQuery().eqName("nonexistent"));
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void test_count_correctValue() {
        // 插入测试数据
        IntStream.range(0, 5).forEach(i -> {
            User u = new User();
            u.setName("count_test");
            DrRepository.insert(u);
        });

        long count = DrRepository.count(new UserQuery().eqName("count_test"));
        Assertions.assertEquals(5, count);
    }

}
