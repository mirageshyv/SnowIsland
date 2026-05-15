package com.example.snowisland.config;

import com.example.snowisland.entity.User;
import com.example.snowisland.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/api/ark/status")) {
            return true;
        }

        if (requestUri.startsWith("/api/ark/invest") ||
            requestUri.startsWith("/api/ark/component") ||
            requestUri.startsWith("/api/ark/sail") ||
            requestUri.startsWith("/api/ark/reset")) {
            
            String userIdStr = request.getHeader("userId");
            if (userIdStr == null || userIdStr.isEmpty()) {
                userIdStr = request.getParameter("userId");
            }

            if (userIdStr == null || userIdStr.isEmpty()) {
                sendErrorResponse(response, "请先登录");
                return false;
            }

            try {
                Integer userId = Integer.parseInt(userIdStr);
                User user = userRepository.findById(userId).orElse(null);
                
                if (user == null) {
                    sendErrorResponse(response, "用户不存在");
                    return false;
                }

                if (!User.Role.DM.equals(user.getRole())) {
                    sendErrorResponse(response, "无权进行操作");
                    return false;
                }
            } catch (NumberFormatException e) {
                sendErrorResponse(response, "用户ID格式错误");
                return false;
            }
        }

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}
