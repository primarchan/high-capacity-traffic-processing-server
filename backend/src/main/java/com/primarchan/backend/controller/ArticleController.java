package com.primarchan.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.primarchan.backend.dto.EditArticleDto;
import com.primarchan.backend.dto.WriteArticleDto;
import com.primarchan.backend.entity.Article;
import com.primarchan.backend.service.ArticleService;
import com.primarchan.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final AuthenticationManager authenticationManager;
    private final CommentService commentService;

    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(
            @PathVariable Long boardId,
            @RequestBody WriteArticleDto writeArticleDto
     ) throws JsonProcessingException {
        Article article = articleService.writeArticle(boardId, writeArticleDto);

        return ResponseEntity.ok(article);
    }

    @GetMapping("/{boardId}/articles")
    public ResponseEntity<List<Article>> getArticles(
            @PathVariable Long boardId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Long firstId
    ) {
        if (lastId != null) {
            return ResponseEntity.ok(articleService.getOldArticle(boardId, lastId));
        }

        if (firstId != null) {
            return ResponseEntity.ok(articleService.getNewArticle(boardId, firstId));
        }

         return ResponseEntity.ok(articleService.firstGetArticle(boardId));
    }

    @GetMapping("/{boardId}/articles/search")
    public ResponseEntity<List<Article>> searchArticles(
            @PathVariable Long boardId,
            @RequestParam(required = false) String keyword
    ) {
        if (keyword != null) {
            return ResponseEntity.ok(articleService.searchArticle(keyword));
        }

        return ResponseEntity.ok(articleService.firstGetArticle(boardId));
    }

    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> editArticle(
            @PathVariable Long boardId,
            @PathVariable Long articleId,
            @RequestBody EditArticleDto editArticleDto
    ) throws JsonProcessingException {
         return ResponseEntity.ok(articleService.editArticle(boardId, articleId, editArticleDto));
    }

    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<String> deleteArticle(
            @PathVariable Long boardId, @PathVariable Long articleId) throws JsonProcessingException {
        articleService.deleteArticle(boardId, articleId);
        return ResponseEntity.ok("Article is deleted");
    }

    @GetMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> getArticleWithComment(
            @PathVariable Long boardId,
            @PathVariable Long articleId
    ) throws JsonProcessingException {
        CompletableFuture<Article> article = commentService.getArticleWithComment(boardId, articleId);
        return ResponseEntity.ok(article.join());
    }

}
