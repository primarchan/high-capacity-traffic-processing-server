package com.primarchan.backend.service;

import com.primarchan.backend.dto.WriteArticleDto;
import com.primarchan.backend.entity.Article;
import com.primarchan.backend.entity.Board;
import com.primarchan.backend.entity.User;
import com.primarchan.backend.exception.ResourceNotFoundException;
import com.primarchan.backend.repository.ArticleRepository;
import com.primarchan.backend.repository.BoardRepository;
import com.primarchan.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;

    public Article writeArticle(WriteArticleDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        Board board = boardRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Article article = new Article();
        article.setBoard(board);
        article.setAuthor(author);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        articleRepository.save(article);

        return article;
    }

}
