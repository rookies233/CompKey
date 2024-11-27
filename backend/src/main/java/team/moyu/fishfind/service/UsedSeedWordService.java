package team.moyu.fishfind.service;

import io.vertx.core.Future;
import team.moyu.fishfind.entity.UsedSeedWord;

import java.util.List;

/**
 * @author moyu
 */
public interface UsedSeedWordService {

  // 添加搜索记录
  Future<UsedSeedWord> addUsedSeedWord(UsedSeedWord usedSeedWord);

  // 删除搜索记录
  Future<String> deleteUsedSeedWord(Long id);

  // 查询搜索记录
  Future<List<UsedSeedWord>> getUsedSeedWord(Long userId);

}
