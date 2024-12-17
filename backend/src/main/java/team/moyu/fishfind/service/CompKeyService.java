package team.moyu.fishfind.service;

import io.vertx.core.Future;
import team.moyu.fishfind.dto.CompKeyReqDTO;
import team.moyu.fishfind.dto.CompKeyRespDTO;

import java.util.List;

/**
 * @author moyu
 */
public interface CompKeyService {
  Future<List<CompKeyRespDTO>> getCompKeys(CompKeyReqDTO requestParam);
}
