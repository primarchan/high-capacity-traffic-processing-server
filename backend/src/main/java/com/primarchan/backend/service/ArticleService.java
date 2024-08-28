package com.primarchan.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primarchan.backend.dto.EditArticleDto;
import com.primarchan.backend.dto.WriteArticleDto;
import com.primarchan.backend.entity.Article;
import com.primarchan.backend.entity.Board;
import com.primarchan.backend.entity.User;
import com.primarchan.backend.exception.ForbiddenException;
import com.primarchan.backend.exception.RateLimitException;
import com.primarchan.backend.exception.ResourceNotFoundException;
import com.primarchan.backend.repository.ArticleRepository;
import com.primarchan.backend.repository.BoardRepository;
import com.primarchan.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final ElasticSearchService elasticSearchService;

    @Transactional
    public Article writeArticle(Long boardId, WriteArticleDto dto) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (!this.isCanWriteArticle()) {
            throw new RateLimitException("Article not written by rate limit");
        }

        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Article article = new Article();
        article.setBoard(board);
        article.setAuthor(author);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        articleRepository.save(article);

        this.indexArticle(article);

        return article;
    }

    public List<Article> firstGetArticle(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public List<Article> getOldArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public List<Article> getNewArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, articleId);
    }



    @Transactional
    public Article editArticle(Long boardId, Long articleId, EditArticleDto dto) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        if (article.getAuthor() != author) {
            throw new ForbiddenException("Article author different");
        }

        if (!this.isCanEditArticle()) {
            throw new RateLimitException("Article not edited by rate limit");
        }

        if (dto.getTitle() != null) {
            article.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            article.setContent(dto.getContent());
        }

        articleRepository.save(article);
        this.indexArticle(article);

        return article;
    }

    @Transactional
    public boolean deleteArticle(Long boardId, Long articleId) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        if (article.getAuthor() != author) {
            throw new ForbiddenException("Article author different");
        }

        if (!this.isCanEditArticle()) {
            throw new RateLimitException("Article not edited by rate limit");
        }

        article.setIsDeleted(true);
        articleRepository.save(article);
        this.indexArticle(article);

        return true;
    }

    private boolean isCanWriteArticle() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByCreatedDate(userDetails.getUsername());
        if (latestArticle == null) {
            return true;
        }
        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getCreatedDate());
    }

    private boolean isCanEditArticle() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByUpdatedDate(userDetails.getUsername());
        if (latestArticle == null || latestArticle.getUpdatedDate() == null) {
            return true;
        }
        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getUpdatedDate());
    }

    private boolean isDifferenceMoreThanFiveMinutes(LocalDateTime localDateTime) {
        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > 5;
    }

    public String indexArticle(Article article) throws JsonProcessingException {
        String articleJson = objectMapper.writeValueAsString(article);

        return elasticSearchService.indexArticleDocument(article.getId().toString(), articleJson).block();
    }

    public List<Article> searchArticle(String keyword) {
        Mono<List<Long>> articleIds = elasticSearchService.articleSearch(keyword);

        try {
            return articleRepository.findAllById(articleIds.toFuture().get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
