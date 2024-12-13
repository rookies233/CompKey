package team.moyu.fishfind.algorithm;

/**
 * 竞争性关键词数据
 */
public class CompWordData {
  private String compWord;
  /**
   * 竞争性关键词的词频（不含有种子关键词，但有中介关键词的搜索）
   */
  private int ka;

  public String getCompWord() {
    return compWord;
  }

  public void setCompWord(String compWord) {
    this.compWord = compWord;
  }

  public int getKa() {
    return ka;
  }

  public void setKa(int ka) {
    this.ka = ka;
  }
}
