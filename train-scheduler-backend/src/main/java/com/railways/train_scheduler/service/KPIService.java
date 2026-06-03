package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.monitoring.KPI;
import com.railways.train_scheduler.repository.KPIRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KPIService {
    
    private final KPIRepository kpiRepository;

    public KPIService(KPIRepository kpiRepository) {
        this.kpiRepository = kpiRepository;
    }

    public KPI saveKPI(@NonNull KPI kpi) {
        return kpiRepository.save(kpi);
    }

    public List<KPI> getAllKPIs() {
        return kpiRepository.findAll();
    }

    public Optional<KPI> getKPIById(@NonNull String id) {
        return kpiRepository.findById(id);
    }
    
    public void deleteKPI(@NonNull String id) {
        kpiRepository.deleteById(id);
    }
}
