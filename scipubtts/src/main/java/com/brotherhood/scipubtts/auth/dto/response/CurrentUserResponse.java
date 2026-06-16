package com.brotherhood.scipubtts.auth.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {
    private UUID id;

    private String email;

    private String firstName;

    private String lastName;

    private String role;

    private boolean googleLinked;

    private boolean hasPassword;

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String role() {
        return role;
    }

    public boolean googleLinked() {
        return googleLinked;
    }

    public boolean hasPassword() {
        return hasPassword;
    }
}

