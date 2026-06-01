package com.gamesplatform.common;

/**
 * 业务异常。
 */
public class BusinessException extends RuntimeException {

    /**
     * 业务编码。
     */
    private final int code;

    /**
     * 使用默认业务编码创建异常。
     *
     * @param message 提示信息。
     */
    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * 使用指定业务编码创建异常。
     *
     * @param code 业务编码。
     * @param message 提示信息。
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 查询业务异常编码。
     *
     * @return 处理结果。
     */
    public int getCode() {
        return code;
    }
}
