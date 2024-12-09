package com.baeldung.service;

import com.baeldung.domain.Article;
import com.baeldung.repository.ArticleRepository;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublishArticleService implements JavaDelegate {
    @Autowired
    private ArticleRepository articleRepository;

    public void execute(DelegateExecution execution) {
        Article article = (Article) execution.getVariable("article");
        articleRepository.save(article);
    }
}
