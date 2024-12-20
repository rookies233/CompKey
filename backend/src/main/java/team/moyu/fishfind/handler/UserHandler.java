package team.moyu.fishfind.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import team.moyu.fishfind.common.CommonResponse;
import team.moyu.fishfind.common.ResultUtils;
import team.moyu.fishfind.dto.UserLoginReqDTO;
import team.moyu.fishfind.entity.User;
import team.moyu.fishfind.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * @author moyu
 */
public class UserHandler {

  private final UserService userService;

  private final ObjectMapper mapper;

  public UserHandler(UserService userService, ObjectMapper mapper) {
    this.userService = userService;
    this.mapper = mapper;
  }

  // 登录
  public void login(RoutingContext context) {
    userService.login(context.body().asPojo(UserLoginReqDTO.class))
      .onSuccess(user -> {
        CommonResponse<User> response = ResultUtils.success(user);
        try {
          context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }).onFailure(err -> {
        context.response().setStatusCode(401).putHeader("content-type", "application/json").end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 注册
  public void register(RoutingContext context) {
    JsonObject body = context.getBodyAsJson();
    String username = body.getString("username");
    String password = body.getString("password");
    String telephone = body.getString("telephone");
    String email = body.getString("email");

    User newUser = new User();
    newUser.setUsername(username);
    newUser.setPassword(password);
    newUser.setTelephone(telephone);
    newUser.setEmail(email);

    userService.register(newUser)
      .onSuccess(user -> {
        CommonResponse<User> response = ResultUtils.success(user);
        try {
          context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }).onFailure(err -> {
        context.response().setStatusCode(401).putHeader("content-type", "application/json").end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 更新用户
  public void updateUser(RoutingContext context) {
    JsonObject body = context.getBodyAsJson();
    // 从请求路径中获取用户 ID
    Long userId = Long.valueOf(context.request().getParam("id"));
    String username = body.getString("username");
    String password = body.getString("password");
    String telephone = body.getString("telephone");
    String email = body.getString("email");

    User newUser = new User();
    newUser.setId(userId);
    newUser.setUsername(username);
    newUser.setPassword(password);
    newUser.setTelephone(telephone);
    newUser.setEmail(email);

    userService.updateUser(newUser)
      .onSuccess(user -> {
        CommonResponse<User> response = ResultUtils.success(user);
        try {
          context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }).onFailure(err -> {
        context.response().setStatusCode(401).putHeader("content-type", "application/json").end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 删除用户
  public void deleteUser(RoutingContext context) {
    // 从请求路径中获取用户 ID
    String userIdParam = context.request().getParam("id");
    if (userIdParam == null) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "User ID is required").toString());
      return;
    }

    Long userId;
    try {
      userId = Long.valueOf(userIdParam);
    } catch (NumberFormatException e) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "Invalid User ID format").toString());
      return;
    }

    userService.deleteUser(userId)
      .onSuccess(resultMessage -> {
        CommonResponse<String> response = ResultUtils.success(resultMessage);
        try {
          context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      })
      .onFailure(err -> {
        context.response().setStatusCode(404).putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 获取用户信息
  public void getUserById(RoutingContext context) {
    // 从请求路径中获取用户 ID
    String userIdParam = context.request().getParam("id");
    if (userIdParam == null) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "User ID is required").toString());
      return;
    }

    Long userId;
    try {
      userId = Long.valueOf(userIdParam);
    } catch (NumberFormatException e) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "Invalid User ID format").toString());
      return;
    }

    userService.getUserById(userId)
      .onSuccess(user -> {
        if (user != null) {
          CommonResponse<User> response = ResultUtils.success(user);
          try {
            context.response().putHeader("content-type", "application/json")
              .end(mapper.writeValueAsString(response));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        } else {
          context.response().setStatusCode(404).putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "User not found").toString());
        }
      })
      .onFailure(err -> {
        context.response().setStatusCode(500).putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 获取所有用户信息
  public void getUsers(RoutingContext context) {
    userService.getAllUsers()
      .onSuccess(users -> {
        if (users != null) {
          CommonResponse<List<User>> response = ResultUtils.success(users);
          try {
            context.response().putHeader("content-type", "application/json")
              .end(mapper.writeValueAsString(response));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        } else {
          context.response().setStatusCode(404).putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Users not found").toString());
        }
      })
      .onFailure(err -> {
        context.response().setStatusCode(500).putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

}
