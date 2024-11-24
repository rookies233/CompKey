package team.moyu.fishfind.common;


import java.io.Serializable;

/**
 * 通用返回类
 *
 * @author moyu
 */
public class CommonResponse<T> implements Serializable {
  private int code;
  private String message;
  private T data;

  public CommonResponse(int code, T data, String message) {
    this.code = code;
    this.data = data;
    this.message = message;
  }

  public CommonResponse(int code, T data) {
    this(code, data, "");
  }

  public CommonResponse(ErrorCode errorCode) {
    this(errorCode.getCode(), null, errorCode.getMessage());
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
