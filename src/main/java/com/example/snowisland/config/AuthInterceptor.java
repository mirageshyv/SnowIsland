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
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod() != null ? request.getMethod() : "";

        if (uri.startsWith("/api/warehouses") && "GET".equalsIgnoreCase(method)) {
            String userRole = request.getParameter("userRole");
            if (userRole != null && "dm".equalsIgnoreCase(userRole)) {
                return checkDmPermission(request, response);
            }
            return true;
        }

        if (isPublic(uri, method)) {
            return true;
        }

        if (requiresLogin(uri)) {
            return checkLoggedIn(request, response);
        }

        return checkDmPermission(request, response);
    }

    /** Player-facing reads/submits that are registered on this interceptor. */
    private static boolean isPublic(String uri, String method) {
        boolean get = "GET".equalsIgnoreCase(method);

        if (uri.startsWith("/api/ark/status") || uri.startsWith("/api/ark/init-progress")) {
            return true;
        }

        if (uri.equals("/api/game-state") && get) {
            return true;
        }

        if (uri.startsWith("/api/catastrophe/") && get) {
            return true;
        }

        // 天灾使者选牌；其余 catastrophe POST 仍需真实 DM
        if (uri.equals("/api/catastrophe/select-card") && "POST".equalsIgnoreCase(method)) {
            return true;
        }

        if (uri.startsWith("/api/shelter") && get) {
            return true;
        }

        if (uri.startsWith("/api/shelter/labor/roster") && "PUT".equalsIgnoreCase(method)) {
            return true;
        }

        if (uri.startsWith("/api/actions/submit")
                || uri.startsWith("/api/actions/submit-context")
                || uri.startsWith("/api/actions/player/")
                || uri.startsWith("/api/actions/production-info/")
                || uri.startsWith("/api/actions/stealth/")) {
            return true;
        }

        if (uri.startsWith("/api/night-actions/context/")
                || uri.startsWith("/api/night-actions/submit")) {
            return true;
        }

        if (uri.startsWith("/api/faction-actions/context/")
                || uri.startsWith("/api/faction-actions/submit")
                || uri.startsWith("/api/faction-actions/player/")) {
            return true;
        }

        if (uri.startsWith("/api/quick-interactions/context/")
                || uri.startsWith("/api/quick-interactions/submit")) {
            return true;
        }

        if (uri.startsWith("/api/exploration/submit")
                || (get && uri.startsWith("/api/exploration/player/"))
                || (get && uri.startsWith("/api/exploration/events"))) {
            return true;
        }

        if (uri.startsWith("/api/jobs") && get && !uri.equals("/api/jobs/all") && !uri.startsWith("/api/jobs/all")) {
            return true;
        }

        if (uri.startsWith("/api/skills") && get) {
            return true;
        }

        if (uri.startsWith("/api/locations") && get) {
            return true;
        }

        if (uri.startsWith("/api/players") && get) {
            return true;
        }

        if (uri.startsWith("/api/lore/") && get) {
            return true;
        }

        if (uri.startsWith("/api/lore/acknowledge") && "POST".equalsIgnoreCase(method)) {
            return true;
        }

        if (uri.startsWith("/api/milestones") && get) {
            return true;
        }

        return false;
    }

    private static boolean requiresLogin(String uri) {
        return uri.startsWith("/api/notebook");
    }

    private boolean checkLoggedIn(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        } catch (NumberFormatException e) {
            sendErrorResponse(response, "用户ID格式错误");
            return false;
        }
        return true;
    }

    private boolean checkDmPermission(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
