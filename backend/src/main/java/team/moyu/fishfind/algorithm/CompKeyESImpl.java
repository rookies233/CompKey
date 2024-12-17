package team.moyu.fishfind.algorithm;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CompKeyESImpl {

  private static final RequestOptions COMMON_OPTIONS;

  static {
    RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

    // 默认缓存限制为100MB，此处修改为30MB。
    builder.setHttpAsyncResponseConsumerFactory(
      new HttpAsyncResponseConsumerFactory
        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024));
    COMMON_OPTIONS = builder.build();
  }

  public static void main(String[] args) {
    String seedword = "修仙";
    // 阿里云Elasticsearch集群需要basic auth验证。
//    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    //访问用户名和密码为您创建阿里云Elasticsearch实例时设置的用户名和密码，也是Kibana控制台的登录用户名和密码。
//    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("{访问用户名}", "{访问密码}"));

    // 通过builder创建rest client，配置http client的HttpClientConfigCallback。
    // 单击所创建的Elasticsearch实例ID，在基本信息页面获取公网地址，即为ES集群地址。
    RestClientBuilder builder = RestClient.builder(new HttpHost("119.29.194.130", 9200, "http"));

    // RestHighLevelClient实例通过REST high-level client builder进行构造。
    RestHighLevelClient client = new RestHighLevelClient(builder);

    try {
      // 创建搜索请求
      SearchRequest searchRequest = new SearchRequest("compkey");

      // 构建查询
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
      sourceBuilder.size(0); // 不返回文档，仅返回聚合结果
      sourceBuilder.query(QueryBuilders.matchQuery("query_content", seedword));
      sourceBuilder.aggregation(
        AggregationBuilders.terms("related_terms")
          .field("query_content") // 使用精确匹配字段
          .size(10) // 限定返回的聚合结果数
      );

      // 将查询添加到请求中
      searchRequest.source(sourceBuilder);

      // 执行请求
      SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

      // 处理聚合结果
      Terms terms = response.getAggregations().get("related_terms");
      System.out.println("Top related terms for keyword '" + seedword + "':");
      for (Terms.Bucket bucket : terms.getBuckets()) {
        System.out.println(bucket.getKeyAsString() + " (count: " + bucket.getDocCount() + ")");
      }

      client.close();
    } catch (IOException ioException) {
      // 异常处理。
    }
  }
}
