package team.moyu.fishfind.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public class CompKeyRespDTO {
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
}
