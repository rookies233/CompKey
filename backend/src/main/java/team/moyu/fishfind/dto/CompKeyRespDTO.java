package team.moyu.fishfind.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public class CompKeyRespDTO {
  private Long compWordId;

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

  public Long getCompWordId() {
    return compWordId;
  }

  public void setCompWordId(Long compWordId) {
    this.compWordId = compWordId;
  }
}
