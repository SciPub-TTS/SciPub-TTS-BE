package com.brotherhood.scipubtts.auth.service;

import com.brotherhood.scipubtts.auth.dto.RegisterLocalRequest;
import jakarta.transaction.Transactional;

public interface AuthService {

    String registerLocal(RegisterLocalRequest request);

    String verifyEmail(String rawToken);

    String loginLocal(String email, String password);
}
