package com.swp.backend.controller.common;

import com.swp.backend.service.ChatService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common/chat")
public class ChatController {

    private ChatService chatService;


//    @GetMapping("/{requestId}")
//    public ResponseEntity<ResponseObject> getHistory(@PathVariable UUID requestId) {
//        ChatHistoryResponse history = chatService.getChatHistory(requestId);
//        return ResponseEntity.ok(new ResponseObject(200, "Tải lịch sử chat thành công", history));
//    }
}
