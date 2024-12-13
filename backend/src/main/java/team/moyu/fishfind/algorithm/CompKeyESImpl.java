package team.moyu.fishfind.algorithm;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

//@Log4j2
@Slf4j
public class CompKeyESImpl {


  public static void main(String[] args) throws IOException {
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
      new UsernamePasswordCredentials("elastic", "fM_kW604xfv5YthRQoZH"));

    RestClient client = RestClient.builder(
        new HttpHost("localhost", 9200, "http"))
      .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
      .build();

    SearchRequest searchRequest = new SearchRequest("compkey");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // 查询前10的中介关键词
    searchSourceBuilder.aggregation(AggregationBuilders.terms("compWords").field("compWord").size(10));
  }
}
