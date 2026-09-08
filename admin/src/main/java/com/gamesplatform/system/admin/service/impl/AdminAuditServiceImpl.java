package com.gamesplatform.system.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.system.admin.entity.AdminOperationLog;
import com.gamesplatform.system.admin.mapper.AdminOperationLogMapper;
import com.gamesplatform.system.admin.service.AdminAuditService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/** 管理员操作审计服务实现。 */
@Service
@RequiredArgsConstructor
public class AdminAuditServiceImpl implements AdminAuditService {
    /** 管理员操作日志数据访问组件。 */
    @Schema(description = "管理员操作日志数据访问组件")
    private final AdminOperationLogMapper operationLogMapper;
    /** JSON 序列化组件。 */
    @Schema(description = "JSON 序列化组件")
    private final ObjectMapper objectMapper;

    /** {@inheritDoc} */
    @Override
    public void record(Long adminId, Long userId, String module, String action, Object before, Object after) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setUserId(userId);
        log.setModule(module);
        log.setAction(action);
        log.setBeforeData(toJson(before));
        log.setAfterData(toJson(after));
        HttpServletRequest request = currentRequest();
        if (request != null) {
            log.setRequestId(request.getHeader("X-Request-Id"));
            log.setIpAddress(clientIp(request));
        }
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
