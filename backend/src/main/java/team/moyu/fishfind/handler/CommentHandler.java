package team.moyu.fishfind.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import team.moyu.fishfind.common.CommonResponse;
import team.moyu.fishfind.common.ResultUtils;
import team.moyu.fishfind.entity.Comment;
import team.moyu.fishfind.service.CommentService;
import team.moyu.fishfind.vo.CommentVo;

import java.util.List;

/**
 * @author moyu
 */
public class CommentHandler {

  private final CommentService commentService;

  private final ObjectMapper mapper;

  public CommentHandler(CommentService commentService, ObjectMapper mapper) {
    this.commentService = commentService;
    this.mapper = mapper;
  }

  // 添加评论
  public void addComment(RoutingContext context) {
    JsonObject body = context.getBodyAsJson();
    Long userId = body.getLong("userId");
    Long compwordId = body.getLong("compwordId");
    int grade = body.getInteger("grade");
    String text = body.getString("text");

    Comment newComment = new Comment();
    newComment.setUserId(userId);
    newComment.setCompwordId(compwordId);
    newComment.setGrade(grade);
    newComment.setText(text);

    commentService.addComment(newComment)
      .onSuccess(comment -> {
        CommonResponse<Comment> response = ResultUtils.success(comment);
        try {
          context.response().putHeader("content-type", "application/json").end(mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }).onFailure(err -> {
        context.response().setStatusCode(401).putHeader("content-type", "application/json").end(new JsonObject().put("error", err.getMessage()).toString());
      });
  }

  // 删除评论
  public void deleteComment(RoutingContext context) {
    // 从请求路径中获取评论 ID
    String commentIdParam = context.request().getParam("id");
    if (commentIdParam == null) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "Comment ID is required").toString());
      return;
    }

    Long commentId;
    try {
      commentId = Long.valueOf(commentIdParam);
    } catch (NumberFormatException e) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "Invalid Comment ID format").toString());
      return;
    }

    commentService.deleteComment(commentId)
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

  // 获取评论列表
  public void getAllComments(RoutingContext context) {
    // 从请求路径中获取竞争关键词id
    String compwordIdParam = context.request().getParam("compwordId");
    if (compwordIdParam == null) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "Compword ID is required").toString());
      return;
    }

    Long compwordId;
    try {
      compwordId = Long.valueOf(compwordIdParam);
    } catch (NumberFormatException e) {
      context.response().setStatusCode(400).putHeader("content-type", "application/json")
        .end(new JsonObject().put("error", "Invalid Compword ID format").toString());
      return;
    }

    commentService.getAllComments(compwordId)
      .onSuccess(resultMessage -> {
        CommonResponse<List<CommentVo>> response = ResultUtils.success(resultMessage);
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

}
