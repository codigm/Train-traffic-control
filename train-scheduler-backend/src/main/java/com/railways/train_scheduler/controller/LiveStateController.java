package com.railways.train_scheduler.controller;

import com.railways.train_scheduler.model.monitoring.LiveState;
import com.railways.train_scheduler.service.LiveStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livestate")
public class LiveStateController {
    
    private final LiveStateService liveStateService;

    public LiveStateController(LiveStateService liveStateService) {
        this.liveStateService = liveStateService;
    }

    @PostMapping
    public ResponseEntity<LiveState> updateLiveState(@RequestBody @NonNull LiveState state) {
        return ResponseEntity.ok(liveStateService.updateLiveState(state));
    }

    @GetMapping
    public ResponseEntity<List<LiveState>> getAllLiveStates() {
        return ResponseEntity.ok(liveStateService.getAllLiveStates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiveState> getLiveStateById(@PathVariable @NonNull String id) {
        return liveStateService.getLiveStateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLiveState(@PathVariable @NonNull String id) {
        liveStateService.deleteLiveState(id);
        return ResponseEntity.noContent().build();
    }
}
