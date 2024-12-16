package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.SqlTemplate;
import team.moyu.fishfind.entity.UsedSeedWord;
import team.moyu.fishfind.entity.UsedSeedWordRowMapper;
import team.moyu.fishfind.service.UsedSeedWordService;
import team.moyu.fishfind.vo.UsedSeedWordsVo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
  public Future<List<UsedSeedWordsVo>> getUsedSeedWord(Long userId) {

    String query = "SELECT * FROM used_seedword WHERE user_id = #{userId}";

    return SqlTemplate
      .forQuery(client, query)
      .execute(Collections.singletonMap("userId", userId))
      .compose(usedSeedWordResults -> {

        if (usedSeedWordResults.size() == 0) {
          return Future.succeededFuture(null);
        }

        // 提取 id、seedword_id 和 time
        List<Long> ids = new ArrayList<>();
        List<Long> seedWordIds = new ArrayList<>();
        List<LocalDateTime> dates = new ArrayList<>();
        usedSeedWordResults.forEach(row -> {
          ids.add(row.getLong("id"));
          seedWordIds.add(row.getLong("seedword_id"));
          dates.add(row.getLocalDateTime("time"));
        });

        // 生成动态的 IN 子句
        String idsPlaceholder = seedWordIds.stream()
          .map(String::valueOf)
          .collect(Collectors.joining(", "));

        // 查询 seedword 表
        String querySeedWord = "SELECT word FROM seedword WHERE id IN (" + idsPlaceholder + ")";
        return SqlTemplate
          .forQuery(client, querySeedWord)
          .execute(Collections.emptyMap())
          .compose(seedWordResults -> {
            // 映射到 UsedSeedWordsVo
            List<UsedSeedWordsVo> usedSeedWords = new ArrayList<>();
            Iterator<Row> iterator = seedWordResults.iterator();
            int index = 0;
            while (iterator.hasNext()) {
              Row row = iterator.next();
              Long id = ids.get(index);
              String word = row.getString("word");
              LocalDateTime date = dates.get(index); // 获取对应的时间
              UsedSeedWordsVo vo = new UsedSeedWordsVo();
              vo.setId(id);
              vo.setSeedWord(word);
              vo.setTime(date);
              usedSeedWords.add(vo);
              index++;
            }
            return Future.succeededFuture(usedSeedWords);
          });
      });
  }

}
