package com.green.eats.common.auth;
//각 서비스에 로그인 정보를 전달하는 역할

import com.green.eats.common.model.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component // common에서 이렇게 하면.. auth에서도 빈등록, store에서도 빈등록
public class UserContextInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");

        if (userId != null && userName != null) {
            try {
                String decodedUserName = URLDecoder.decode(userName, StandardCharsets.UTF_8);
                UserContext.set(new UserDto(Long.parseLong(userId), decodedUserName));
            } catch (Exception e) {
                // 디코딩 실패 시 원본 사용 (또는 무시)
                UserContext.set(new UserDto(Long.parseLong(userId), userName));
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear(); // 메모리 누수 방지를 위해 반드시 삭제
    }
}