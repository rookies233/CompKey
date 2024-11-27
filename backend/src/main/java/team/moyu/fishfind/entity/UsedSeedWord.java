package team.moyu.fishfind.entity;

import java.util.Date;

/**
 * @author moyu
 */
public class UsedSeedWord {

  private Long id;
  private Long seedWordId;
  private Long userId;
  private Date time;

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

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

}
