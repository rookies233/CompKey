package team.moyu.fishfind.service.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import team.moyu.fishfind.dto.CompKeyReqDTO;
import team.moyu.fishfind.dto.CompKeyRespDTO;
import team.moyu.fishfind.entity.CompWord;
import team.moyu.fishfind.entity.UsedSeedWord;
import team.moyu.fishfind.model.AgencyWordInfo;
import team.moyu.fishfind.model.CompKeyWordInfo;
import team.moyu.fishfind.service.CompKeyService;
import org.elasticsearch.client.*;
import team.moyu.fishfind.service.SeedWordService;
import team.moyu.fishfind.service.UsedSeedWordService;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CompKeyServiceESImpl implements CompKeyService {

  private final RestHighLevelClient client;

  private final Pool sqlClient;

  private final UsedSeedWordService usedSeedWordService;

  private final SeedWordService seedWordService;

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

  public CompKeyServiceESImpl(RestHighLevelClient client, Pool sqlClient, UsedSeedWordService usedSeedWordService, SeedWordService seedWordService) {
    this.client = client;
    this.sqlClient = sqlClient;
    this.usedSeedWordService = usedSeedWordService;
    this.seedWordService = seedWordService;
  }

  @Override
  public Future<List<CompKeyRespDTO>> getCompKeys(CompKeyReqDTO requestParam) {
    seedWordService
      .addSeedWord(requestParam.getSeedWord())
      .onSuccess(seedWord -> {
        UsedSeedWord usedSeedWord = new UsedSeedWord();
        usedSeedWord.setSeedWordId(seedWord.getId());
        usedSeedWord.setUserId(requestParam.getUserId());
        usedSeedWordService.addUsedSeedWord(usedSeedWord);
      });

//    return getAgencyWords(requestParam.getSeedWord())
//      .compose(this::getAgencyWordsWeight)
//      .compose(agencyWordInfos -> getCompetitorKeywords(requestParam.getSeedWord(), agencyWordInfos))
//      .compose(this::saveCompKey)
//      .compose(this::generateRandomCompScore);

    return getAgencyWords(requestParam.getSeedWord())
      .compose(this::getAgencyWordsWeight)
      .compose(agencyWordInfos -> getAgencyWordsCompKeys(requestParam.getSeedWord(), agencyWordInfos))
      .compose(this::calculateCompScore)
      .compose(this::saveCompKey);
  }

  private Future<List<AgencyWordInfo>> getAgencyWords(String seedWord) {
    Promise<List<AgencyWordInfo>> promise = Promise.promise();

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
        List<AgencyWordInfo> agencyWordInfos = new ArrayList<>();

        // 处理聚合结果
        Terms terms = response.getAggregations().get("related_terms");
        for (Terms.Bucket bucket : terms.getBuckets()) {
          if (stopWords.contains(bucket.getKeyAsString())) {
            continue;
          }
          AgencyWordInfo agencyWord = new AgencyWordInfo();
          agencyWord.setAgencyWord(bucket.getKeyAsString());
          agencyWord.setSa(bucket.getDocCount());
          agencyWordInfos.add(agencyWord);
        }

        List<String> agencyWords = new ArrayList<>();
        for (AgencyWordInfo agencyWordInfo : agencyWordInfos) {
          agencyWords.add(agencyWordInfo.getAgencyWord());
        }

        System.out.println("种子关键词: " + seedWord + ", 中介关键词: " + agencyWords.size() + agencyWords);
        promise.complete(agencyWordInfos);
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

  /**
   * 获取竞争关键词,并计算每个竞争性关键词的ka，通过getCoOccurrenceCount方法计算
   * 然后计算每个竞争性关键词的竞争度 = ka / (a * sa)
   *
   * @param seedWord
   * @param agencyWordInfo
   * @return
   */
  private Future<List<CompKeyWordInfo>> getCompWords(String seedWord, AgencyWordInfo agencyWordInfo) {
    Promise<List<CompKeyWordInfo>> promise = Promise.promise();

    // 创建搜索请求
    SearchRequest searchRequest = new SearchRequest("compkey");

    // 构建查询条件
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
      .mustNot(QueryBuilders.matchQuery("query_content", seedWord))
      .must(QueryBuilders.matchQuery("query_content", agencyWordInfo.getAgencyWord()));

    // 构建聚合
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.size(0); // 不返回文档，仅返回聚合结果
    sourceBuilder.query(boolQuery);
    sourceBuilder.aggregation(
      AggregationBuilders.terms("comp_words")
        .field("query_content") // 精确聚合关键词
        .size(20) // 限定返回的关键词数量
        .includeExclude(new IncludeExclude(".*", seedWord + "|[\u4e00-\u9fa5]|[0-9]|[a-z]|^.$|" + agencyWordInfo.getAgencyWord())) // 排除指定词和中文
    );
    searchRequest.source(sourceBuilder);

    // 异步执行请求
    client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
      @Override
      public void onResponse(SearchResponse response) {
        List<CompKeyWordInfo> compKeys = new ArrayList<>();
        // 处理响应结果
        Terms terms = response.getAggregations().get("comp_words");
        for (Terms.Bucket bucket : terms.getBuckets()) {
          if (stopWords.contains(bucket.getKeyAsString())) {
            continue;
          }
          CompKeyWordInfo compKey = new CompKeyWordInfo();
          compKey.setCompKeyWord(bucket.getKeyAsString());
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

  /**
   * 获取所有中介关键词的竞争性关键词
   */
  private Future<Map<AgencyWordInfo, List<CompKeyWordInfo>>> getAgencyWordsCompKeys(String seedWord, List<AgencyWordInfo> agencyWordInfos) {
    Promise<Map<AgencyWordInfo, List<CompKeyWordInfo>>> promise = Promise.promise();
    Map<AgencyWordInfo, List<CompKeyWordInfo>> agencyWordMap = new HashMap<>();
    List<Future> futures = new ArrayList<>();

    for (AgencyWordInfo agencyWordInfo : agencyWordInfos) {
      Future<List<CompKeyWordInfo>> future = getCompWords(seedWord, agencyWordInfo)
        .compose(compKeys -> calculateKa(compKeys, agencyWordInfo))
        .map(compKeys -> {
          // 计算竞争度
          for (CompKeyWordInfo compKey : compKeys) {
            Long ka = compKey.getKa();
            Long a = agencyWordInfo.getA();
            Long sa = agencyWordInfo.getSa();
            double comp = (double) ka / (a * sa);
            compKey.setCompScore(comp);
          }
          agencyWordMap.put(agencyWordInfo, compKeys);
          return compKeys;
        });
      futures.add(future);
    }

    CompositeFuture.all(futures)
      .onSuccess(compositeFuture -> {
        promise.complete(agencyWordMap);
      })
      .onFailure(err -> {
        err.printStackTrace();
        promise.fail(err);
      });

    return promise.future();
  }

  /**
   * 竞争性Comp测度计算
   * $$Comp(k,s)=\sum_{i=1}^{m}{\{w_{a_i}(k)\times Comp_{a_i}(k,s)\}}$$
   */
  private Future<List<CompKeyRespDTO>> calculateCompScore(Map<AgencyWordInfo, List<CompKeyWordInfo>> agencyWordMap) {
    // 将不同的中介关键词的相同竞争性关键词合并
    // key: 竞争性关键词，value: 竞争性关键词信息
    Map<String, List<CompKeyWordInfo>> compKeyMap = new HashMap<>();
    // key: 竞争性关键词，value: 中介关键词信息
    Map<String, List<AgencyWordInfo>> compKeyAgencyMap = new HashMap<>();
    for (Map.Entry<AgencyWordInfo, List<CompKeyWordInfo>> entry : agencyWordMap.entrySet()) {
      AgencyWordInfo agencyWordInfo = entry.getKey();
      List<CompKeyWordInfo> compKeys = entry.getValue();
      for (CompKeyWordInfo compKey : compKeys) {
        String compWord = compKey.getCompKeyWord();
        if (!compKeyMap.containsKey(compWord)) {
          compKeyMap.put(compWord, new ArrayList<>());
        }
        compKeyMap.get(compWord).add(compKey);

        if (!compKeyAgencyMap.containsKey(compWord)) {
          compKeyAgencyMap.put(compWord, new ArrayList<>());
        }
        compKeyAgencyMap.get(compWord).add(agencyWordInfo);
      }
    }

    // 计算竞争性关键词的竞争度
    List<CompKeyRespDTO> compKeys = new ArrayList<>();
    for (Map.Entry<String, List<CompKeyWordInfo>> entry : compKeyMap.entrySet()) {
      String compWord = entry.getKey();
      List<CompKeyWordInfo> compKeyInfos = entry.getValue();
      List<AgencyWordInfo> agencyWordInfos = compKeyAgencyMap.get(compWord);
      double compScore = 0;
      for (int i = 0; i < compKeyInfos.size(); i++) {
        CompKeyWordInfo compKey = compKeyInfos.get(i);
        AgencyWordInfo agencyWordInfo = agencyWordInfos.get(i);
        compScore += agencyWordInfo.getWeight() * compKey.getCompScore();
      }
      CompKeyRespDTO compKeyResp = new CompKeyRespDTO();
      compKeyResp.setCompWord(compWord);
      compKeyResp.setCompScore(compScore);
      compKeys.add(compKeyResp);
    }

    // 按竞争度降序排序
    compKeys.sort(Comparator.comparing(CompKeyRespDTO::getCompScore).reversed());

    // 取前20个
    if (compKeys.size() > 20) {
      compKeys = compKeys.subList(0, 20);
    }

    // 乘1000
    for (CompKeyRespDTO compKey : compKeys) {
      compKey.setCompScore(compKey.getCompScore() * 1000);
    }

    // 保留三位有效数字
    for (CompKeyRespDTO compKey : compKeys) {
      compKey.setCompScore((double) Math.round(compKey.getCompScore() * 1000) / 1000);
    }

    return Future.succeededFuture(compKeys);
  }

  /**
   * 计算每个竞争性关键词的ka
   */
  private Future<List<CompKeyWordInfo>> calculateKa(List<CompKeyWordInfo> compKeys, AgencyWordInfo agencyWordInfos) {
    List<Future> futures = new ArrayList<>();
    for (CompKeyWordInfo compKey : compKeys) {
      Future<Long> future = getCoOccurrenceCount(compKey.getCompKeyWord(), agencyWordInfos.getAgencyWord())
        .compose(coOccurrenceCount -> {
          compKey.setKa(coOccurrenceCount);
          return Future.succeededFuture();
        });
      futures.add(future);
    }

    return CompositeFuture.all(futures)
      .map(compositeFuture -> compKeys);
  }

  /**
   * 获取竞争关键词 k 与中介关键词 a 的共现次数
   */
  private Future<Long> getCoOccurrenceCount(String word1, String word2) {
    Promise<Long> promise = Promise.promise();
    SearchRequest searchRequest = new SearchRequest("compkey");
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.size(0);
    sourceBuilder.query(QueryBuilders.boolQuery()
      .must(QueryBuilders.matchQuery("query_content", word1))
      .must(QueryBuilders.matchQuery("query_content", word2))
    );
    sourceBuilder.aggregation(
      AggregationBuilders.count("co_occurrence_count").field("query_content.keyword")
    );
    searchRequest.source(sourceBuilder);

    client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
      @Override
      public void onResponse(SearchResponse response) {
        ValueCount valueCount = response.getAggregations().get("co_occurrence_count");
        long count = valueCount.getValue();
        promise.complete(count);
      }

      @Override
      public void onFailure(Exception e) {
        e.printStackTrace();
        promise.fail(e);
      }
    });

    return promise.future();
  }

  /**
   * 按List的顺序，从高到低，有下降趋势的生成随机的竞争性关键词分数，小数，不用存数据库
   *
   * @param compKeys
   * @return
   */
  private Future<List<CompKeyRespDTO>> generateRandomCompScore(List<CompKeyRespDTO> compKeys) {
    Promise<List<CompKeyRespDTO>> promise = Promise.promise();
    List<Future> futures = new ArrayList<>();

    for (int i = 0; i < compKeys.size(); i++) {
      CompKeyRespDTO compKey = compKeys.get(i);
      double score = 0.7 + Math.random() * 0.2 - 0.03 * i;
      compKey.setCompScore(score);
      compKeys.set(i, compKey);
    }

    promise.complete(compKeys);
    return promise.future();
  }

  private Future<List<CompKeyRespDTO>> saveCompKey(List<CompKeyRespDTO> compKeys) {
    Promise<List<CompKeyRespDTO>> promise = Promise.promise();
    List<Future> futures = new ArrayList<>();

    for (CompKeyRespDTO compKey : compKeys) {
      Future<Long> future = Future.future(promise1 -> {
        // 先查询数据库，检查关键词是否已经存在
        SqlTemplate.forQuery(sqlClient, "SELECT * FROM compword WHERE word=#{word}")
          .mapTo(CompWord.class)
          .execute(Collections.singletonMap("word", compKey.getCompWord()))
          .onSuccess(rows -> {
            if (rows.size() > 0) {
              // 关键词已经存在，获取其ID并跳过插入
              Long existingId = rows.iterator().next().getId();
              compKey.setCompWordId(existingId);
              promise1.complete(existingId);
            } else {
              // 关键词不存在，执行插入操作
              SqlTemplate.forUpdate(sqlClient, "INSERT INTO compword (word) VALUES (#{word})")
                .execute(Map.of("word", compKey.getCompWord()))
                .onSuccess(voidSqlResult -> {
                  SqlTemplate.forQuery(sqlClient, "SELECT LAST_INSERT_ID() AS id")
                    .mapTo(CompWord.class)
                    .execute(Map.of())
                    .onSuccess(rowSet -> {
                      Long id = rowSet.iterator().next().getId();
                      compKey.setCompWordId(id);
                      promise1.complete(id);
                    })
                    .onFailure(err -> {
                      err.printStackTrace();
                      promise1.fail(err);
                    });
                })
                .onFailure(err -> {
                  err.printStackTrace();
                  promise1.fail(err);
                });
            }
          })
          .onFailure(err -> {
            err.printStackTrace();
            promise1.fail(err);
          });
      });
      futures.add(future);
    }

    CompositeFuture.all(futures)
      .onSuccess(compositeFuture -> {
        promise.complete(compKeys);
      })
      .onFailure(err -> {
        err.printStackTrace();
        promise.fail(err);
      });

    return promise.future();
  }

  private Future<List<CompKeyRespDTO>> getCompetitorKeywords(String seedWord, List<AgencyWordInfo> agencyWordInfos) {
    Promise<List<CompKeyRespDTO>> promise = Promise.promise();

    // 创建搜索请求
    SearchRequest searchRequest = new SearchRequest("compkey");

    List<String> agencyWords = new ArrayList<>();
    for (AgencyWordInfo agencyWordInfo : agencyWordInfos) {
      agencyWords.add(agencyWordInfo.getAgencyWord());
    }

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

  private Future<List<AgencyWordInfo>> getAgencyWordsWeight(List<AgencyWordInfo> agencyWordInfos) {
    // 并行调用 countWord 方法, 计算每个中介关键词的权重
    List<Future> futures = new ArrayList<>();
    for (AgencyWordInfo agencyWordInfo : agencyWordInfos) {
      Future<Long> future = countWord(agencyWordInfo.getAgencyWord());
      future.onSuccess(count -> {
        agencyWordInfo.setA(count);
        agencyWordInfo.setWeight((double) agencyWordInfo.getSa() / agencyWordInfo.getA());
      });
      futures.add(future);
    }

    return CompositeFuture.all(futures)
      .map(compositeFuture -> agencyWordInfos);
  }

  /**
   * 计算关键词出现次数
   *
   * @param word
   * @return
   */
  private Future<Long> countWord(String word) {
    Promise<Long> promise = Promise.promise();
    SearchRequest searchRequest = new SearchRequest("compkey");
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.size(0);
    sourceBuilder.query(QueryBuilders.matchQuery("query_content", word));
    sourceBuilder.aggregation(
      AggregationBuilders.count("total_occurrence_count").field("query_content.keyword")
    );
    searchRequest.source(sourceBuilder);

    // 异步执行请求
    client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
      @Override
      public void onResponse(SearchResponse response) {
        // 处理聚合结果
        ValueCount valueCount = response.getAggregations().get("total_occurrence_count");
        long totalCount = valueCount.getValue();
        promise.complete(totalCount);
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
}
