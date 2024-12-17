package team.moyu.fishfind.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public class CompKeyRespDTO {
  private Integer compWordId;

  /**
   * 竞争性关键词
   */
  private String compWord;

  public String getCompWord() {
    return compWord;
  }

  public void setCompWord(String compWord) {
    this.compWord = compWord;
  }

  public Integer getCompWordId() {
    return compWordId;
  }

  public void setCompWordId(Integer compWordId) {
    this.compWordId = compWordId;
  }
}
