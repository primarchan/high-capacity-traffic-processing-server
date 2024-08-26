package com.primarchan.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.primarchan.backend.dto.WriteArticleDto;
import com.primarchan.backend.entity.Article;
import com.primarchan.backend.entity.User;
import com.primarchan.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final AuthenticationManager authenticationManager;

     @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(@RequestBody WriteArticleDto writeArticleDto) {
        Article article = articleService.writeArticle(writeArticleDto);

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

}
