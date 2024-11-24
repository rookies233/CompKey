package team.moyu.fishfind.common;

/**
 * 返回工具类
 *
 * @author moyu
 */
public class ResultUtils {

  /**
   * 成功
   *
   * @param data
   * @param <T>
   * @return
   */
  public static <T> CommonResponse<T> success(T data) {
    return new CommonResponse<>(0, data, "ok");
  }

  /**
   * 失败
   *
   * @param errorCode
   * @return
   */
  public static CommonResponse<?> error(ErrorCode errorCode) {
    return new CommonResponse<>(errorCode);
  }

  /**
   * 失败
   *
   * @param code
   * @param message
   * @return
   */
  public static CommonResponse<?> error(int code, String message) {
    return new CommonResponse(code, null, message);
  }

  /**
   * 失败
   *
   * @param errorCode
   * @return
   */
  public static CommonResponse<?> error(ErrorCode errorCode, String message) {
    return new CommonResponse<>(errorCode.getCode(), null, message);
  }
}
