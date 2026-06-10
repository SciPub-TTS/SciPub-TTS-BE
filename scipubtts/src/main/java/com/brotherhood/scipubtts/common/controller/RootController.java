package com.brotherhood.scipubtts.common.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<ResponseObject> home() {
        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "ScipubTTS API is running",
                        Map.of(
                                "status", "ok",
                                "message", "Use /swagger-ui/index.html or /api/auth/** for API endpoints"
                        )
                )
        );
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}

