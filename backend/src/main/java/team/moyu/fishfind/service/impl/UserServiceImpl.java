package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.templates.SqlTemplate;
import team.moyu.fishfind.dto.UserLoginReqDTO;
import team.moyu.fishfind.entity.User;
import team.moyu.fishfind.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author moyu
 */
public class UserServiceImpl implements UserService {

  private final Pool client;

  public UserServiceImpl(Pool client) {
    this.client = client;
  }

  @Override
  public Future<User> login(UserLoginReqDTO requestParam) {
    String username = requestParam.getUsername();
    String password = requestParam.getPassword();
    if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
      return Future.failedFuture("Username or password is null");
    }
    String query = "SELECT * FROM user WHERE username = #{username}";
    Map<String, Object> parameters = Map.of("username", username);
    return SqlTemplate
      .forQuery(client, query)
      .mapTo(User.class)
      .execute(parameters)
      .compose(users -> {
        if (users.size() == 0) {
          return Future.failedFuture("User not found");
        }
        RowIterator<User> iterator = users.iterator();
        User user = iterator.next();
        if (!user.getPassword().equals(password)) {
          return Future.failedFuture("Password incorrect");
        }
        return Future.succeededFuture(user);
      });
  }

  @Override
  public Future<User> register(User user) {
    String username = user.getUsername();
    // 首先检查用户名是否已经存在
    String checkQuery = "SELECT * FROM user WHERE username = #{username}";
    Map<String, Object> checkParameters = Map.of("username", username);

    return SqlTemplate
      .forQuery(client, checkQuery)
      .mapTo(User.class)
      .execute(checkParameters)
      .compose(existingUsers -> {
        if (existingUsers.size() > 0) {
          return Future.failedFuture("Username already exists");
        }

        // 如果用户名不存在，插入新用户
        String insertQuery = "INSERT INTO user (username, password, telephone, email) VALUES (#{username}, #{password},#{telephone}, #{email})";
        Map<String, Object> insertParameters = Map.of(
          "username", user.getUsername(),
          "password", user.getPassword(),
          "telephone", user.getTelephone(),
          "email", user.getEmail()
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
                  user.setId(generatedId);
                  return Future.succeededFuture(user);

                });
            } else {
              return Future.failedFuture("Failed to register user");
            }
          });
      });
  }

  @Override
  public Future<User> updateUser(User user) {
    String updateQuery = "UPDATE user SET username = #{username}, password = #{password}, telephone = #{telephone}, email = #{email} WHERE id = #{userId}";
    Map<String, Object> updateParameters = Map.of(
      "username", user.getUsername(),
      "password", user.getPassword(),
      "telephone", user.getTelephone(),
      "email", user.getEmail(),
      "userId", user.getId()
    );

    return SqlTemplate
      .forUpdate(client, updateQuery)
      .execute(updateParameters)
      .compose(updateResult -> {
        if (updateResult.rowCount() > 0) {
          return Future.succeededFuture(user); // 返回更新后的用户对象
        } else {
          return Future.failedFuture("User not found or no changes made");
        }
      });
  }

  @Override
  public Future<String> deleteUser(Long userId) {
    // 删除用户的查询
    String deleteQuery = "DELETE FROM user WHERE id = #{userId}";
    Map<String, Object> deleteParameters = Map.of("userId", userId);

    return SqlTemplate
      .forUpdate(client, deleteQuery)
      .execute(deleteParameters)
      .compose(deleteResult -> {
        if (deleteResult.rowCount() > 0) {
          return Future.succeededFuture("User has been deleted");
        } else {
          return Future.failedFuture("User not found");
        }
      });
  }

  @Override
  public Future<User> getUserById(Long userId) {
    // 检查参数是否为空或无效
    if (userId == null || userId <= 0) {
      return Future.failedFuture("Invalid user ID");
    }

    // 获取用户的查询语句
    String query = "SELECT * FROM user WHERE id = #{userId}";
    Map<String, Object> parameters = Map.of("userId", userId);

    return SqlTemplate
      .forQuery(client, query)
      .mapTo(User.class)
      .execute(parameters)
      .compose(users -> {
        if (users.size() == 0) {
          return Future.failedFuture("User not found");
        }
        User user = users.iterator().next();
        return Future.succeededFuture(user);
      });
  }

  @Override
  public Future<List<User>> getAllUsers() {
    // 获取用户列表的查询语句
    String query = "SELECT * FROM user";

    return SqlTemplate.forQuery(client, query)
      .mapTo(User.class)
      .execute(Map.of())
      .map(users -> {
        List<User> userList = new ArrayList<>();
        for (User user : users) {
          userList.add(user);
        }
        return userList;
      });
  }
}
