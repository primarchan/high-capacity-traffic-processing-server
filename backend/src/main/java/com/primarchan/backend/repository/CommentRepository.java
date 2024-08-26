package com.primarchan.backend.repository;

import com.primarchan.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN c.author u WHERE u.username = :username ORDER BY c.createdDate DESC LIMIT 1")
    Comment findLatestCommentOrderByCreatedDate(@Param("username") String username);

    @Query("SELECT c FROM Comment c JOIN c.author u WHERE u.username = :username ORDER BY c.updatedDate DESC LIMIT 1")
    Comment findLatestCommentOrderByUpdatedDate(@Param("username") String username);

    @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false")
    List<Comment> findByArticleId(@Param("articleId") Long articleId);

}
