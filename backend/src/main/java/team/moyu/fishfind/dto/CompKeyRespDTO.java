package team.moyu.fishfind.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  private Double compScore;

  /**
   * 竞争关键词 k 与中介关键词 a 的共现次数
   */
  @JsonIgnore
  private Long ka;

  public Long getKa() {
    return ka;
  }

  public void setKa(Long ka) {
    this.ka = ka;
  }

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

  public Double getCompScore() {
    return compScore;
  }

  public void setCompScore(Double compScore) {
    this.compScore = compScore;
  }
}
