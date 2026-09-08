package com.gamesplatform.system.admin.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.system.admin.dto.AdminBindingCodeResponse;
import com.gamesplatform.system.admin.dto.AdminBindingStatusResponse;
import com.gamesplatform.system.admin.service.AdminBindingService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 普通用户侧管理员关联接口。 */
@RestController
@RequestMapping("/api/user/admin-binding")
@RequiredArgsConstructor
public class AdminBindingController {
    /** 管理员用户关联服务。 */
    @Schema(description = "管理员用户关联服务")
    private final AdminBindingService bindingService;

    /** 查询当前用户关联状态。 @param authentication 当前认证。 @return 关联状态。 */
    @GetMapping
    public ApiResponse<AdminBindingStatusResponse> status(Authentication authentication) {
        return ApiResponse.success(bindingService.getBindingStatus((Long) authentication.getPrincipal()));
    }

    /** 生成一次性绑定码。 @param authentication 当前认证。 @return 绑定码。 */
    @PostMapping("/code")
    public ApiResponse<AdminBindingCodeResponse> generateCode(Authentication authentication) {
        return ApiResponse.success(bindingService.generateBindingCode((Long) authentication.getPrincipal()));
    }
}
