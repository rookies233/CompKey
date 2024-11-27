package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.templates.SqlTemplate;
import team.moyu.fishfind.entity.UsedSeedWord;
import team.moyu.fishfind.service.UsedSeedWordService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    Date currentDate = new Date();
    // 定义日期格式
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String formattedDate = dateFormat.format(currentDate);
    usedSeedWord.setTime(formattedDate);
    Map<String, Object> parameters = Map.of(
      "seedWordId", usedSeedWord.getSeedWordId(),
      "userId", usedSeedWord.getUserId(),
      "time", usedSeedWord.getTime()
    );

    return SqlTemplate.forQuery(client, insertQuery)
      .mapTo(UsedSeedWord.class)
      .execute(parameters)
      .compose(result -> {
        if (result.rowCount() > 0) {
          RowIterator<UsedSeedWord> iterator = result.iterator();
          if (iterator.hasNext()) {
            UsedSeedWord newUsedSeedWord = iterator.next();
            return Future.succeededFuture(newUsedSeedWord);
          }
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
    String selectQuery = "SELECT * FROM used_seedword WHERE user_id = #{userId}";
    Map<String, Object> parameters = Map.of("user_id", userId);

    return SqlTemplate.forQuery(client, selectQuery)
      .mapTo(UsedSeedWord.class)
      .execute(parameters)
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
