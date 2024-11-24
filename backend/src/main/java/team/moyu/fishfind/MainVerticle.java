package team.moyu.fishfind;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import team.moyu.fishfind.handler.UserHandler;
import team.moyu.fishfind.service.UserService;
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

    // 初始化服务与处理器
    ObjectMapper mapper = new ObjectMapper();
    UserService userService = new UserServiceImpl(client);
    UserHandler userHandler = new UserHandler(userService, mapper);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.post("/users/login").handler(userHandler::login);

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
