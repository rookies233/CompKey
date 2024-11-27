package team.moyu.fishfind.algorithm;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * @author moyu
 */
public class CompKeyAlgorithm {

  private static final String FILE_PATH = "data/temp/cleaned.train";

  private static final List<String> STOP_WORDS;

  private static final JiebaSegmenter SEGMENTER = new JiebaSegmenter();

  static {
    try {
      STOP_WORDS = FileUtils.readLines(new File("data/stop_words/merge_stopwords.txt"), "utf8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * compKey算法实现函数
   *
   * @param seedKey 种子关键词
   */
  public static List<CompKeyResult> compKey(String seedKey) {
    // 统计包含种子关键词的搜索记录数
    int seedLogCount = 0;
    // 定义词频统计Map
    Map<String, Integer> wordFrequency = new HashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
      String line;
      while ((line = br.readLine()) != null) {
        // 遍历处理每一行
        if (line.contains(seedKey)) {
          seedLogCount++;
          line = line.replaceAll("\\s+", "");

          // 分词处理
          List<String> segResult = SEGMENTER.sentenceProcess(line);
          // 去除结果中的种子关键词
          segResult.removeIf(word -> word.equals(seedKey));
          // 去除停用词
          segResult.removeAll(STOP_WORDS);
          // 统计词频
          for (String word : segResult) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // 获取频率最高的前十个词，作为中介关键词
    List<Map.Entry<String, Integer>> agencyKeys = wordFrequency.entrySet().stream()
      .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
      .limit(10)
      .toList();

    // 打印频率最高的前十个词
    System.out.println("频率最高的前十个词:");
    agencyKeys.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

    System.out.println("seedLogCount" + seedLogCount);

    return List.of();
  }

  public static void main(String[] args) {
    // 设置计时器
    long startTime = System.currentTimeMillis();
    compKey("图片");
    compKey("手机");
    compKey("小说");
    compKey("视频");
    compKey("下载");
    compKey("大全");
    compKey("qq");
    compKey("电影");
    compKey("中国");
    compKey("世界");
    long endTime = System.currentTimeMillis();
    System.out.println("运行时间：" + (endTime - startTime) + "毫秒");
  }
}
