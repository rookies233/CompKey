package team.moyu.fishfind.vo;


public class CommentVo {

  // id
  private Long id;
  // 用户名
  private String username;
  // 关联的竞争性关键词id
  private Long compwordId;
  // 分数
  private int grade;
  // 评论内容
  private String text;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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
