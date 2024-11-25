package team.moyu.fishfind.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class CompKeyAlgorithm {

    /**
    * compKey算法实现函数
    * @param seedWord 种子关键词
    */
    public static void compKey(String seedWord) {
      String filePath = "data/cleaned.train";
      int seedLogCount = 0;   // 统计包含种子关键词的搜索记录数
      JiebaSegmenter segment = new JiebaSegmenter();   // 初始化jieba分词器
      Map<String, Integer> wordFrequency = new HashMap<>(); // 定义词频统计Map

      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        File file1 = new File("data/stop_words/merge_stopwords.txt");
        List<String> stopwords= FileUtils.readLines(file1,"utf8");  //加载停用词，存储在List集合中
        String line;

        while ((line = br.readLine()) != null) {
          // 遍历处理每一行
          if(line.contains(seedWord)) {
            seedLogCount++;
            line = line.replaceAll("\\s+", "");

            // 分词处理
            List<String> segResult = segment.sentenceProcess(line);
            // 去除结果中的种子关键词
            segResult.removeIf(word -> word.equals(seedWord));
            // 去除停用词
            segResult.removeAll(stopwords);
            // 统计词频
            for (String word : segResult) {
              wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      // 获取频率最高的前十个词，作为中介关键词
      List<Map.Entry<String, Integer>> agencyWords = wordFrequency.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(10)
        .toList();

      // 打印频率最高的前十个词
      System.out.println("频率最高的前十个词:");
      agencyWords.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

      System.out.println("seedLogCount" + seedLogCount);
    }

    public static void main(String[] args){
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
