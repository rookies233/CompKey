package team.moyu.fishfind.service;

import io.vertx.core.Future;
import team.moyu.fishfind.entity.SeedWord;

public interface SeedWordService {
  Future<SeedWord> addSeedWord(String seedWord);
}
