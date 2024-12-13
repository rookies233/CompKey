package team.moyu.fishfind.algorithm;

/**
 * @author moyu
 */
public class CompKeyResult {
  private String compWord;
  /**
   * 竞争度
   */
  private double compScore;

  public String getCompWord() {
    return compWord;
  }

  public void setCompWord(String compWord) {
    this.compWord = compWord;
  }

  public double getCompScore() {
    return compScore;
  }

  public void setCompScore(double compScore) {
    this.compScore = compScore;
  }
}
