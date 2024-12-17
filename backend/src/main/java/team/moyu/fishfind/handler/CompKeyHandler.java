package team.moyu.fishfind.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import team.moyu.fishfind.common.CommonResponse;
import team.moyu.fishfind.common.ResultUtils;
import team.moyu.fishfind.dto.CompKeyReqDTO;
import team.moyu.fishfind.dto.CompKeyRespDTO;
import team.moyu.fishfind.service.CompKeyService;
import team.moyu.fishfind.service.UsedSeedWordService;

import java.util.List;

public class CompKeyHandler {

  private final CompKeyService compKeyService;

  private final UsedSeedWordService usedSeedWordService;

  private final ObjectMapper mapper;

  public CompKeyHandler(CompKeyService compKeyService, UsedSeedWordService usedSeedWordService, ObjectMapper mapper) {
    this.compKeyService = compKeyService;
    this.usedSeedWordService = usedSeedWordService;
    this.mapper = mapper;
  }

  public void getCompWords(RoutingContext context) {
    String seedWord = context.request().getParam("seedWord");
    Long userId = Long.parseLong(context.request().getParam("userId"));
    CompKeyReqDTO requestParam = new CompKeyReqDTO();
    requestParam.setSeedWord(seedWord);
    requestParam.setUserId(userId);
    compKeyService.getCompKeys(requestParam).onSuccess(results -> {
      CommonResponse<List<CompKeyRespDTO>> response = ResultUtils.success(results);
      try {
        context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }).onFailure(err -> {
      context.response().setStatusCode(500).putHeader("content-type", "application/json").end(err.getMessage());
    });
  }
}
