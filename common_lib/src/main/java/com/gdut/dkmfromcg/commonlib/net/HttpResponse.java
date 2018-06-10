package com.gdut.dkmfromcg.commonlib.net;

/**
 * Created by dkmFromCG on 2018/4/8.
 * function: 服务器约定返回的数据格式
 */

public class HttpResponse {

    private String message;//返回信息
    private int code; //状态码
    private Object data;

    /**
     * 是否成功(这里约定 code=0 为成功)
     *
     * @return
     */
    public boolean isSuccess() {
        return code == 0 ? true : false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        String response = "[http response]" + "{\"code\": " + code + ",\"message\":" + message +
                ",\"data\":" + data + "}";
        return response;
    }
}
