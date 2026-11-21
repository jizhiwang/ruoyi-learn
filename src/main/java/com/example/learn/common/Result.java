package com.example.learn.common;

import com.example.learn.common.constant.HttpStatus;

import java.util.LinkedHashMap;

/**
 * 操作消息提醒
 *
 * @author ruoyi
 */
public class Result extends LinkedHashMap<String, Object>
{
    private static final long serialVersionUID = 1L;
    public static final String CODE_KEY = "code";
    public static final String MSG_KEY = "msg";
    public static final String DATA_KEY = "data";

    /** 默认成功提示 */
    private static final String DEFAULT_SUCCESS_MSG = "操作成功";

    /** 默认失败提示 */
    private static final String DEFAULT_ERROR_MSG = "操作失败";

    public Result()
    {
        this(HttpStatus.SUCCESS, DEFAULT_SUCCESS_MSG);
    }

    public Result(int code, String msg)
    {
        super.put(CODE_KEY, code);
        super.put(MSG_KEY, msg);
    }

    public Result(int code, String msg, Object data)
    {
        this(code, msg);
        if (data != null)
        {
            super.put(DATA_KEY, data);
        }
    }



    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static Result success()
    {
        return Result.success(DEFAULT_SUCCESS_MSG);
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static Result success(Object data)
    {
        return Result.success(DEFAULT_SUCCESS_MSG, data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static Result success(String msg)
    {
        return Result.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static Result success(String msg, Object data)
    {
        return new Result(HttpStatus.SUCCESS, msg, data);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static Result warn(String msg)
    {
        return new Result(HttpStatus.WARN, msg, null);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static Result warn(String msg, Object data)
    {
        return new Result(HttpStatus.WARN, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public static Result error()
    {
        return new Result(HttpStatus.ERROR, DEFAULT_ERROR_MSG, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 错误消息
     */
    public static Result error(String msg)
    {
        return new Result(HttpStatus.ERROR, msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 错误消息
     */
    public static Result error(String msg, Object data)
    {
        return new Result(HttpStatus.ERROR, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg 返回内容
     * @return 错误消息
     */
    public static Result error(int code, String msg)
    {
        return new Result(code, msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg 返回内容
     * @param data 数据对象
     * @return 错误消息
     */
    public static Result error(int code, String msg, Object data)
    {
        return new Result(code, msg, data);
    }

    /**
     * 状态码，缺失或类型不符时按 500 处理
     *
     * @return 状态码
     */
    public int getCode()
    {
        Object code = this.get(CODE_KEY);
        return code instanceof Number number ? number.intValue() : HttpStatus.ERROR;
    }

    /**
     * 提示消息，缺失时返回空串
     *
     * @return 提示消息
     */
    public String getMsg()
    {
        Object msg = this.get(MSG_KEY);
        return msg == null ? "" : msg.toString();
    }

    /**
     * 业务数据
     *
     * @param <T> 数据类型
     * @return 业务数据，可能为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getData()
    {
        return (T) this.get(DATA_KEY);
    }

    /**
     * 是否为成功消息
     *
     * @return 结果
     */
    public boolean isSuccess()
    {
        return getCode() == HttpStatus.SUCCESS;
    }

    /**
     * 是否为警告消息
     *
     * @return 结果
     */
    public boolean isWarn()
    {
        return getCode() == HttpStatus.WARN;
    }

    /**
     * 是否为错误消息
     *
     * @return 结果
     */
    public boolean isError()
    {
        return getCode() == HttpStatus.ERROR;
    }

    /**
     * 方便链式调用
     *
     * @param key 键
     * @param value 值
     * @return 数据对象
     */
    @Override
    public Result put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
