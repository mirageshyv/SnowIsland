package com.example.snowisland.controller;

import com.example.snowisland.service.PlayerNotebookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notebook")
@CrossOrigin(origins = "*")
public class PlayerNotebookController {

    @Autowired
    private PlayerNotebookService playerNotebookService;

    @GetMapping
    public Map<String, Object> list(@RequestHeader(value = "userId", required = false) Integer userId) {
        return playerNotebookService.listForUser(userId);
    }

    @PostMapping
    public Map<String, Object> create(@RequestHeader(value = "userId", required = false) Integer userId) {
        return playerNotebookService.createForUser(userId);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(
            @RequestHeader(value = "userId", required = false) Integer userId,
            @PathVariable Integer id) {
        return playerNotebookService.getForUser(userId, id);
    }

    @PatchMapping("/{id}")
    public Map<String, Object> patch(
            @RequestHeader(value = "userId", required = false) Integer userId,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> body) {
        return playerNotebookService.patchForUser(userId, id, body != null ? body : new HashMap<>());
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(
            @RequestHeader(value = "userId", required = false) Integer userId,
            @PathVariable Integer id) {
        return playerNotebookService.deleteForUser(userId, id);
    }
}
