package com.gdut.dkmfromcg.commonlib.net.exception;

/**
 * Created by dkmFromCG on 2018/4/7.
 * function: 自定义的错误类型: 在解析服务端返回数据的时候，当code!=0，就抛出ServerException
 */

public class ServerException extends RuntimeException {

    private int code;//表示接口请求状态，0表示成功，-101表示密码错误等等
    private String msg;//表示接口请求返回的描述。success，”token过期”等等

    public ServerException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
