package team.moyu.fishfind.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.vertx.codegen.annotations.DataObject;

import java.time.LocalDateTime;

/**
 * @author moyu
 */
public class UsedSeedWordsVo {

  private String seedWord;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime time;

  public String getSeedWord() {
    return seedWord;
  }

  public void setSeedWord(String seedWordId) {
    this.seedWord = seedWordId;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }
}
