package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import team.moyu.fishfind.entity.SeedWord;
import team.moyu.fishfind.service.SeedWordService;

import java.util.Collections;

public class SeedWordServiceImpl implements SeedWordService {

  private final Pool client;

  public SeedWordServiceImpl(Pool client) {
    this.client = client;
  }

  // 插入种子词，如果不存在则插入，存在则返回，返回结果包含id
  @Override
  public Future<SeedWord> addSeedWord(String seedWord) {
    return SqlTemplate
      .forUpdate(client, "INSERT INTO seedword (word) VALUES (#{word}) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)")
      .execute(Collections.singletonMap("word", seedWord))
      .compose(result -> {
        if (result.rowCount() > 0) {
          return client
            .preparedQuery("SELECT LAST_INSERT_ID() AS id")
            .execute()
            .compose(resultId -> {
              long generatedId = resultId.iterator().next().getLong("id");
              SeedWord seedWord1 = new SeedWord();
              seedWord1.setId(generatedId);
              seedWord1.setWord(seedWord);
              return Future.succeededFuture(seedWord1);
            });
        }
        return Future.failedFuture("Failed to add SeedWord");
      });
  }
}
