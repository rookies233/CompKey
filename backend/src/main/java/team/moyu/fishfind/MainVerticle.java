package team.moyu.fishfind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import team.moyu.fishfind.handler.UsedSeedWordHandler;
import team.moyu.fishfind.handler.UserHandler;
import team.moyu.fishfind.service.UsedSeedWordService;
import team.moyu.fishfind.service.UserService;
import team.moyu.fishfind.service.impl.UsedSeedWordServiceImpl;
import team.moyu.fishfind.service.impl.UserServiceImpl;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    // MySQL配置
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("localhost")
      .setDatabase("fishfind")
      .setUser("root")
      .setPassword("Hyx_123456");

    // 连接池选项
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    Pool client = MySQLBuilder
      .pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    // 初始化服务与处理器
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    // 用户管理模块
    UserService userService = new UserServiceImpl(client);
    UserHandler userHandler = new UserHandler(userService, mapper);
    //用户搜索记录管理模块
    UsedSeedWordService usedSeedWordService = new UsedSeedWordServiceImpl(client);
    UsedSeedWordHandler usedSeedWordHandler = new UsedSeedWordHandler(usedSeedWordService, mapper);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    // 用户管理模块
    // 登录
    router.post("/users/login").handler(userHandler::login);
    // 注册
    router.post("/users/register").handler(userHandler::register);
    // 更新用户信息
    router.put("/users/:id").handler(userHandler::updateUser);
    // 删除用户
    router.delete("/users/:id").handler(userHandler::deleteUser);

    // 用户搜索记录管理模块
    // 添加搜索记录
    router.post("/usedSeedWords").handler(usedSeedWordHandler::addUsedSeedWord);
    // 删除搜索记录
    router.delete("/usedSeedWords/:id").handler(usedSeedWordHandler::deleteUsedSeedWord);
    // 查询搜索记录
    router.get("/usedSeedWords/:userId").handler(usedSeedWordHandler::getUsedSeedWords);

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

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}
