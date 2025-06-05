package com.twitter.demo.entities.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateCommentDto {
    private String message;
    private UUID postId;
}
