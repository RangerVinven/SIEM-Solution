package siem.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import siem.account.dto.CreateLocationRequest;
import siem.account.dto.UpdateLocationRequest;
import siem.account.entity.Location;
import siem.account.repository.LocationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository repository;

    public List<Location> getLocationsForSchool(String schoolId) {
        return repository.findBySchoolId(UUID.fromString(schoolId));
    }

    public Location getLocation(String schoolId, UUID id) {
        Location location = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        if (!location.getSchoolId().equals(UUID.fromString(schoolId))) {
            throw new AccessDeniedException("Forbidden");
        }

        return location;
    }

    public Location createLocation(String schoolId, CreateLocationRequest request) {
        Location location = new Location();

        location.setSchoolId(UUID.fromString(schoolId));
        location.setName(request.getName());
        location.setDepartment(request.getDepartment());
        location.setRoomNumber(request.getRoomNumber());

        return repository.save(location);
    }

    public Location updateLocation(String schoolId, UUID id, UpdateLocationRequest request) {
        Location location = getLocation(schoolId, id);

        location.setName(request.getName());
        location.setDepartment(request.getDepartment());
        location.setRoomNumber(request.getRoomNumber());

        return repository.save(location);
    }

    public void deleteLocation(String schoolId, UUID id) {
        repository.delete(getLocation(schoolId, id));
    }
}
