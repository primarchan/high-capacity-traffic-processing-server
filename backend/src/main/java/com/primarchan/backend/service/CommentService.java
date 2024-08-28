package com.primarchan.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primarchan.backend.entity.Article;
import com.primarchan.backend.entity.Board;
import com.primarchan.backend.entity.Comment;
import com.primarchan.backend.entity.User;
import com.primarchan.backend.exception.ForbiddenException;
import com.primarchan.backend.exception.RateLimitException;
import com.primarchan.backend.exception.ResourceNotFoundException;
import com.primarchan.backend.repository.ArticleRepository;
import com.primarchan.backend.repository.BoardRepository;
import com.primarchan.backend.repository.CommentRepository;
import com.primarchan.backend.repository.UserRepository;
import com.primarchan.backend.dto.WriteCommentDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ElasticSearchService elasticSearchService;

    @Transactional
    public Comment writeComment(Long boardId, Long articleId, WriteCommentDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (!this.isCanWriteComment()) {
            throw new RateLimitException("Comment not written by rate limit");
        }

        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        if (article.getIsDeleted()) {
            throw new ForbiddenException("Article is deleted");
        }

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor(author);
        comment.setContent(dto.getContent());

        commentRepository.save(comment);

        return comment;
    }

    @Transactional
    public Comment editComment(Long boardId, Long articleId, Long commentId, WriteCommentDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (!this.isCanEditComment()) {
            throw new RateLimitException("comment not written by rate limit");
        }

        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        if (article.getIsDeleted()) {
            throw new ForbiddenException("Article is deleted");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (comment.getAuthor() != author) {
            throw new ForbiddenException("Comment author different");
        }

        if (dto.getContent() != null) {
            comment.setContent(dto.getContent());
        }

        commentRepository.save(comment);

        return comment;
    }

    public boolean deleteComment(Long boardId, Long articleId, Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (!this.isCanEditComment()) {
            throw new RateLimitException("comment not written by rate limit");
        }
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);
        Optional<Article> article = articleRepository.findById(articleId);
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("article not found");
        }
        if (article.get().getIsDeleted()) {
            throw new ForbiddenException("article is deleted");
        }
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isEmpty() || comment.get().getIsDeleted()) {
            throw new ResourceNotFoundException("comment not found");
        }
        if (comment.get().getAuthor() != author.get()) {
            throw new ForbiddenException("comment author different");
        }
        comment.get().setIsDeleted(true);
        commentRepository.save(comment.get());
        return true;
    }

    private boolean isCanWriteComment() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Comment latestComment = commentRepository.findLatestCommentOrderByCreatedDate(userDetails.getUsername());
        if (latestComment == null) {
            return true;
        }
        return this.isDifferenceMoreThanOneMinutes(latestComment.getCreatedDate());
    }

    private boolean isCanEditComment() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Comment latestComment = commentRepository.findLatestCommentOrderByCreatedDate(userDetails.getUsername());
        if (latestComment == null || latestComment.getUpdatedDate() == null) {
            return true;
        }
        return this.isDifferenceMoreThanOneMinutes(latestComment.getUpdatedDate());
    }

    private boolean isDifferenceMoreThanOneMinutes(LocalDateTime localDateTime) {
        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > 1;
    }

    @Async
    @Transactional
    protected CompletableFuture<Article> getArticle(Long boardId, Long articleId) throws JsonProcessingException {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        String articleJson = objectMapper.writeValueAsString(article);
        elasticSearchService.indexArticleDocument(article.getId().toString(), articleJson).block();

        return CompletableFuture.completedFuture(article);
    }

    @Async
    protected CompletableFuture<List<Comment>> getComments(Long articleId) {
        return CompletableFuture.completedFuture(commentRepository.findByArticleId(articleId));
    }

    public CompletableFuture<Article> getArticleWithComment(Long boardId, Long articleId) throws JsonProcessingException {
        CompletableFuture<Article> articleFuture = this.getArticle(boardId, articleId);
        CompletableFuture<List<Comment>> commentsFuture = this.getComments(articleId);

        return CompletableFuture.allOf(articleFuture, commentsFuture)
                .thenApply(voidResult -> {
                    try {
                        Article article = articleFuture.get();
                        List<Comment> comments = commentsFuture.get();
                        article.setComments(comments);
                        return article;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

}
