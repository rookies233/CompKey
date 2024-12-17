package team.moyu.fishfind.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

/**
 * @author moyu
 */
@DataObject
@RowMapped
public class Comment {

  // 评论id
  private Long id;
  // 用户id
  @Column(name = "user_id")
  private Long userId;
  // 关联的竞争性关键词id
  @Column(name = "compword_id")
  private Long compwordId;
  // 分数
  private int grade;
  // 评论内容
  @Column(name = "comment_text")
  private String text;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getCompwordId() {
    return compwordId;
  }

  public void setCompwordId(Long compwordId) {
    this.compwordId = compwordId;
  }

  public int getGrade() {
    return grade;
  }

  public void setGrade(int grade) {
    this.grade = grade;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
