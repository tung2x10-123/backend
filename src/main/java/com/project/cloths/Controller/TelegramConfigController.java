package com.project.cloths.Controller;

import com.project.cloths.Entity.TelegramConfig;
import com.project.cloths.repository.TelegramConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/telegram-configs")
public class TelegramConfigController {

    @Autowired
    private TelegramConfigRepository telegramConfigRepository;

    @GetMapping
    public List<TelegramConfig> getAllConfigs() {
        return telegramConfigRepository.findAll();
    }

    @PostMapping
    public TelegramConfig createConfig(@RequestBody TelegramConfig config) {
        return telegramConfigRepository.save(config);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TelegramConfig> updateConfig(@PathVariable Long id, @RequestBody TelegramConfig configDetails) {
        TelegramConfig config = telegramConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TelegramConfig not found with id: " + id));
        config.setBotToken(configDetails.getBotToken());
        config.setChatId(configDetails.getChatId());
        TelegramConfig updatedConfig = telegramConfigRepository.save(config);
        return ResponseEntity.ok(updatedConfig);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        TelegramConfig config = telegramConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TelegramConfig not found with id: " + id));
        telegramConfigRepository.delete(config);
        return ResponseEntity.ok().build();
    }
}