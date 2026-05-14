package com.swp.backend.service;

import com.swp.backend.exception.BusinessException;
import com.swp.backend.exception.ErrorCode;
import com.swp.backend.entity.Staff;
import com.swp.backend.repository.StaffDAO;
import com.swp.backend.dto.auth.request.LoginRequest;
import com.swp.backend.dto.auth.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String RESCUE_TEAM = "cứu hộ";

    @Autowired
    private StaffDAO staffDAO;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {

        Staff staff = staffDAO.findByPhone(loginRequest.phone())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!bCryptPasswordEncoder.matches(loginRequest.password(), staff.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        boolean isRescueTeam = RESCUE_TEAM.equalsIgnoreCase(staff.getRole());

        return new LoginResponse(
                staff.getId(),
                staff.getPhone(),
                staff.getRole(),
                staff.getName(),
                isRescueTeam ? staff.getTeamName() : null,
                isRescueTeam ? staff.getTeamSize() : null,
                isRescueTeam ? staff.getLatitude() : null,
                isRescueTeam ? staff.getLongitude() : null
        );
    }
}