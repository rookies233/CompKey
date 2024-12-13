package team.moyu.fishfind.algorithm;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;
import org.apache.commons.io.FileUtils;

/**
 * @author moyu
 */
public class CompKeyAlgorithm {

  private static final String FILE_PATH = "data/processed/cleaned.txt";

  private static final JiebaSegmenter SEGMENTER = new JiebaSegmenter();

  private static final String DICTIONARY_PATH = "data/dict/";

  private static final String STOP_WORDS_PATH = "data/stop_words/merge_stopwords.txt";

  private static final List<String> STOP_WORDS;

  static {
    try {
      STOP_WORDS = FileUtils.readLines(new File(STOP_WORDS_PATH), "utf8");
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    // 加载路径中的所有词典
    File dictDir = new File(DICTIONARY_PATH);
    File[] dictFiles = dictDir.listFiles();
    if (dictFiles != null) {
      for (File dictFile : dictFiles) {
        WordDictionary.getInstance().loadUserDict(dictFile.toPath());
      }
    }
  }

  public static List<CompKeyResult> compute(String seedWord) {
    return compute(seedWord, 10);
  }

  /**
   * 获取前 N 个竞争性关键词
   *
   * @param seedWord 种子关键词
   * @param n        竞争性关键词数量
   * @return 竞争性关键词
   */
  public static List<CompKeyResult> compute(String seedWord, int n) {
    // 获取中介关键词
    Map<String, AgencyWordValue> agencyWords = extractAgencyWords(seedWord);
    // 打印中介关键词
    System.out.println("中介关键词 -> 搜索量(sa) -> 权重(wa)");
    for (Map.Entry<String, AgencyWordValue> entry : agencyWords.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue().getSa() + ": " + entry.getValue().getWeight());
    }

    // 筛选出不含种子关键词，但含有其中介关键词的搜索数据
    Map<String, List<String>> agencyWordData = new HashMap<>();
    for (String agencyWord : agencyWords.keySet()) {
      agencyWordData.put(agencyWord, dataWithAgencyWord(seedWord, agencyWord));
    }

    // 对得到的数据，按照中介关键词对搜索内容分词，进行词频统计。
    // 打印出词频前三的词，词频最高的词一般是中介关键词，再基于语义筛选出竞争性关键词。
    // 对每个中介关键词，都可确定一个其种子关键词对应的竞争性关键字。
    List<CompWordData> compWordData = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : agencyWordData.entrySet()) {
      Map<String, Integer> wordFrequency = new HashMap<>();
      for (String line : entry.getValue()) {
        List<String> segResults = segWords(line);
        segResults.removeIf(word -> word.equals(seedWord));
        segResults.removeAll(STOP_WORDS);
        for (String word : segResults) {
          wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        }
      }

      // 获取频率最高的前 3 个词
      List<String> top3Words = wordFrequency.entrySet().stream()
        .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
        .limit(3)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

      // 筛选出竞争性关键词
      String compWord = top3Words.stream()
        .filter(word -> !agencyWords.containsKey(word))
        .findFirst()
        .orElse(null);

      CompWordData compWordDataItem = new CompWordData();
      compWordDataItem.setCompWord(compWord);
      // |{ka}| 竞争性关键词的词频（不含有种子关键词，但有中介关键词的搜索）
      compWordDataItem.setKa(wordFrequency.getOrDefault(compWord, 0));
      compWordData.add(compWordDataItem);
      System.out.println(entry.getKey() + ": " + top3Words + " -> " + compWord);
    }

    // 局部竞争性计算
    // - 竞争性 Comp 测度的计算公式：$$Comp_s(k,s)=\frac{|\{ka\}|}{(|\{a\}|-|\{sa\}|)}$$
    // - 其中，$|\{ka\}|$ 表示关键词 k 与中介关键词 a 的联合查询量，$|\{a\}|$ 表示中介关键词 a 总查询量，$|\{sa\}|$ 表示种子关键词 s 与中介关键词 a 的联合查询量

    // 全局竞争性计算
    // - 关键词 k 与种子关键词 s 的竞争性程度：$$Comp(k,s)=\sum_{i=1}^{m}{\{w_{a_i}(k)\times Comp_{a_i}(k,s)\}}$$
    // - 其中，$w_{a_i}(k)$ 表示关键词 k 与中介关键词 a 的权重，$Comp_{a_i}(k,s)$ 表示关键词 k 与中介关键词 a 的竞争性
    // - 通过计算，得到最终的竞争性关键词
    List<CompKeyResult> finalCompKeyResults = new ArrayList<>();

    return finalCompKeyResults;
  }

  /**
   * 提取中介关键词
   *
   * @param seedWord
   * @return
   */
  private static Map<String, AgencyWordValue> extractAgencyWords(String seedWord) {
    int sCount = 0;
    // 词频
    Map<String, Integer> wordFrequency = new HashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
      String line;
      while ((line = br.readLine()) != null) {
        // 遍历处理每一行
        if (line.contains(seedWord)) {
          ++sCount;
          // 去除空白字符
          line = line.replaceAll("\\s+", "");

          // 分词处理
          List<String> segResults = segWords(line);

          // 去除结果中的种子关键词
          segResults.removeIf(word -> word.equals(seedWord));

          // 去除停用词
          segResults.removeAll(STOP_WORDS);

          // 统计词频
          for (String word : segResults) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    // 遍历每一个中介关键词，计算权重
    // 获取频率最高的前 quantity 个词，作为中介关键词
    wordFrequency = wordFrequency.entrySet().stream()
      .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
      .limit(10)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<String, AgencyWordValue> agencyWords = new HashMap<>();

    for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
      agencyWords.put(entry.getKey(), new AgencyWordValue(entry.getValue(), agencyWordWeight(entry.getValue(), sCount)));
    }

    return agencyWords;
  }

  /**
   * 局部竞争性计算
   *
   * @param ka 关键词 k 与中介关键词 a 的联合查询量
   * @param a  中介关键词 a 总查询量
   * @param sa 种子关键词 s 与中介关键词 a 的联合查询量
   * @return 局部竞争性
   */
  private static double computeCompA(int ka, int a, int sa) {
    return (double) ka / (a - sa);
  }

  /**
   * 全局竞争性计算
   * Comp(k,s)=
   * a
   * ∑
   * ​
   * [w
   * a
   * ​
   * (k)×Comp
   * a
   * ​
   * (k,s)]
   *
   * @return
   */
  private static double computeComp(List<ComputeCompParams> params) {
    return params.stream()
      .mapToDouble(param -> param.getWa() * param.getCompA())
      .sum();
  }

  class ComputeCompParams {
    double wa;
    double compA;

    public double getWa() {
      return wa;
    }

    public void setWa(double wa) {
      this.wa = wa;
    }

    public double getCompA() {
      return compA;
    }

    public void setCompA(double compA) {
      this.compA = compA;
    }
  }

  /**
   * 计算中介关键词的搜索量|{a}|
   *
   * @param agencyWord
   * @return
   */
  private static int computeA(String agencyWord) {
    if (agencyWord == null || agencyWord.isEmpty()) {
      return 0;
    }
    int aCount = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains(agencyWord)) {
          ++aCount;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return aCount;
  }

  /**
   * 计算中介关键词权重
   * w_a(k) = |{sa}| / |{s}|
   *
   * @param sa 种子关键词和中介关键词共现次数
   * @param s  种子关键词出现次数
   * @return 中介关键词权重
   */
  private static double agencyWordWeight(int sa, int s) {
    return (double) sa / s;
  }

  /**
   * 分词
   *
   * @param line
   * @return
   */
  private static List<String> segWords(String line) {
    List<SegToken> segTokens = SEGMENTER.process(line, JiebaSegmenter.SegMode.SEARCH);
    return segTokens.stream()
      .map(segToken -> segToken.word)
      // 排除空白字符
      .filter(word -> !word.trim().isEmpty())
      .collect(Collectors.toList());
  }

  // 筛选出不含种子关键词，但含有其中介关键词的搜索数据
  private static List<String> dataWithAgencyWord(String seedWord, String agencyWord) {
    List<String> result = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains(agencyWord) && !line.contains(seedWord)) {
          result.add(line);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return result;
  }

  public static void main(String[] args) {
    // 设置计时器
    long startTime = System.currentTimeMillis();
    List<CompKeyResult> zhuXianCompKeys = compute("穿越");
    long endTime = System.currentTimeMillis();
    for (CompKeyResult zhuXianCompKey : zhuXianCompKeys) {
      System.out.println(zhuXianCompKey.getCompWord() + ": " + zhuXianCompKey.getCompScore());
    }
    // 运行时间（分和秒）
    long runTime = (endTime - startTime) / 1000;
    System.out.println("运行时间：" + runTime / 60 + "分" + runTime % 60 + "秒");
  }
}
