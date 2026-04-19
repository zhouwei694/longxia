# 数据库初始化

-- 创建库
create database if not exists longxia;

-- 切换库
use longxia;

-- 用户表
-- 以下是建表语句

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;

CREATE TABLE if not exists `coupon` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                          `coupon_name` varchar(20) NOT NULL COMMENT '卡券名称',
                          `coupon_no` varchar(20) NOT NULL COMMENT '卡券号，从00001开始',
                          `coupon_password` varchar(100) NOT NULL COMMENT '卡券密码',
                          `display_amount` decimal(10,2) NOT NULL COMMENT '卡券展示金额',
                          `actual_amount` decimal(10,2) NOT NULL COMMENT '卡券实际金额',
                          `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '卡券状态：0-未激活，1-已激活，2-已核销',
                          `create_by` varchar(50) NOT NULL COMMENT '创建人',
                          `verify_by` varchar(50) DEFAULT NULL COMMENT '核销人',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                          `verify_time` datetime DEFAULT NULL COMMENT '核销时间',
                          `recipient_phone` varchar(20) DEFAULT NULL COMMENT '收件人电话',
                          `recipient_express_no` varchar(50) DEFAULT NULL COMMENT '收件人快递号',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_coupon_no` (`coupon_no`),
                          KEY `idx_status` (`status`),
                          KEY `idx_create_time` (`create_time`)
) COMMENT='卡券表' collate = utf8mb4_unicode_ci;

ALTER TABLE coupon ADD COLUMN `recipient_phone` varchar(20) DEFAULT NULL COMMENT '收件人电话';
ALTER TABLE coupon ADD COLUMN `recipient_express_no` varchar(50) DEFAULT NULL COMMENT '收件人快递号';
ALTER TABLE coupon ADD COLUMN `coupon_url` MEDIUMTEXT DEFAULT NULL COMMENT '卡券图片地址';
ALTER TABLE coupon MODIFY COLUMN `coupon_url` MEDIUMTEXT DEFAULT NULL COMMENT '卡券图片地址';
TRUNCATE TABLE coupon;