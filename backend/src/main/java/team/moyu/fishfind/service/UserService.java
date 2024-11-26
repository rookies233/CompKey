package team.moyu.fishfind.service;

import io.vertx.core.Future;
import team.moyu.fishfind.entity.User;

/**
 * @author moyu
 */
public interface UserService {

  // 登录
  Future<User> login(String username, String password);

  // 注册
  Future<User> register(User user);

  // 修改用户
  Future<User> updateUser(User user);

  // 删除用户
  Future<String> deleteUser(Long userId);

}
