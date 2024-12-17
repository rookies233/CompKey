package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.SqlTemplate;
import team.moyu.fishfind.entity.Comment;
import team.moyu.fishfind.entity.User;
import team.moyu.fishfind.service.CommentService;
import team.moyu.fishfind.vo.CommentVo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author moyu
 */
public class CommentServiceImpl implements CommentService {

  private final Pool client;

  public CommentServiceImpl(Pool client) {
    this.client = client;
  }

  // 添加评论
  @Override
  public Future<Comment> addComment(Comment comment) {
    String insertQuery = "INSERT INTO comments (user_id, compword_id, grade, comment_text) VALUES (#{userId}, #{compwordId},#{grade}, #{text})";
    Map<String, Object> insertParameters = Map.of(
      "userId", comment.getUserId(),
      "compwordId", comment.getCompwordId(),
      "grade", comment.getGrade(),
      "text", comment.getText()
    );

    return SqlTemplate
      .forUpdate(client, insertQuery)
      .execute(insertParameters)
      .compose(updateResult -> {
        // 如果插入成功，返回新用户对象
        if (updateResult.rowCount() > 0) {
          return client
            .preparedQuery("SELECT LAST_INSERT_ID() AS id")
            .execute()
            .compose(result -> {
              long generatedId = result.iterator().next().getLong("id");
              comment.setId(generatedId);
              return Future.succeededFuture(comment);

            });
        } else {
          return Future.failedFuture("Failed to insert comment");
        }
      });
  }

  // 删除评论
  @Override
  public Future<String> deleteComment(Long commentId) {
    // 删除评论的查询
    String deleteQuery = "DELETE FROM comments WHERE id = #{id}";
    Map<String, Object> deleteParameters = Map.of("id", commentId);

    return SqlTemplate
      .forUpdate(client, deleteQuery)
      .execute(deleteParameters)
      .compose(deleteResult -> {
        if (deleteResult.rowCount() > 0) {
          return Future.succeededFuture("Comment has been deleted");
        } else {
          return Future.failedFuture("Comment not found");
        }
      });
  }

  // 根据竞争关键词id查询所有评论
  @Override
  public Future<List<CommentVo>> getAllCommentsById(Long compwordId) {
    // 检查参数是否为空或无效
    if (compwordId == null || compwordId <= 0) {
      return Future.failedFuture("Invalid ID");
    }

    // 获取评论的查询语句
    String query = "SELECT * FROM comments WHERE compword_id = #{compwordId}";
    Map<String, Object> parameters = Map.of("compwordId", compwordId);

    return SqlTemplate
      .forQuery(client, query)
      .execute(parameters)
      .compose(commentResults -> {

        if (commentResults.size() == 0) {
          return Future.succeededFuture(null);
        }

        // 提取每列数据
        List<Long> ids = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();
        List<Integer> grades = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        commentResults.forEach(row -> {
          ids.add(row.getLong("id"));
          userIds.add(row.getLong("user_id"));
          grades.add(row.getInteger("grade"));
          texts.add(row.getString("comment_text"));
        });

        // 生成动态的 IN 子句
        String idsPlaceholder = userIds.stream()
          .map(String::valueOf)
          .collect(Collectors.joining(", "));

        // 查询user表
        String queryComment = "SELECT * FROM user WHERE id IN (" + idsPlaceholder + ")";
        return SqlTemplate
          .forQuery(client, queryComment)
          .execute(Collections.emptyMap())
          .compose(rows -> {
            // 映射到 commentVo
            List<CommentVo> commentVo = new ArrayList<>();
            Iterator<Row> iterator = rows.iterator();
            int index = 0;
            while (iterator.hasNext()) {
              Row row = iterator.next();
              Long id = ids.get(index);
              String username = row.getString("username");
              int grade = grades.get(index);
              String text = texts.get(index);

              CommentVo vo = new CommentVo();
              vo.setId(id);
              vo.setUsername(username);
              vo.setGrade(grade);
              vo.setText(text);
              commentVo.add(vo);
              index++;
            }
            return Future.succeededFuture(commentVo);
          });
      });
  }

  @Override
  public Future<List<CommentVo>> getAllComments() {
    String query = "SELECT * FROM comments";

    return SqlTemplate.forQuery(client, query)
      .execute(Collections.emptyMap())
      .compose(commentResults -> {

        if (commentResults.size() == 0) {
          return Future.succeededFuture(new ArrayList<>());
        }

        // 提取每列数据
        List<Long> ids = new ArrayList<>();
        List<Long> compwordIds = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();
        List<Integer> grades = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        for (Row row : commentResults) { // 使用增强for循环遍历 RowSet
          ids.add(row.getLong("id"));
          compwordIds.add(row.getLong("compword_id"));
          userIds.add(row.getLong("user_id"));
          grades.add(row.getInteger("grade"));
          texts.add(row.getString("comment_text"));
        }

        // 构建动态 IN 子句
        String userIdsPlaceholder = userIds.stream()
          .map(String::valueOf)
          .collect(Collectors.joining(", "));

        // 查询 user 表
        String userQuery = "SELECT id, username FROM user WHERE id IN (" + userIdsPlaceholder + ")";

        return SqlTemplate.forQuery(client, userQuery)
          .execute(Collections.emptyMap())
          .compose(userResult -> {
            // 创建用户映射
            Map<Long, String> userMap = new HashMap<>();
            for (Row row : userResult) { // 使用增强for循环遍历 RowSet
              Long id = row.getLong("id");
              String username = row.getString("username");
              userMap.put(id, username);
            }

            // 构建 compwordId 的 IN 子句
            String compwordIdsPlaceholder = compwordIds.stream()
              .map(String::valueOf)
              .collect(Collectors.joining(", "));

            // 查询 compword 表
            String wordQuery = "SELECT id, word FROM compword WHERE id IN (" + compwordIdsPlaceholder + ")";

            return SqlTemplate.forQuery(client, wordQuery)
              .execute(Collections.emptyMap())
              .compose(wordResult -> {
                // 创建单词映射
                Map<Long, String> wordMap = new HashMap<>();
                for (Row row : wordResult) { // 使用增强for循环遍历 RowSet
                  Long id = row.getLong("id");
                  String word = row.getString("word");
                  wordMap.put(id, word);
                }

                // 映射到 CommentVo
                List<CommentVo> commentVos = IntStream.range(0, ids.size())
                  .mapToObj(i -> {
                    Long id = ids.get(i);
                    Long compwordId = compwordIds.get(i);
                    String username = userMap.get(userIds.get(i));
                    int grade = grades.get(i);
                    String text = texts.get(i);
                    String word = wordMap.get(compwordId); // 根据 compwordId 获取对应的 word

                    CommentVo vo = new CommentVo();
                    vo.setId(id);
                    vo.setCompwordId(compwordId);
                    vo.setUsername(username);
                    vo.setGrade(grade);
                    vo.setText(text);
                    vo.setWord(word); // 设置 word 属性
                    return vo;
                  }).collect(Collectors.toList());

                return Future.succeededFuture(commentVos);
              });
          });
      });
  }
}
