package com.gamesplatform.system.admin.security;

import com.gamesplatform.system.admin.entity.AdminAccount;
import com.gamesplatform.system.admin.mapper.AdminAccountMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 独立管理员 JWT 身份认证过滤器。
 */
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    /** 管理员令牌组件。 */
    @Schema(description = "管理员令牌组件")
    private final AdminJwtTokenProvider tokenProvider;
    /** 管理员账户数据访问组件。 */
    @Schema(description = "管理员账户数据访问组件")
    private final AdminAccountMapper adminAccountMapper;

    /** {@inheritDoc} */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            Long adminId = tokenProvider.getAdminId(token);
            AdminAccount admin = adminAccountMapper.selectById(adminId);
            if (admin != null && "ENABLED".equals(admin.getStatus())
                    && Objects.equals(admin.getTokenVersion(), tokenProvider.getTokenVersion(token))) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                adminId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/admin/");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")
                ? bearer.substring(7) : null;
    }
}
