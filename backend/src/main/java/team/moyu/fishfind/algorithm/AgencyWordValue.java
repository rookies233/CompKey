package team.moyu.fishfind.algorithm;

/**
 * 中介关键词信息
 *
 * @author moyu
 */
public class AgencyWordValue {

  /**
   * 联合查询搜索量，种子关键词和对应的中介关键词一起出现的查询搜索
   */
  private int sa;

  /**
   * 权重
   */
  private double weight;

  public AgencyWordValue() {
  }

  public AgencyWordValue(int sa, double weight) {
    this.sa = sa;
    this.weight = weight;
  }

  public int getSa() {
    return sa;
  }

  public void setSa(int sa) {
    this.sa = sa;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}
