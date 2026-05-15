package com.example.snowisland.controller;

import com.example.snowisland.entity.Location;
import com.example.snowisland.entity.LocationFacility;
import com.example.snowisland.entity.LocationNpc;
import com.example.snowisland.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllLocations(
            @RequestParam(required = false) String area) {
        if (area != null && !area.isEmpty()) {
            return ResponseEntity.ok(locationService.getLocationsByArea(area));
        }
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLocationById(@PathVariable Integer id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createLocation(@RequestBody Map<String, Object> body) {
        Location location = new Location();
        location.setName((String) body.get("name"));
        location.setArea((String) body.get("area"));
        location.setDescription((String) body.get("description"));
        location.setDefenseValue(body.get("defenseValue") != null ? ((Number) body.get("defenseValue")).intValue() : 0);
        location.setManagement((String) body.get("management"));
        location.setOrderNumber(body.get("orderNumber") != null ? ((Number) body.get("orderNumber")).intValue() : 0);

        List<LocationFacility> facilities = null;
        if (body.get("facilities") != null) {
            List<Map<String, Object>> facilityMaps = (List<Map<String, Object>>) body.get("facilities");
            facilities = facilityMaps.stream().map(fm -> {
                LocationFacility f = new LocationFacility();
                f.setName((String) fm.get("name"));
                f.setDescription((String) fm.get("description"));
                return f;
            }).collect(java.util.stream.Collectors.toList());
        }

        List<LocationNpc> npcs = null;
        if (body.get("npcs") != null) {
            List<Map<String, Object>> npcMaps = (List<Map<String, Object>>) body.get("npcs");
            npcs = npcMaps.stream().map(LocationController::mapNpc).collect(java.util.stream.Collectors.toList());
        }

        return ResponseEntity.ok(locationService.createLocation(location, facilities, npcs));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Location location = new Location();
        location.setName((String) body.get("name"));
        location.setArea((String) body.get("area"));
        location.setDescription((String) body.get("description"));
        location.setDefenseValue(body.get("defenseValue") != null ? ((Number) body.get("defenseValue")).intValue() : null);
        location.setManagement((String) body.get("management"));
        location.setOrderNumber(body.get("orderNumber") != null ? ((Number) body.get("orderNumber")).intValue() : null);

        List<LocationFacility> facilities = null;
        if (body.get("facilities") != null) {
            List<Map<String, Object>> facilityMaps = (List<Map<String, Object>>) body.get("facilities");
            facilities = facilityMaps.stream().map(fm -> {
                LocationFacility f = new LocationFacility();
                f.setName((String) fm.get("name"));
                f.setDescription((String) fm.get("description"));
                return f;
            }).collect(java.util.stream.Collectors.toList());
        }

        List<LocationNpc> npcs = null;
        if (body.get("npcs") != null) {
            List<Map<String, Object>> npcMaps = (List<Map<String, Object>>) body.get("npcs");
            npcs = npcMaps.stream().map(LocationController::mapNpc).collect(java.util.stream.Collectors.toList());
        }

        return ResponseEntity.ok(locationService.updateLocation(id, location, facilities, npcs));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLocation(@PathVariable Integer id) {
        return ResponseEntity.ok(locationService.deleteLocation(id));
    }

    private static LocationNpc mapNpc(Map<String, Object> nm) {
        LocationNpc n = new LocationNpc();
        n.setName((String) nm.get("name"));
        n.setJob((String) nm.get("job"));
        if (nm.get("gender") != null) {
            n.setGender(LocationNpc.Gender.valueOf((String) nm.get("gender")));
        }
        n.setIntroduction((String) nm.get("introduction"));
        if (nm.get("locationId") != null) {
            n.setLocationId(((Number) nm.get("locationId")).intValue());
        }
        if (nm.get("attitudeRuler") != null) {
            n.setAttitudeRuler(LocationNpc.Attitude.valueOf((String) nm.get("attitudeRuler")));
        }
        if (nm.get("attitudeRebel") != null) {
            n.setAttitudeRebel(LocationNpc.Attitude.valueOf((String) nm.get("attitudeRebel")));
        }
        if (nm.get("attitudeAdventurer") != null) {
            n.setAttitudeAdventurer(LocationNpc.Attitude.valueOf((String) nm.get("attitudeAdventurer")));
        }
        if (nm.get("attitudeScourge") != null) {
            n.setAttitudeScourge(LocationNpc.Attitude.valueOf((String) nm.get("attitudeScourge")));
        }
        return n;
    }
}
