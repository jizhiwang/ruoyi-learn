package com.example.common;

import com.example.common.constant.HttpStatus;

import java.util.LinkedHashMap;

public class Result extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static final String CODE_KEY = "code";
    public static final String MSG_KEY = "msg";
    public static final String DATA_KEY = "data";

    private static final String DEFAULT_SUCCESS_MSG = "操作成功";
    private static final String DEFAULT_ERROR_MSG = "操作失败";

    // ==================== 构造器 ====================

    public Result() {
        this(HttpStatus.SUCCESS, DEFAULT_SUCCESS_MSG, null);
    }

    public Result(int code, String msg) {
        this(code, msg, null);
    }

    /** 唯一真正做初始化的构造器，其余构造器都委托到这里 */
    public Result(int code, String msg, Object data) {
        super.put(CODE_KEY, code);
        super.put(MSG_KEY, msg);
        if (data != null) {
            super.put(DATA_KEY, data);
        }
    }

    // ==================== 成功 ====================

    /** 成功，无数据：{"code":200,"msg":"操作成功"} */
    public static Result success() {
        return new Result(HttpStatus.SUCCESS, DEFAULT_SUCCESS_MSG, null);
    }

    /**
     * 成功，携带数据。
     * 单参数明确视为「数据」，即使传的是 String。
     */
    public static Result success(Object data) {
        return new Result(HttpStatus.SUCCESS, DEFAULT_SUCCESS_MSG, data);
    }

    /** 成功，仅自定义提示消息：{"code":200,"msg":"保存成功"} */
    public static Result successMsg(String msg) {
        return new Result(HttpStatus.SUCCESS, msg, null);
    }

    /** 成功，提示消息 + 数据 */
    public static Result success(String msg, Object data) {
        return new Result(HttpStatus.SUCCESS, msg, data);
    }

    // ==================== 警告 ====================

    public static Result warn(String msg) {
        return new Result(HttpStatus.WARN, msg, null);
    }

    public static Result warn(String msg, Object data) {
        return new Result(HttpStatus.WARN, msg, data);
    }

    // ==================== 错误 ====================

    /** 错误，默认提示 "操作失败" */
    public static Result error() {
        return new Result(HttpStatus.ERROR, DEFAULT_ERROR_MSG, null);
    }

    public static Result error(String msg) {
        return new Result(HttpStatus.ERROR, msg, null);
    }

    public static Result error(String msg, Object data) {
        return new Result(HttpStatus.ERROR, msg, data);
    }

    public static Result error(int code, String msg) {
        return new Result(code, msg, null);
    }

    public static Result error(int code, String msg, Object data) {
        return new Result(code, msg, data);
    }

    // ==================== 读取 ====================

    /** 状态码，缺失或非数字时按 500 处理 */
    public int getCode() {
        Object code = get(CODE_KEY);
        return code instanceof Number n ? n.intValue() : HttpStatus.ERROR;
    }

    /** 提示消息，缺失时返回空串 */
    public String getMsg() {
        Object msg = get(MSG_KEY);
        return msg == null ? "" : msg.toString();
    }

    /** 业务数据，可能为 null */
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) get(DATA_KEY);
    }

    /** 是否成功（code == 200） */
    public boolean isSuccess() {
        return getCode() == HttpStatus.SUCCESS;
    }

    /** 是否警告（code == 601） */
    public boolean isWarn() {
        return getCode() == HttpStatus.WARN;
    }

    /**
     * 是否错误（code == 500）。
     * 注意：这里只精确匹配 ERROR，401/403/404 等自定义错误码不会被识别。
     * 如需「非成功即错误」语义，改用 {@link #isSuccess()} 取反。
     */
    public boolean isError() {
        return getCode() == HttpStatus.ERROR;
    }

    // ==================== 链式扩展 ====================

    /** 追加自定义字段，返回 this 以支持链式调用 */
    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
