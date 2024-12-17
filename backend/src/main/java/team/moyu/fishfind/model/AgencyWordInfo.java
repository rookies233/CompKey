package team.moyu.fishfind.model;

public class AgencyWordInfo {
  /**
   * 中介关键词
   */
  private String agencyWord;
  /**
   * 中介关键词总出现次数
   */
  private long a;
  /**
   * 共现次数（s 与 a 同时出现的次数）
   */
  private long sa;

  public String getAgencyWord() {
    return agencyWord;
  }

  public void setAgencyWord(String agencyWord) {
    this.agencyWord = agencyWord;
  }

  public long getA() {
    return a;
  }

  public void setA(long a) {
    this.a = a;
  }

  public long getSa() {
    return sa;
  }

  public void setSa(long sa) {
    this.sa = sa;
  }
}
