package com.gamesplatform.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 业务编码。
     */
    private int code;
    /**
     * 提示信息。
     */
    private String message;
    /**
     * 响应数据。
     */
    private T data;

    /**
     * 创建成功响应。
     *
     * @param data 响应数据。
     * @return 处理结果。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 创建成功响应。
     *
     * @param message 提示信息。
     * @param data 响应数据。
     * @return 处理结果。
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 创建失败响应。
     *
     * @param code 业务编码。
     * @param message 提示信息。
     * @return 处理结果。
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 创建失败响应。
     *
     * @param message 提示信息。
     * @return 处理结果。
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(400, message);
    }
}
