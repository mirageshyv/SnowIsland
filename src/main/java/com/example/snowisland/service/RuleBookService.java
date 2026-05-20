package com.example.snowisland.service;

import com.example.snowisland.entity.RuleBook;
import com.example.snowisland.repository.RuleBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RuleBookService {

    @Autowired
    private RuleBookRepository ruleBookRepository;

    public Map<String, Object> getAllRules() {
        Map<String, Object> result = new HashMap<>();
        result.put("general", ruleBookRepository.findBySectionOrderByOrderNum("general"));
        result.put("ruler", ruleBookRepository.findBySectionOrderByOrderNum("ruler"));
        result.put("rebel", ruleBookRepository.findBySectionOrderByOrderNum("rebel"));
        result.put("adventurer", ruleBookRepository.findBySectionOrderByOrderNum("adventurer"));
        result.put("scourge", ruleBookRepository.findBySectionOrderByOrderNum("scourge"));
        result.put("civilian", ruleBookRepository.findBySectionOrderByOrderNum("civilian"));
        return result;
    }

    public List<RuleBook> getRulesBySection(String section) {
        return ruleBookRepository.findBySectionOrderByOrderNum(section);
    }
}
