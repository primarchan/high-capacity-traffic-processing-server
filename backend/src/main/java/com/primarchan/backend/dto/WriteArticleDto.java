package com.primarchan.backend.dto;

import lombok.Getter;

@Getter
public class WriteArticleDto {

    private Long boardId;
    private String title;
    private String content;

}
