package com.example.snowisland.controller;

import com.example.snowisland.service.RuleBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rule-book")
public class RuleBookController {

    @Autowired
    private RuleBookService ruleBookService;

    @GetMapping("/all")
    public Map<String, Object> getAllRules() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", ruleBookService.getAllRules());
        return result;
    }

    @GetMapping("/section/{section}")
    public Map<String, Object> getRulesBySection(@PathVariable String section) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", ruleBookService.getRulesBySection(section));
        return result;
    }
}
