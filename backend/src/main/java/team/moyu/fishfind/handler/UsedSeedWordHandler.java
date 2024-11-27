package team.moyu.fishfind.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import team.moyu.fishfind.common.CommonResponse;
import team.moyu.fishfind.common.ResultUtils;
import team.moyu.fishfind.entity.UsedSeedWord;
import team.moyu.fishfind.service.UsedSeedWordService;

import java.util.List;

/**
 * @author moyu
 */
public class UsedSeedWordHandler {

  private final UsedSeedWordService usedSeedWordService;

  private final ObjectMapper mapper;

  public UsedSeedWordHandler(UsedSeedWordService usedSeedWordService, ObjectMapper mapper) {
    this.usedSeedWordService = usedSeedWordService;
    this.mapper = mapper;
  }

  // 添加已使用的种子词
  public void addUsedSeedWord(RoutingContext context) {
    JsonObject body = context.getBodyAsJson();
    UsedSeedWord usedSeedWord = new UsedSeedWord();
    usedSeedWord.setSeedWordId(body.getLong("seedWordId"));
    usedSeedWord.setUserId(body.getLong("userId"));

    usedSeedWordService.addUsedSeedWord(usedSeedWord)
      .onSuccess(result -> {
        CommonResponse<UsedSeedWord> response = ResultUtils.success(result);
        try {
          context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }).onFailure(err -> {
        context.response().setStatusCode(500).putHeader("content-type", "application/json").end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 删除已使用的种子词
  public void deleteUsedSeedWord(RoutingContext context) {
    Long id = Long.valueOf(context.pathParam("id"));

    usedSeedWordService.deleteUsedSeedWord(id)
      .onSuccess(message -> {
        context.response().putHeader("content-type", "application/json").end(new JsonObject().put("message", message).toString());
      }).onFailure(err -> {
        context.response().setStatusCode(404).putHeader("content-type", "application/json").end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 获取用户搜索记录
  public void getUsedSeedWords(RoutingContext context) {
    Long userId = Long.valueOf(context.pathParam("userId"));

    usedSeedWordService.getUsedSeedWord(userId)
      .onSuccess(result -> {
        CommonResponse<List<UsedSeedWord>> response = ResultUtils.success(result);
        try {
          context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }).onFailure(err -> {
        context.response().setStatusCode(404).putHeader("content-type", "application/json").end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

}
