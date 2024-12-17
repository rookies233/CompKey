package team.moyu.fishfind.dto;

public class CompKeyReqDTO {
  private String seedWord;
  private Long userId;

  public String getSeedWord() {
    return seedWord;
  }

  public void setSeedWord(String seedWord) {
    this.seedWord = seedWord;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
