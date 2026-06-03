package com.railways.train_scheduler.controller;

import com.railways.train_scheduler.model.core.Track;
import com.railways.train_scheduler.repository.TrackRepository;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackRepository trackRepo;

    public TrackController(TrackRepository trackRepo) {
        this.trackRepo = trackRepo;
    }

    // Create new track
    @PostMapping
    public Track addTrack(@RequestBody @NonNull Track track) {
        return trackRepo.save(track);
    }

    // Get all tracks
    @GetMapping
    public List<Track> getAllTracks() {
        return trackRepo.findAll();
    }

    // Get track by id
    @GetMapping("/{id}")
    public Track getTrack(@PathVariable @NonNull String id) {
        return trackRepo.findById(id).orElse(null);
    }
    // Delete a track
    @DeleteMapping("/{id}")
    public void deleteTrack(@PathVariable @NonNull String id) {
    trackRepo.deleteById(id);
     }
    // Delete all tracks
     @DeleteMapping
    public void deleteAllTracks() {
    trackRepo.deleteAll();
}

}
