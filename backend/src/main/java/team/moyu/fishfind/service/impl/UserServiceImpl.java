package team.moyu.fishfind.service.impl;

import io.vertx.core.Future;
import io.vertx.mysqlclient.impl.MySQLPoolImpl;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.templates.SqlTemplate;
import team.moyu.fishfind.entity.User;
import team.moyu.fishfind.service.UserService;

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
  public Future<User> login(String username, String password) {
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
}
