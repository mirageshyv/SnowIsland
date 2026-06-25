package com.example.snowisland.service;

import com.example.snowisland.entity.EndgameShelterEvent;
import com.example.snowisland.entity.EndgameArkEvent;
import com.example.snowisland.repository.EndgameShelterEventRepository;
import com.example.snowisland.repository.EndgameArkEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EndgameSettlementService {

    @Autowired
    private EndgameShelterEventRepository shelterEventRepository;

    @Autowired
    private EndgameArkEventRepository arkEventRepository;

    private final Random random = new Random();

    public Map<String, Object> drawShelterEvent() {
        List<EndgameShelterEvent> all = shelterEventRepository.findAll();
        return drawRandom(all, this::toShelterMap);
    }

    public Map<String, Object> drawArkEvent() {
        List<EndgameArkEvent> all = arkEventRepository.findAll();
        return drawRandom(all, this::toArkMap);
    }

    public List<Map<String, Object>> getAllShelterEvents() {
        return shelterEventRepository.findAll().stream()
                .map(this::toShelterMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllArkEvents() {
        return arkEventRepository.findAll().stream()
                .map(this::toArkMap)
                .collect(Collectors.toList());
    }

    private <T> Map<String, Object> drawRandom(List<T> all, java.util.function.Function<T, Map<String, Object>> mapper) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (all.isEmpty()) {
            result.put("success", false);
            result.put("message", "事件池为空，请先添加事件");
            return result;
        }
        T picked = all.get(random.nextInt(all.size()));
        result.put("success", true);
        result.put("event", mapper.apply(picked));
        return result;
    }

    private Map<String, Object> toShelterMap(EndgameShelterEvent e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("title", e.getTitle());
        m.put("description", e.getDescription());
        m.put("category", e.getCategory());
        m.put("sortOrder", e.getSortOrder());
        m.put("createdAt", e.getCreatedAt());
        return m;
    }

    private Map<String, Object> toArkMap(EndgameArkEvent e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("title", e.getTitle());
        m.put("description", e.getDescription());
        m.put("category", e.getCategory());
        m.put("sortOrder", e.getSortOrder());
        m.put("createdAt", e.getCreatedAt());
        return m;
    }
}
