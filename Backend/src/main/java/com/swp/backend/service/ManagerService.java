package com.swp.backend.service;

import com.swp.backend.exception.BusinessException;
import com.swp.backend.exception.ErrorCode;
import com.swp.backend.entity.Staff;
import com.swp.backend.entity.Vehicle;
import com.swp.backend.repository.RequestDAO;
import com.swp.backend.repository.StaffDAO;
import com.swp.backend.repository.VehicleDAO;
import com.swp.backend.dto.manager.request.CreateStaffRequest;
import com.swp.backend.dto.manager.request.CreateVehicleRequest;
import com.swp.backend.dto.manager.request.UpdateStaffRequest;
import com.rescue.backend.view.dto.manager.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    @Autowired
    private StaffDAO staffDAO;

    @Autowired
    private VehicleDAO vehicleDAO;

    @Autowired
    private RequestDAO requestDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final List<String> VALID_VehicleTypes = List.of("xuồng", "trực thăng", "xe cứu hộ");
    private static final List<String> VALID_ROLES = List.of("điều phối viên", "cứu hộ");
    private static final List<String> VALID_STAFF_STATE = List.of("hoạt động", "không hoạt động");
    private static final List<String> VALID_VEHICLE_STATE = List.of("đang sử dụng", "bảo trì", "không hoạt động");
    private static final int PAGE_SIZE = 1000;

    public Page<StaffResponse> getStaffs(String search, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

        Page<Staff> staffs;

        if (search == null || search.isBlank()) {
            staffs = staffDAO.findAll(pageable);
        } else {
            String keyword = search.trim().toLowerCase();
            staffs = staffDAO.searchByKeyword(keyword, pageable);
        }

        return staffs.map(this::toStaffResponse);
    }

    public StaffResponse getStaffDetail(UUID id) {
        Staff staff = staffDAO.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAFF_NOT_FOUND, id));

        return toStaffResponse(staff);
    }

    private StaffResponse toStaffResponse(Staff staff) {
        boolean isRescueTeam = "cứu hộ".equalsIgnoreCase(staff.getRole().trim());

        return new StaffResponse(
                staff.getId(),
                staff.getName(),
                staff.getPhone(),
                staff.getPassword(),
                staff.getRole(),
                isRescueTeam ? staff.getTeamName() : null,
                isRescueTeam ? staff.getTeamSize() : null,
                isRescueTeam ? staff.getLatitude() : null,
                isRescueTeam ? staff.getLongitude() : null,
                staff.getStaffState()
        );
    }

    public StaffResponse createStaffs(CreateStaffRequest createStaffRequest) {
        String role = createStaffRequest.role();
        if (role == null || !VALID_ROLES.contains(role.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_ROLE);
        }

        if (staffDAO.existsByPhone(createStaffRequest.phone())) {
            throw new BusinessException(ErrorCode.STAFF_ALREADY_EXISTS);
        }

        Staff staff = new Staff();

        staff.setName(createStaffRequest.name());
        staff.setPhone(createStaffRequest.phone());
        staff.setPassword(passwordEncoder.encode(createStaffRequest.password()));

        if ("điều phối viên".equalsIgnoreCase(createStaffRequest.role().trim())) {
            staff.setRole("điều phối viên");
            staff.setTeamName(null);
            staff.setTeamSize(null);
            staff.setLatitude(null);
            staff.setLongitude(null);
        } else {
            staff.setRole("cứu hộ");
            staff.setTeamName(createStaffRequest.teamName());
            staff.setTeamSize(createStaffRequest.teamSize());
            staff.setLatitude(createStaffRequest.latitude());
            staff.setLongitude(createStaffRequest.longitude());
        }

        Staff saved = staffDAO.save(staff);
        return getStaffDetail(saved.getId());

    }

    public StaffResponse updateStaff(UpdateStaffRequest updateStaffRequest, UUID id) {
        Staff staff = staffDAO.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAFF_NOT_FOUND, id));

        String role = updateStaffRequest.role();
        if (role == null || !VALID_ROLES.contains(role.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_ROLE);
        }

        String state = updateStaffRequest.state();
        if (state == null || !VALID_STAFF_STATE.contains(state.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_STAFF_STATE);
        }

        if (!staff.getPhone().equals(updateStaffRequest.phone()) &&
                staffDAO.existsByPhone(updateStaffRequest.phone())) {
            throw new BusinessException(ErrorCode.STAFF_ALREADY_EXISTS);
        }
        staff.setStaffState(state);
        staff.setName(updateStaffRequest.name());
        staff.setPhone(updateStaffRequest.phone());

        if (updateStaffRequest.password() != null && !updateStaffRequest.password().isBlank()) {
            if (!passwordEncoder.matches(updateStaffRequest.password(), staff.getPassword())) {
                staff.setPassword(passwordEncoder.encode(updateStaffRequest.password()));
            }
        }

        if ("điều phối viên".equalsIgnoreCase(updateStaffRequest.role().trim())) {
            staff.setRole("điều phối viên");
            staff.setTeamName(null);
            staff.setTeamSize(null);
            staff.setLatitude(null);
            staff.setLongitude(null);
        } else {
            staff.setRole("cứu hộ");
            staff.setTeamName(updateStaffRequest.teamName());
            staff.setTeamSize(updateStaffRequest.teamSize());
            staff.setLatitude(updateStaffRequest.latitude());
            staff.setLongitude(updateStaffRequest.longitude());
        }

        Staff saved = staffDAO.save(staff);
        return getStaffDetail(saved.getId());

    }

    public StaffResponse deleteStaff(UUID id) {
        if (!staffDAO.existsById(id)) {
            throw new BusinessException(ErrorCode.STAFF_NOT_FOUND, id);
        }

        StaffResponse deletedStaff = getStaffDetail(id);

        staffDAO.deleteById(id);
        return deletedStaff;
    }

    // Vehicle
    public Page<VehicleResponse> getVehicles(String search, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

        Page<Vehicle> vehicles;

        if (search == null || search.isBlank()) {
            vehicles = vehicleDAO.findAll(pageable);
        } else {
            String keyword = search.trim().toLowerCase();
            vehicles = vehicleDAO.searchVehicle(keyword, pageable);
        }

        return vehicles.map(this::toVehicleResponse);
    }

    public VehicleResponse getVehicleDetail(UUID id) {
        Vehicle vehicle = vehicleDAO.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND, id.toString()));

        return toVehicleResponse(vehicle);
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        String ownerName = null;
        UUID ownerId = null;
        if (vehicle.getStaff() != null) {
            ownerName = vehicle.getStaff().getName();
            ownerId = vehicle.getStaff().getId();
        }
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getType(),
                ownerId,
                ownerName,
                vehicle.getState()
        );
    }

    public VehicleResponse createVehicle(CreateVehicleRequest createVehicleRequest) {
        String type = createVehicleRequest.type();
        if (type == null || !VALID_VehicleTypes.contains(type.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_VEHICLE_TYPE);
        }

        Vehicle vehicle = new Vehicle();

        Staff staff = staffDAO.findById(createVehicleRequest.rescueTeamId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STAFF_NOT_FOUND, createVehicleRequest.rescueTeamId()));

        if (!"cứu hộ".equals(staff.getRole())) {
            throw new BusinessException(ErrorCode.INVALID_VEHICLE_OWNER);
        }

        vehicle.setType(createVehicleRequest.type());
        vehicle.setStaff(staff);


        Vehicle saved = vehicleDAO.save(vehicle);
        return getVehicleDetail(saved.getId());
    }

    public VehicleResponse updateVehicle(CreateVehicleRequest createVehicleRequest, UUID vehicleId) {
        Vehicle vehicle = vehicleDAO.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND, vehicleId));

        Staff staff = staffDAO.findById(createVehicleRequest.rescueTeamId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STAFF_NOT_FOUND, createVehicleRequest.rescueTeamId()));

        String newType = createVehicleRequest.type();

        if (newType == null || !VALID_VehicleTypes.contains(newType.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_VEHICLE_TYPE);
        }

        String state = createVehicleRequest.state();
        if (state == null || !VALID_VEHICLE_STATE.contains(state.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_VEHICLE_STATE);
        }

        vehicle.setType(createVehicleRequest.type());
        vehicle.setState(state);
        vehicle.setStaff(staff);

        Vehicle saved = vehicleDAO.save(vehicle);
        return getVehicleDetail(saved.getId());
    }

    public VehicleResponse deleteVehicle(UUID vehicleId) {
        if (!vehicleDAO.existsById(vehicleId)) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND, vehicleId);
        }

        VehicleResponse deletedVehicle = getVehicleDetail(vehicleId);
        vehicleDAO.deleteById(vehicleId);
        return deletedVehicle;
    }

    public List<TeamOwnerResponse> getTeamOwners(String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        return staffDAO.findTeamOwners("cứu hộ", kw)
                .stream()
                .map(row -> new TeamOwnerResponse(
                        (UUID) row[0],
                        (String) row[1]
                ))
                .toList();

    }

    //Rescue team
    public Page<RescueTeamResponse> getRescueTeams(String search, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

        Page<Staff> rescueTeams;

        if (search == null || search.isBlank()) {
            rescueTeams = staffDAO.findAllByRole("cứu hộ", pageable);
        } else {
            rescueTeams = staffDAO.searchByRoleAndKeyword("cứu hộ", search, pageable);
        }

        List<UUID> teamIds = rescueTeams.getContent().stream()
                .map(Staff::getId)
                .toList();

        Map<UUID, Long> requestCountsMap = requestDAO.countRequestsByRescueTeamIds(teamIds)
                .stream()
                .collect(Collectors.toMap(
                        result -> (UUID) result[0],
                        result -> (Long) result[1]
                ));

        return rescueTeams.map(staff -> new RescueTeamResponse(
                staff.getId(),
                staff.getName(),
                staff.getTeamSize(),
                staff.getPhone(),
                staff.getStaffState(),
                requestCountsMap.getOrDefault(staff.getId(), 0L)
        ));
    }

    public DashboardResponse getDashboard() {

        long totalRequests = requestDAO.countBy();

        long completedRequests = requestDAO.countByStatus("hoàn thành");
        double completionRate = (totalRequests == 0)
                ? 0.0
                : Math.round((completedRequests * 100.0 / totalRequests) * 10.0) / 10.0;

        long activeStaffCount = staffDAO.countByStaffState("hoạt động");
        long totalStaff = staffDAO.countBy();
        if (totalStaff == 0) {
            throw new BusinessException(ErrorCode.STAFF_DATA_EMPTY);
        }

        long availableVehicleCount = vehicleDAO.countByState("sẵn sàng");
        long totalVehicles = vehicleDAO.countBy();
        if (totalVehicles == 0) {
            throw new BusinessException(ErrorCode.VEHICLE_DATA_EMPTY);
        }

        // Top 4 đội hiệu suất
        List<Object[]> teamRows = requestDAO.findTop4TeamsByCompletedRequests("hoàn thành");
        if (teamRows == null || teamRows.isEmpty()) {
            throw new BusinessException(ErrorCode.TEAM_PERFORMANCE_EMPTY);
        }
        List<DashboardResponse.TeamPerformance> topTeams = teamRows.stream()
                .map(row -> new DashboardResponse.TeamPerformance(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();

        //  Top 3 thành phố
        List<Object[]> cityRows = requestDAO.findTop3CitiesByRequestCount();
        if (cityRows == null || cityRows.isEmpty()) {
            throw new BusinessException(ErrorCode.CITY_DATA_EMPTY);
        }
        List<DashboardResponse.CityRequest> topCities = cityRows.stream()
                .map(row -> new DashboardResponse.CityRequest(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();

        return new DashboardResponse(
                totalRequests,
                completionRate,
                activeStaffCount,
                totalStaff,
                availableVehicleCount,
                totalVehicles,
                topTeams,
                topCities
        );
    }
}
