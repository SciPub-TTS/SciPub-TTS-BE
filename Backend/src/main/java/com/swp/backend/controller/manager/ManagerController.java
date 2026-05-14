package com.swp.backend.controller.manager;

import com.swp.backend.service.ManagerService;
import com.swp.backend.dto.common.ResponseObject;
import com.swp.backend.dto.manager.request.CreateStaffRequest;
import com.swp.backend.dto.manager.request.CreateVehicleRequest;
import com.swp.backend.dto.manager.request.UpdateStaffRequest;
import com.rescue.backend.view.dto.manager.response.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("manager")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/staff")
    public ResponseEntity<ResponseObject> getStaff(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page) {
        Page<StaffResponse> staffs = managerService.getStaffs(search, page);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Lấy danh sách nhân viên thành công", staffs));
//        try {
//            Page<StaffResponse> staffs = managerService.getStaffs(search, page);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Lấy danh sách nhân viên thành công", staffs));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        }
    }

    @PostMapping(value = "/staff")
    public ResponseEntity<ResponseObject> createStaff(
            @RequestBody CreateStaffRequest request) {
        StaffResponse staffs = managerService.createStaffs(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject(201, "Tạo nhân viên thành công", staffs));
//        try {
//            StaffResponse staffs = managerService.createStaffs(request);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new ResponseObject(201, "Tạo nhân viên thành công", staffs));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        }
    }

    @PatchMapping(value = "/staff/{id}")
    public ResponseEntity<ResponseObject> updateStaff(
            @PathVariable UUID id,
            @RequestBody UpdateStaffRequest request
    ) {
        StaffResponse staffs = managerService.updateStaff(request, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Cập nhật nhân viên thành công", staffs));
//        try {
//            StaffResponse staffs = managerService.updateStaff(request, id);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Cập nhật nhân viên thành công", staffs));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ResponseObject(404, e.getMessage(), null));
//        }
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<ResponseObject> deleteStaff(
            @PathVariable UUID id
    ) {
        StaffResponse staffs = managerService.deleteStaff(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Xóa nhân viên thành công", staffs));
//        try {
//            StaffResponse staffs = managerService.deleteStaff(id);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Xóa nhân viên thành công", staffs));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        }
    }

    @GetMapping("/vehicles")
    public ResponseEntity<ResponseObject> getVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page) {
        Page<VehicleResponse> vehicles = managerService.getVehicles(search, page);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Lấy danh sách phương tiện thành công", vehicles));
//        try {
//            Page<VehicleResponse> vehicles = managerService.getVehicles(search, page);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Lấy danh sách phương tiện thành công", vehicles));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        }
    }

    @PostMapping(value = "/vehicles")
    public ResponseEntity<ResponseObject> createVehicle(
            @RequestBody CreateVehicleRequest request) {
        VehicleResponse vehicles = managerService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject(201, "Thêm phương tiện thành công", vehicles));
//        try {
//            VehicleResponse vehicles = managerService.createVehicle(request);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new ResponseObject(201, "Thêm phương tiện thành công", vehicles));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        }
    }

    @PatchMapping(value = "/vehicles/{id}")
    public ResponseEntity<ResponseObject> updateVehicle(
            @PathVariable UUID id,
            @RequestBody CreateVehicleRequest request) {
        VehicleResponse vehicles = managerService.updateVehicle(request, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Cập nhật phương tiện thành công", vehicles));
//        try {
//            VehicleResponse vehicles = managerService.updateVehicle(request, id);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Cập nhật phương tiện thành công", vehicles));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ResponseObject(404, e.getMessage(), null));
//        }
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<ResponseObject> deleteVehicle(
            @PathVariable UUID id
    ) {
        VehicleResponse vehicles = managerService.deleteVehicle(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Xóa phương tiện thành công", vehicles));
//        try {
//            VehicleResponse vehicles = managerService.deleteVehicle(id);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Xóa phương tiện thành công", vehicles));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        }
    }

    @GetMapping("/vehicles/owner")
    public ResponseEntity<ResponseObject> getRescueTeam(
            @RequestParam(required = false) String search
    ) {
        List<TeamOwnerResponse> rescueTeams = managerService.getTeamOwners(search);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Lấy danh sách đội cứu hộ thành công", rescueTeams));
//        try {
//            List<TeamOwnerResponse> rescueTeams = managerService.getTeamOwners(search);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Lấy danh sách đội cứu hộ thành công", rescueTeams));
//        } catch (Exception e) {
//            String errorMsg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống khi lấy danh sách đội cứu hộ";
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, errorMsg, null));
//        }
    }

    @GetMapping("/rescueteam/")
    public ResponseEntity<ResponseObject> getRescueTeam(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<RescueTeamResponse> rescueTeams = managerService.getRescueTeams(search, page);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseObject(200, "Lấy danh sách đội cứu hộ thành công", rescueTeams));
//        try {
//            Page<RescueTeamResponse> rescueTeams = managerService.getRescueTeams(search, page);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseObject(200, "Lấy danh sách đội cứu hộ thành công", rescueTeams));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseObject(400, e.getMessage(), null));
//        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ResponseObject> getDashboard() {
        DashboardResponse data = managerService.getDashboard();
        return ResponseEntity.ok(
                new ResponseObject(200, "Lấy bảng thống kê thành công", data)
        );
//        try {
//            DashboardResponse data = managerService.getDashboard();
//            return ResponseEntity.ok(
//                    new ResponseObject(200, "Lấy bảng thống kê thành công", data)
//            );
//        } catch (IllegalStateException e) {
//            // Lỗi thống kê nội bộ (data trống, tính toán sai)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new ResponseObject(400, e.getMessage(), null)
//            );
//        } catch (Exception e) {
//            // Lỗi bất ngờ (DB down, null pointer,...)
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject(500, "Lỗi hệ thống: " + e.getMessage(), null)
//            );
//        }
    }

}
