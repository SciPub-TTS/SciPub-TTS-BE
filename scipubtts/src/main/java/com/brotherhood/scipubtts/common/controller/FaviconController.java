package com.brotherhood.scipubtts.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> disableFavicon() {
        // Trả về 204 No Content để trình duyệt biết là không có icon và dừng lại
        return ResponseEntity.noContent().build();
    }
}