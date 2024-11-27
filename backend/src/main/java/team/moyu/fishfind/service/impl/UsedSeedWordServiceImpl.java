package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.templates.SqlTemplate;
import team.moyu.fishfind.entity.UsedSeedWord;
import team.moyu.fishfind.entity.UsedSeedWordRowMapper;
import team.moyu.fishfind.service.UsedSeedWordService;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author moyu
 */
public class UsedSeedWordServiceImpl implements UsedSeedWordService {

  private final Pool client;

  public UsedSeedWordServiceImpl(Pool client) {
    this.client = client;
  }

  @Override
  public Future<UsedSeedWord> addUsedSeedWord(UsedSeedWord usedSeedWord) {
    String insertQuery = "INSERT INTO used_seedword (seedword_id, user_id, time) VALUES (#{seedWordId}, #{userId}, #{time})";
    usedSeedWord.setTime(LocalDateTime.now());
    Map<String, Object> parameters = Map.of(
      "seedWordId", usedSeedWord.getSeedWordId(),
      "userId", usedSeedWord.getUserId(),
      "time", usedSeedWord.getTime()
    );

    return SqlTemplate.forUpdate(client, insertQuery)
      .mapTo(UsedSeedWord.class)
      .execute(parameters)
      .compose(result -> {
        if (result.rowCount() > 0) {
          return client
            .preparedQuery("SELECT LAST_INSERT_ID() AS id")
            .execute()
            .compose(resultId -> {
              long generatedId = resultId.iterator().next().getLong("id");
              usedSeedWord.setId(generatedId);
              return Future.succeededFuture(usedSeedWord);
            });
        }
        return Future.failedFuture("Failed to add UsedSeedWord");
      });
  }

  @Override
  public Future<String> deleteUsedSeedWord(Long id) {
    String deleteQuery = "DELETE FROM used_seedword WHERE id = #{id}";
    Map<String, Object> parameters = Map.of("id", id);

    return SqlTemplate.forUpdate(client, deleteQuery)
      .execute(parameters)
      .compose(deleteResult -> {
        if (deleteResult.rowCount() > 0) {
          return Future.succeededFuture("UsedSeedWord has been deleted");
        } else {
          return Future.failedFuture("UsedSeedWord not found");
        }
      });
  }

  @Override
  public Future<List<UsedSeedWord>> getUsedSeedWord(Long userId) {

    String query = "SELECT * FROM used_seedword WHERE user_id = #{userId}";

    return SqlTemplate
      .forQuery(client, query)
      .mapTo(UsedSeedWordRowMapper.INSTANCE)
      .execute(Collections.singletonMap("userId", userId))
      .compose(results -> {
        if (results.size() == 0) {
          return Future.failedFuture("User not found");
        }

        List<UsedSeedWord> usedSeedWords = new ArrayList<>();
        results.forEach(usedSeedWords::add); // 将结果添加到列表中
        return Future.succeededFuture(usedSeedWords);
      });
  }

}
