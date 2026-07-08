package com.brotherhood.scipubtts.user.service.impl;

import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.service.AvatarService;
import com.brotherhood.scipubtts.user.support.AvatarConstants;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AvatarServiceImpl implements AvatarService {

    @Override
    public String buildDefaultAvatarUrl(User user) {
        return UriComponentsBuilder.fromUriString(AvatarConstants.DICEBEAR_ADVENTURER_AVATAR_URL)
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
