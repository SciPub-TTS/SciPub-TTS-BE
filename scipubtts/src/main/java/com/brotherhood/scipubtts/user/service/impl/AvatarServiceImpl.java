package com.brotherhood.scipubtts.user.service.impl;

import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.service.AvatarService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AvatarServiceImpl implements AvatarService {

    private static final String DICEBEAR_BASE_URL =
            "https://api.dicebear.com/9.x/adventurer/svg";

    @Override
    public String buildDefaultAvatarUrl(User user) {
        return UriComponentsBuilder.fromUriString(DICEBEAR_BASE_URL)
                .queryParam("seed", resolveSeed(user))
                .toUriString();
    }

    private String resolveSeed(User user) {
        if (user.getId() != null) {
            return user.getId().toString();
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail().trim().toLowerCase();
        }

        return "owlreka-default-user";
    }
}
