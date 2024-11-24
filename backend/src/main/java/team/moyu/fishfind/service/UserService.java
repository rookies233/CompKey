package team.moyu.fishfind.service;

import io.vertx.core.Future;
import team.moyu.fishfind.entity.User;

/**
 * @author moyu
 */
public interface UserService {
  Future<User> login(String username, String password);
}
