package com.example.snowisland.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/auth/reset-password",
                        "/api/auth/users",
                        "/api/ark/**",
                        "/api/player-markers",
                        "/api/player-markers/**",
                        "/api/actions/**",
                        "/api/night-actions/**",
                        "/api/faction-actions/**",
                        "/api/quick-interactions/**",
                        "/api/dm/**",
                        "/api/jobs",
                        "/api/jobs/**",
                        "/api/skills",
                        "/api/skills/**",
                        "/api/locations",
                        "/api/locations/**",
                        "/api/players",
                        "/api/players/**",
                        "/api/game-reset/**",
                        "/api/game-state",
                        "/api/game-state/**",
                        "/api/catastrophe/**",
                        "/api/warehouses/**",
                        "/api/shelter/**",
                        "/api/npc/manage/**",
                        "/api/npc/favor/set",
                        "/api/lore/**",
                        "/api/milestones/**",
                        "/api/special-clue/**",
                        "/api/exploration/**",
                        "/api/notebook",
                        "/api/notebook/**");
    }
}
