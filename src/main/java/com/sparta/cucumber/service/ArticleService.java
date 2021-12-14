package com.sparta.cucumber.service;

import com.sparta.cucumber.dto.ArticleRequestDto;
import com.sparta.cucumber.models.Article;
import com.sparta.cucumber.models.User;
import com.sparta.cucumber.repository.ArticleRepository;
import com.sparta.cucumber.repository.UserRepository;
import com.sparta.cucumber.utils.LocationDistance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ArticleService {
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final LocationDistance location;

    // S3에 사진 업로드 가능한 메소드
    @Transactional
    public Article upload(ArticleRequestDto requestDto, String imagePath) {
        Long userId = requestDto.getUserId();
        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new NullPointerException("잘못된 접근입니다."));
        String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);

        Article article = Article.builder()
                .user(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .imagePath(imagePath)
                .imageName(imageName)
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();

        return articleRepository.save(article);
    }

    public List<Article> getArticles(String query) {
        return articleRepository.findAllByTitleContainsOrderByCreatedAtDesc(query);
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Article seeDetailOfArticle(Long articleId) {
        return articleRepository
                .findById(articleId)
                .orElseThrow(NullPointerException::new);
    }

    public List<Article> getAroundArticle(Double lat, Double lon) {
        return articleRepository
                .findAll()
                .stream()
                .filter(article -> {
                    double dist = location.distance(
                            lat, lon,
                            article.getLatitude(), article.getLongitude(),
                            "meter");
                    return dist < 1000;
                }).collect(Collectors.toList());
    }

    public List<Article> getUsersArticles(@PathVariable("id") Long userId) {
        return articleRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
    }


    public Long removeArticle(Long articleId) {
        Article article = articleRepository
                .findById(articleId)
                .orElseThrow(
                        () -> new NullPointerException("게시물이 존재하지 않습니다."));
        articleRepository.delete(article);
        return articleId;
    }

    @Transactional
    public Article update(ArticleRequestDto requestDto) {
        Long userId = requestDto.getUserId();
        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new NullPointerException("잘못된 접근입니다."));
        Article article = articleRepository.findById(requestDto.getId()).orElse(null);
        if (article != null) {
            if (article.getUser() == user) {
                return article.update(requestDto);
            }
        }
        return article;
    }
}
