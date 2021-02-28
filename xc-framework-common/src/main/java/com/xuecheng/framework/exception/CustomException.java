package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

public class CustomException extends RuntimeException {

     //  RuntimeException异常相较于Exception来说对编写的业务代码没有侵入性 比较友好
    //错误代码
    private ResultCode resultCode;

    public CustomException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode(){
        return  resultCode;
    }

}
