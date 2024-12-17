package team.moyu.fishfind.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

/**
 * CREATE TABLE seedword
 * (
 *   id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '种子关键词ID',
 *   word         VARCHAR(20) NOT NULL COMMENT '种子关键词名称',
 *   introduction VARCHAR(500) DEFAULT NULL COMMENT '种子关键词介绍信息'
 * ) COMMENT = '种子关键词表';
 */
@DataObject
@RowMapped
public class SeedWord {
  @Column(name = "id")
  private Long id;
  @Column(name = "word")
  private String word;
  @Column(name = "introduction")
  private String introduction;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public String getIntroduction() {
    return introduction;
  }

  public void setIntroduction(String introduction) {
    this.introduction = introduction;
  }
}
