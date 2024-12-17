package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import team.moyu.fishfind.dto.CompKeyRespDTO;
import team.moyu.fishfind.service.CompKeyService;
import org.elasticsearch.client.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompKeyServiceESImpl implements CompKeyService {

  private final RestHighLevelClient client;

  private static final Set<String> stopWords;

  static {
    System.out.println("加载停用词...");
    List<String> stopWordList;
    try {
      stopWordList = FileUtils.readLines(new File("data/merge_stopwords.txt"), "utf8");
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    stopWords = new HashSet<>(stopWordList);
    System.out.println("停用词加载完成");
  }

  public CompKeyServiceESImpl(RestHighLevelClient client) {
    this.client = client;
  }

  @Override
  public Future<List<CompKeyRespDTO>> getCompKeys(String seedWord) {
    return getAgencyWords(seedWord)
      .compose(agencyWords -> getCompetitorKeywords(seedWord, agencyWords));
  }

  private Future<List<String>> getAgencyWords(String seedWord) {
    Promise<List<String>> promise = Promise.promise();

    // 创建搜索请求
    SearchRequest searchRequest = new SearchRequest("compkey");

    // 构建查询
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.size(0); // 不返回文档，仅返回聚合结果
    sourceBuilder.query(QueryBuilders.matchQuery("query_content", seedWord));
    sourceBuilder.aggregation(
      AggregationBuilders.terms("related_terms")
        .field("query_content") // 使用精确匹配字段
        .size(30) // 限定返回的聚合结果数
        .includeExclude(new IncludeExclude(".*", seedWord + "|[\u4e00-\u9fa5]|[0-9]|[a-z]|^.$"))
    );

    // 将查询添加到请求中
    searchRequest.source(sourceBuilder);

    client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
      @Override
      public void onResponse(SearchResponse response) {
        List<String> agencyWords = new ArrayList<>();

        // 处理聚合结果
        Terms terms = response.getAggregations().get("related_terms");
        for (Terms.Bucket bucket : terms.getBuckets()) {
          if (stopWords.contains(bucket.getKeyAsString())) {
            continue;
          }
          agencyWords.add(bucket.getKeyAsString());
        }

        System.out.println("种子关键词: " + seedWord + ", 中介关键词: " + agencyWords.size() + agencyWords);
        promise.complete(agencyWords);
      }

      @Override
      public void onFailure(Exception e) {
        // 处理失败
        e.printStackTrace();
        promise.fail(e);
      }
    });

    return promise.future();
  }

  private Future<List<CompKeyRespDTO>> getCompetitorKeywords(String seedWord, List<String> agencyWords) {
    Promise<List<CompKeyRespDTO>> promise = Promise.promise();

    // 创建搜索请求
    SearchRequest searchRequest = new SearchRequest("compkey");

    // 构建查询条件（Bool 查询）
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termsQuery("query_content", agencyWords)) // 包含的关键字
      .mustNot(QueryBuilders.matchPhraseQuery("query_content.keyword", seedWord)); // 排除的短语

    // 构建聚合
    String excludeRegex = seedWord + "|[\u4e00-\u9fa5]|[0-9]|[a-z]|^.$";
    for (String agencyWord : agencyWords) {
      excludeRegex += "|" + agencyWord;
    }

    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.size(0); // 不返回文档，只返回聚合结果
    sourceBuilder.query(boolQuery);
    sourceBuilder.aggregation(
      AggregationBuilders.terms("competitor_keywords")
        .field("query_content") // 精确聚合关键词
        .size(20) // 限定返回的关键词数量
        .includeExclude(new IncludeExclude(".*", excludeRegex)) // 排除指定词和中文
    );
    searchRequest.source(sourceBuilder);

    // 异步执行请求
    client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
      @Override
      public void onResponse(SearchResponse response) {
        List<CompKeyRespDTO> compKeys = new ArrayList<>();
        // 处理响应结果
        Terms terms = response.getAggregations().get("competitor_keywords");
        for (Terms.Bucket bucket : terms.getBuckets()) {
          if (stopWords.contains(bucket.getKeyAsString())) {
            continue;
          }
          CompKeyRespDTO compKey = new CompKeyRespDTO();
          compKey.setCompWord(bucket.getKeyAsString());
          compKeys.add(compKey);
        }
        promise.complete(compKeys);
      }

      @Override
      public void onFailure(Exception e) {
        // 异常处理
        e.printStackTrace();
        promise.fail(e);
      }
    });

    return promise.future();
  }
}
