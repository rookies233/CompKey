package team.moyu.fishfind.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

/**
 * CREATE TABLE compword
 * (
 *   id            INT AUTO_INCREMENT PRIMARY KEY COMMENT '竞争关键词ID',
 *   word          VARCHAR(20) NOT NULL COMMENT '竞争关键词名称',
 *   compdegree    DOUBLE      NOT NULL COMMENT '竞争关键词竞争度',
 *   seedword_id   INT         NOT NULL COMMENT '种子关键词ID',
 *   agencyword_id INT         NOT NULL COMMENT '中介关键词ID',
 *   FOREIGN KEY (seedword_id) REFERENCES seedword (id) ON DELETE CASCADE,
 *   FOREIGN KEY (agencyword_id) REFERENCES agencyword (id) ON DELETE CASCADE
 * ) COMMENT = '竞争关键词表';
 */
@DataObject
@RowMapped
public class CompWord {
  @Column(name = "id")
  private Long id;
  @Column(name = "word")
  private String word;
  @Column(name = "compdegree")
  private Double compDegree;
}
