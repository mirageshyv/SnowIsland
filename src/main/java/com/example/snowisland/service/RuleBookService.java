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
        List<RuleBook> allRules = ruleBookRepository.findAllByOrderByOrderNum();
        for (RuleBook rule : allRules) {
            String section = rule.getSection();
            if (!result.containsKey(section)) {
                result.put(section, new java.util.ArrayList<RuleBook>());
            }
            @SuppressWarnings("unchecked")
            List<RuleBook> sectionList = (List<RuleBook>) result.get(section);
            sectionList.add(rule);
        }
        return result;
    }

    public List<RuleBook> getRulesBySection(String section) {
        return ruleBookRepository.findBySectionOrderByOrderNum(section);
    }
}
