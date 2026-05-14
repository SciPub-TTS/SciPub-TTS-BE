package com.swp.backend.service;


import com.swp.backend.exception.BusinessException;
import com.swp.backend.exception.ErrorCode;
import com.swp.backend.entity.Message;
import com.swp.backend.entity.Request;
import com.swp.backend.entity.Staff;
import com.swp.backend.entity.Vehicle;
import com.swp.backend.repository.MessageDAO;
import com.swp.backend.repository.RequestDAO;
import com.swp.backend.repository.StaffDAO;
import com.swp.backend.repository.VehicleDAO;
import com.swp.backend.dto.chat.response.MessageResponse;
import com.swp.backend.dto.coordinator.request.TakeListRequest;
import com.swp.backend.dto.coordinator.request.UpdateRequest;
import com.rescue.backend.view.dto.coordinator.response.*;
import com.swp.backend.dto.image.response.CoordinatorImageResponse;
import com.swp.backend.dto.vehicle.request.FilterVehicleRequest;
import com.swp.backend.dto.vehicle.response.FilterVehicleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class DispatchService {

    @Autowired
    private RequestDAO requestDAO;


    @Autowired
    private VehicleDAO vehicleDAO;

    @Autowired
    private StaffDAO staffDAO;

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private ChatService chatService;

    private static final int PAGE_SIZE = 1000;
    private static final List<String> VALID_VEHICLE_TYPES =
            List.of("xuồng", "xe cứu hộ", "trực thăng");
    private static final List<String> VALID_URGENCY_TYPES =
            List.of("cao", "trung bình", "thấp");

    public TakePageResponse getRequestCitizen(TakeListRequest takeListRequest) {
        Page<TakeListResponse> page =
                requestDAO.getRequestCitizen(takeListRequest.status(), PageRequest.of(takeListRequest.pageNumber(), takeListRequest.pageSize()));

        return new TakePageResponse(page.getTotalPages(), page.getContent());
    }

    public SpecificResponse getSpecificRequest(UUID id) {
        SpecificResponse response = requestDAO.findRequestDetail(id);

        if (response == null) {
            throw new BusinessException(ErrorCode.REQUEST_NOT_FOUND, id.toString());
        }

        return response;
    }

//    @Transactional
//    public boolean updateRequest(UpdateMissionReqeuest req) {
//
//        int vehicleUpdated =
//                vehicleDAO.setVehicle(req.vehicleId(), req.vehicleState());
//
//        if (vehicleUpdated == 0) {
//            return false;
//        }
//
//        int requestUpdated = requestDAO.updateRequest(
//                req.id(),
//                req.status(),
//                req.urgency(),
//                req.rescueTeamId(),
//                req.vehicleId()
//        );
//
//        if (requestUpdated == 0) {
//            vehicleDAO.setVehicle(req.vehicleId(), "free");
//            throw new RuntimeException("Update request failed");
//        }
//
//        return true;
//    }

    public List<FilterVehicleResponse> filterVehicleByType(FilterVehicleRequest filterVehicleRequest) {
        return vehicleDAO.filterVehicleByType(filterVehicleRequest.vehicle_type());
    }

    public Page<RequestListResponse> getRequests(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Request> requests;

        if (status == null || status.isBlank()) {
            requests = requestDAO.findAll(pageable);
        } else {
            String cleanStatus = switch (status.trim().toLowerCase()) {
                case "yêu cầu mới", "new" -> "yêu cầu mới";
                case "đang xử lý", "processing" -> "đang xử lý";
                case "tạm hoãn", "delayed" -> "tạm hoãn";
                case "hoàn thành", "completed" -> "hoàn thành";
                case "đã huỷ", "rejected" -> "đã huỷ";
                default -> throw new BusinessException(ErrorCode.INVALID_STATUS, status);
            };
            requests = requestDAO.findAllByStatus(cleanStatus, pageable);
        }

        return requests.map(r -> new RequestListResponse(
                r.getId(),
                r.getCitizen().getName(),
                r.getCitizen().getPhone(),
                r.getStatus(),
                r.getCreatedAt()
        ));
    }

    public RequestDetailResponse getRequestDetail(UUID requestId) {
        Request r = requestDAO.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND, requestId.toString()));

        List<CoordinatorImageResponse> images =
                (r.getImages() != null)
                        ? r.getImages().stream()
                        .map(img -> new CoordinatorImageResponse(img.getId(), img.getImageUrl()))
                        .toList()
                        : List.of();

        String vehicleType = (r.getVehicle() != null) ? r.getVehicle().getType() : null;
        String rescueTeamName = null;
        BigDecimal rescueTeamLatitude = null;
        BigDecimal rescueTeamLongitude = null;

        if (r.getRescueTeam() != null) {
            rescueTeamName = r.getRescueTeam().getTeamName();
            rescueTeamLatitude = r.getRescueTeam().getLatitude();
            rescueTeamLongitude = r.getRescueTeam().getLongitude();
        }

        return new RequestDetailResponse(
                r.getId(),
                r.getStatus(),
                r.getUrgency(),
                r.getCitizen().getName(),
                r.getCitizen().getPhone(),
                r.getAddress(),
                r.getLatitude(),
                r.getLongitude(),
                r.getDescription(),
                r.getAdditionalLink(),
                images,
                vehicleType,
                rescueTeamName,
                rescueTeamLatitude,
                rescueTeamLongitude
        );
    }

    public List<NearbyTeamResponse> getNearbyTeams(UUID requestId, String vehicleType) {
        if (vehicleType == null
                || !VALID_VEHICLE_TYPES.contains(vehicleType.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_VEHICLE_TYPE, vehicleType);
        }

        String trimmedVehicleType = vehicleType.trim().toLowerCase();

        Request request = requestDAO.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND, requestId.toString()));

        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new BusinessException(ErrorCode.REQUEST_LOCATION_MISSING);
        }

        double lat = request.getLatitude().doubleValue();
        double lng = request.getLongitude().doubleValue();

        List<Object[]> rows = staffDAO.findTop4NearbyTeams(
                lat, lng, trimmedVehicleType);

        if (rows == null || rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_NEARBY_TEAM_FOUND, trimmedVehicleType);
        }

        return rows.stream()
                .map(row -> new NearbyTeamResponse(
                        toUuid(row[0]),
                        (String) row[1]
                ))
                .toList();
    }

    private UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }

    public RequestDetailResponse updateRequest(UUID requestID, UUID coordinatorID, UpdateRequest dto) {
        Request request = requestDAO.findById(requestID)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.REQUEST_NOT_FOUND,
                        requestID.toString()
                ));


        if (dto.status() != null) {
            String newStatus = switch (dto.status().trim().toLowerCase()) {
                case "đang xử lý", "processing" -> "đang xử lý";
                case "đã huỷ", "rejected" -> "đã huỷ";
                default -> throw new BusinessException(ErrorCode.INVALID_STATUS_UPDATE);
            };

            if ("đã huỷ".equals(newStatus)) {
                if ("hoàn thành".equals(request.getStatus())) {
                    throw new BusinessException(ErrorCode.REQUEST_ALREADY_COMPLETED);
                }
                if ("đã huỷ".equals(request.getStatus())) {
                    return getRequestDetail(request.getId());
                }
                request.setStatus("đã huỷ");
                requestDAO.save(request);
                return getRequestDetail(request.getId());
            }

            if (!"yêu cầu mới".equals(request.getStatus())) {
                // Nếu không phải huỷ, mà muốn cập nhật xe/đội cứu hộ, thì mới chặn lại
                throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, request.getStatus());
            }

            request.setStatus(newStatus);
        }

        String urgency = dto.urgency();

        if (urgency == null || !VALID_URGENCY_TYPES.contains(urgency.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_URGENCY);
        }
        String normalizedUrgency = urgency.trim().toLowerCase();
        request.setUrgency(normalizedUrgency);

        Staff rescueTeam = staffDAO.findById(dto.rescueTeamID())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESCUE_TEAM_NOT_FOUND,
                        dto.rescueTeamID()
                ));
        request.setRescueTeam(rescueTeam);

        Staff coordinator = staffDAO.findById(coordinatorID)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COORDINATOR_NOT_FOUND,
                        coordinatorID
                ));
        request.setCoordinator(coordinator);

        String type = dto.vehicleType();
        if (type == null || !VALID_VEHICLE_TYPES.contains(type.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_VEHICLE_TYPE);
        }
        String normalizedVehicleType = type.trim().toLowerCase();

        Vehicle vehicle = vehicleDAO
                .findAvailableVehicle(dto.rescueTeamID(), normalizedVehicleType, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new  BusinessException(ErrorCode.VEHICLE_NOT_AVAILABLE));
        vehicle.setState("đang sử dụng");

        request.setVehicle(vehicle);

        requestDAO.save(request);
        vehicleDAO.save(vehicle);
        return getRequestDetail(request.getId());
    }

    @Transactional
    public MessageResponse sendMessage(
            UUID requestId,
            UUID senderId,
            String content,
            LocalDateTime sendAt
    ) {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_ID);
        }
        if (senderId == null) {
            throw new BusinessException(ErrorCode.INVALID_SENDER_ID);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        Request request = requestDAO.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND, requestId));

        Staff sender = staffDAO.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COORDINATOR_NOT_FOUND, senderId));
        if (sender.getRole() == null || !sender.getRole().trim().equalsIgnoreCase("điều phối viên")) {
            throw new BusinessException(ErrorCode.INVALID_COORDINATOR_ROLE);
        }

        Message message = new Message();
        message.setRequest(request);
        message.setSenderId(senderId);
        message.setSenderRole("điều phối viên");
        message.setContent(content.trim());
        message.setSendAt(sendAt != null ? sendAt : LocalDateTime.now());

        Message saved = messageDAO.save(message);

        return new MessageResponse(
                saved.getId(),
                senderId,
                sender.getName(),
                saved.getSenderRole(),
                saved.getContent(),
                saved.getSendAt()
        );
    }

    public List<MessageResponse> takeAllMessageOfRequest(UUID requestId) {
        return chatService.takeAllMessageOfRequest(requestId);
    }
}
