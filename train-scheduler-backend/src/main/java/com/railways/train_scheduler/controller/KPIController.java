package com.railways.train_scheduler.controller;

import com.railways.train_scheduler.model.monitoring.KPI;
import com.railways.train_scheduler.service.KPIService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kpi")
public class KPIController {
    
    private final KPIService kpiService;

    public KPIController(KPIService kpiService) {
        this.kpiService = kpiService;
    }

    @PostMapping
    public ResponseEntity<KPI> createKPI(@RequestBody @NonNull KPI kpi) {
        return ResponseEntity.ok(kpiService.saveKPI(kpi));
    }

    @GetMapping
    public ResponseEntity<List<KPI>> getAllKPIs() {
        return ResponseEntity.ok(kpiService.getAllKPIs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KPI> getKPIById(@PathVariable @NonNull String id) {
        return kpiService.getKPIById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKPI(@PathVariable @NonNull String id) {
        kpiService.deleteKPI(id);
        return ResponseEntity.noContent().build();
    }
}
