package team.moyu.fishfind.service;

import team.moyu.fishfind.entity.Comment;

import java.util.List;

import io.vertx.core.Future;
import team.moyu.fishfind.vo.CommentVo;

/**
 * @author moyu
 */
public interface CommentService {

  // 添加评论
  Future<Comment> addComment(Comment comment);

  // 删除评论
  Future<String> deleteComment(Long commentId);

  // 根据竞争关键词id查询所有评论
  Future<List<CommentVo>> getAllComments(Long compwordId);

}
