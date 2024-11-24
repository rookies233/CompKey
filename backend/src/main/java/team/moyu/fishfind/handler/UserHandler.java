package team.moyu.fishfind.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import team.moyu.fishfind.common.CommonResponse;
import team.moyu.fishfind.common.ResultUtils;
import team.moyu.fishfind.entity.User;
import team.moyu.fishfind.service.UserService;

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

  public void login(RoutingContext context) {
    JsonObject body = context.getBodyAsJson();
    String username = body.getString("username");
    String password = body.getString("password");

    userService.login(username, password)
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
}
