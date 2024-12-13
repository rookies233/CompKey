package team.moyu.fishfind.service;

import io.vertx.core.Future;
import team.moyu.fishfind.dto.UserLoginReqDTO;
import team.moyu.fishfind.entity.User;

/**
 * @author moyu
 */
public interface UserService {

  // 登录
  Future<User> login(UserLoginReqDTO requestParam);

  // 注册
  Future<User> register(User user);

  // 修改用户
  Future<User> updateUser(User user);

  // 删除用户
  Future<String> deleteUser(Long userId);

  //获取用户信息
  Future<User> getUserById(Long userId);

}
