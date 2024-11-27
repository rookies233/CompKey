package team.moyu.fishfind.service;

import io.vertx.core.Future;
import team.moyu.fishfind.algorithm.CompKeyResult;

import java.util.List;

/**
 * @author moyu
 */
public interface CompKeyService {
  Future<List<CompKeyResult>> getCompKeys(String seedKey);
}
