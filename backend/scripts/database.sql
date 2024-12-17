DROP DATABASE IF EXISTS fishfind;
CREATE DATABASE IF NOT EXISTS fishfind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fishfind;
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
# CREATE TABLE agencyword
# (
#   id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '中介关键词ID',
#   word        VARCHAR(20) NOT NULL COMMENT '中介关键词名称',
#   seedword_id INT         NOT NULL COMMENT '种子关键词ID',
#   FOREIGN KEY (seedword_id) REFERENCES seedword (id) ON DELETE CASCADE
# ) COMMENT = '中介关键词表';
CREATE TABLE compword
(
  id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '竞争关键词ID',
  word       VARCHAR(20) COMMENT '竞争关键词名称',
  compdegree DOUBLE COMMENT '竞争关键词竞争度'
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
CREATE TABLE comments
(
  id           INT AUTO_INCREMENT PRIMARY KEY,
  user_id      INT NOT NULL, -- 用户ID
  compword_id  INT,          -- 可选，关联的竞争性关键词ID
  grade        int,          -- 分数
  comment_text TEXT          -- 评论内容
);

INSERT INTO user (username, password, telephone, email)
VALUES ('admin', '123456', '12345678901', '12345678901@qq.com');
INSERT INTO user (username, password, telephone, email)
VALUES ('xst', '123456', '12345678901', '12345678901@qq.com');
INSERT INTO user (username, password, telephone, email)
VALUES ('cz', '123456', '12345678901', '12345678901@qq.com');
INSERT INTO user (username, password, telephone, email)
VALUES ('hyx', '123456', '12345678901', '12345678901@qq.com');
INSERT INTO user (username, password, telephone, email)
VALUES ('gz', '123456', '18379515489', '2235837213@qq.com');

INSERT INTO seedword (word, introduction)
VALUES ('图片', '图片');

# INSERT INTO agencyword(word, seedword_id)
# VALUES ('大全', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('发型', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('报价', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('唯美', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('价格', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('搞笑', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('2016', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('手机', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('qq', '1');
# INSERT INTO agencyword(word, seedword_id)
# VALUES ('动漫', '1');

INSERT INTO used_seedword(seedword_id, user_id, time)
VALUES ('1', '1', '2024-11-27 11:30:00')

INSERT INTO comments(user_id, compword_id, grade, comment_text)
VALUES ('4',1,3.5,'推荐的什么玩意');
INSERT INTO comments(user_id, compword_id, grade, comment_text)
VALUES ('3',2,5,'还可以');
INSERT INTO comments(user_id, compword_id, grade, comment_text)
VALUES ('5',3,0,'不错');

INSERT INTO compword(word,compdegree)
VALUES ('中秋节',85);
INSERT INTO compword(word,compdegree)
VALUES ('视频',85);
INSERT INTO compword(word,compdegree)
VALUES ('下载',85);
