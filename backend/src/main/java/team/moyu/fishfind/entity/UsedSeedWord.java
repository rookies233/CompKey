package team.moyu.fishfind.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author moyu
 */
@DataObject
@RowMapped
public class UsedSeedWord {

  private Long id;
  @Column(name = "seedword_id")
  private Long seedWordId;
  private Long userId;
  private LocalDateTime time;

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

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }
}
