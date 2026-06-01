package com.gamesplatform.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求。
 */
@Data
public class RegisterRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50")
    private String username;

    /**
     * 密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50")
    private String password;

    /**
     * 昵称。
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50")
    private String nickname;
}
