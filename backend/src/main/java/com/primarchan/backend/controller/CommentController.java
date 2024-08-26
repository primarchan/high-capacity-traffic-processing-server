package com.primarchan.backend.controller;

import com.primarchan.backend.dto.WriteCommentDto;
import com.primarchan.backend.entity.Comment;
import com.primarchan.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{boardId}/articles/{articleId}/comments")
    public ResponseEntity<Comment> writeComment(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @RequestBody WriteCommentDto writeCommentDto
    ) {
        return ResponseEntity.ok(commentService.writeComment(boardId, articleId, writeCommentDto));
    }

    @PutMapping("/{boardId}/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Comment> writeComment(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @RequestBody WriteCommentDto editCommentDto
    ) {
        return ResponseEntity.ok(commentService.editComment(boardId, articleId, commentId, editCommentDto));
    }

    @DeleteMapping("/{boardId}/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<String> writeComment(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(boardId, articleId, commentId);

        return ResponseEntity.ok("Comment is deleted");
    }

}
