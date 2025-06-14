CREATE TABLE if not exists user_test
(
    `id`              bigint primary key auto_increment comment 'id',
    `name`            VARCHAR(50) not null comment '用户名',
    `feature` longtext comment '扩展内容',
    `birthday` datetime comment '创建时间',
    `last_login_time` datetime comment '创建时间',
    `created_time`    datetime    not null default CURRENT_TIMESTAMP comment '创建时间',
    `updated_time`    datetime    not null default CURRENT_TIMESTAMP comment '记录修改时间'
);