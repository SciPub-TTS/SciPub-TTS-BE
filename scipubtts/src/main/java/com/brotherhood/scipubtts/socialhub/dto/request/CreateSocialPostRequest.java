package com.brotherhood.scipubtts.socialhub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSocialPostRequest(
@NotBlank(message = "Title is required")
@Size(min = 10, max = 300, message = "Title must be between 10 and 300 characters")
String title,

String body,

@Size(max = 500, message = "Topic tag cannot exceed 500 characters")
String topicTag,

// Tối đa 3 — validate ở đây + kiểm tra lại trong Service
@Size(max = 3, message = "Maximum 3 references per post")
@Valid
List<String> references
) {}
