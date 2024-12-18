package team.moyu.fishfind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import team.moyu.fishfind.handler.CommentHandler;
import team.moyu.fishfind.handler.CompKeyHandler;
import team.moyu.fishfind.handler.UsedSeedWordHandler;
import team.moyu.fishfind.handler.UserHandler;
import team.moyu.fishfind.service.*;
import team.moyu.fishfind.service.impl.*;
import org.elasticsearch.client.*;

public class MainVerticle extends AbstractVerticle {

  private RestHighLevelClient esClient;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    // MySQL配置
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("localhost")
      .setDatabase("fishfind")
      .setUser("root")
      .setPassword("123456");

    // 连接池选项
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    Pool client = MySQLBuilder
      .pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    // 单击所创建的Elasticsearch实例ID，在基本信息页面获取公网地址，即为ES集群地址。
    RestClientBuilder builder = RestClient.builder(new HttpHost(
      "119.29.194.130", 9200, "http"));

    // RestHighLevelClient实例通过REST high-level client builder进行构造。
    esClient = new RestHighLevelClient(builder);


    // 初始化服务与处理器
    ObjectMapper mapper = DatabindCodec.mapper();
    mapper.registerModule(new JavaTimeModule());
    // 用户管理模块
    UserService userService = new UserServiceImpl(client);
    UserHandler userHandler = new UserHandler(userService, mapper);
    //用户搜索记录管理模块
    UsedSeedWordService usedSeedWordService = new UsedSeedWordServiceImpl(client);
    UsedSeedWordHandler usedSeedWordHandler = new UsedSeedWordHandler(usedSeedWordService, mapper);
    // 种子关键词
    SeedWordService seedWordService = new SeedWordServiceImpl(client);
    // 搜索模块
    CompKeyService compKeyService = new CompKeyServiceESImpl(esClient, client, usedSeedWordService, seedWordService);
    CompKeyHandler compKeyHandler = new CompKeyHandler(compKeyService, usedSeedWordService, mapper);
    // 评论模块
    CommentService commentService = new CommentServiceImpl(client);
    CommentHandler commentHandler = new CommentHandler(commentService, mapper);


    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    CorsHandler corsHandler = CorsHandler.create("*")
      .allowedMethod(io.vertx.core.http.HttpMethod.GET)
      .allowedMethod(io.vertx.core.http.HttpMethod.POST)
      .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
      .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
      .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
      .allowedHeader("Content-Type")
      .allowedHeader("Authorization");

    // 将 CorsHandler 添加到路由链中
    router.route().handler(corsHandler).handler(BodyHandler.create());
    // 用户管理模块
    // 登录
    router.post("/users/login").handler(userHandler::login);
    // 注册
    router.post("/users/register").handler(userHandler::register);
    // 更新用户信息
    router.put("/users/:id").handler(userHandler::updateUser);
    // 删除用户
    router.delete("/users/:id").handler(userHandler::deleteUser);
    // 获取用户信息
    router.get("/users/:id").handler(userHandler::getUserById);
    // 获取所有用户信息
    router.get("/users").handler(userHandler::getUsers);

    // 用户搜索记录管理模块
    // 添加搜索记录
    router.post("/usedSeedWords").handler(usedSeedWordHandler::addUsedSeedWord);
    // 删除搜索记录
    router.delete("/usedSeedWords/:id").handler(usedSeedWordHandler::deleteUsedSeedWord);
    // 查询搜索记录
    router.get("/usedSeedWords/:userId").handler(usedSeedWordHandler::getUsedSeedWords);

    // 搜索模块
    router.get("/compKeys").handler(compKeyHandler::getCompWords);

    // 评论模块
    // 添加评论
    router.post("/comments").handler(commentHandler::addComment);
    // 删除评论
    router.delete("/comments/:id").handler(commentHandler::deleteComment);
    // 获取评论
    router.get("/comments/:compwordId").handler(commentHandler::getAllCommentsById);
    // 获取所有评论
    router.get("/comments").handler(commentHandler::getAllComments);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080)
      .onComplete(http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("HTTP server started on port " + 8080);
        } else {
          startPromise.fail(http.cause());
        }
      });
  }

  @Override
  public void stop() throws Exception {
    esClient.close();
    super.stop();
  }

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}
