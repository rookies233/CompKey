package team.moyu.fishfind.entity;

import java.util.Date;

/**
 * @author moyu
 */
public class UsedSeedWord {

  private Long id;
  private Long seedWordId;
  private Long userId;
  private String time;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSeedWordId() {
    return seedWordId;
  }

  public void setSeedWordId(Long seedWordId) {
    this.seedWordId = seedWordId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

}
