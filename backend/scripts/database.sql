CREATE
  DATABASE fishfind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE
  fishfind;
DROP TABLE IF EXISTS used_seedword;
DROP TABLE IF EXISTS compword;
DROP TABLE IF EXISTS agencyword;
DROP TABLE IF EXISTS seedword;
DROP TABLE IF EXISTS user;
CREATE TABLE user
(
  id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
  username  VARCHAR(20)  NOT NULL COMMENT '用户名',
  password  VARCHAR(128) NOT NULL COMMENT '密码',
  telephone VARCHAR(11) DEFAULT NULL COMMENT '用户手机号码',
  email     VARCHAR(20) DEFAULT NULL COMMENT '用户邮箱'
) COMMENT = '用户表';
CREATE TABLE seedword
(
  id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '种子关键词ID',
  word         VARCHAR(20) NOT NULL COMMENT '种子关键词名称',
  introduction VARCHAR(500) DEFAULT NULL COMMENT '种子关键词介绍信息'
) COMMENT = '种子关键词表';
CREATE TABLE agencyword
(
  id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '中介关键词ID',
  word        VARCHAR(20) NOT NULL COMMENT '中介关键词名称',
  seedword_id INT         NOT NULL COMMENT '种子关键词ID',
  FOREIGN KEY (seedword_id) REFERENCES seedword (id) ON DELETE CASCADE
) COMMENT = '中介关键词表';
CREATE TABLE compword
(
  id            INT AUTO_INCREMENT PRIMARY KEY COMMENT '竞争关键词ID',
  word          VARCHAR(20) NOT NULL COMMENT '竞争关键词名称',
  compdegree    DOUBLE      NOT NULL COMMENT '竞争关键词竞争度',
  seedword_id   INT         NOT NULL COMMENT '种子关键词ID',
  agencyword_id INT         NOT NULL COMMENT '中介关键词ID',
  FOREIGN KEY (seedword_id) REFERENCES seedword (id) ON DELETE CASCADE,
  FOREIGN KEY (agencyword_id) REFERENCES agencyword (id) ON DELETE CASCADE
) COMMENT = '竞争关键词表';
CREATE TABLE used_seedword
(
  id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '历史种子关键词ID',
  seedword_id INT      NOT NULL COMMENT '种子关键词ID',
  user_id     INT      NOT NULL COMMENT '用户ID',
  time        DATETIME NOT NULL COMMENT '搜索时间',
  FOREIGN KEY (seedword_id) REFERENCES seedword (id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
) COMMENT = '用户历史关键词表';

INSERT INTO user (username, password, telephone, email)
VALUES ('admin', '123456', '12345678901', '12345678901@qq.com');
