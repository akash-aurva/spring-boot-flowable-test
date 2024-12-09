package com.baeldung.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baeldung.repository.ArticleRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.domain.Approval;
import com.baeldung.domain.Article;

@Service
public class ArticleWorkflowService {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void startProcess(Article article) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("author", article.getAuthor());
        variables.put("url", article.getUrl());

        ProcessInstance articleReview = runtimeService.startProcessInstanceByKey("articleReview", variables);
        article.setId(articleReview.getId());
        articleRepository.saveAndFlush(article);
        updateArticle(articleReview.getId(), article);
    }

    @Transactional
    public List<Article> getTasks(String assignee) {
        List<Task> tasks = taskService.createTaskQuery()
          .taskCandidateGroup(assignee)
          .list();

        return tasks.stream()
          .map(task -> {
              Map<String, Object> variables = taskService.getVariables(task.getId());
              return new Article(
                task.getId(), (String) variables.get("author"), (String) variables.get("url"));
          })
          .collect(Collectors.toList());
    }

    @Transactional
    public void submitReview(Approval approval) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", approval.isStatus());
        taskService.complete(approval.getId(), variables);
    }

    @Transactional
    public void updateArticle(String id, Article updatedArticle) {
        PreparedStatementCreator creator = con -> {
            PreparedStatement updatedStmt = con.prepareStatement(
                    "UPDATE articles SET author = ?, url = ? WHERE id = ?");
            updatedStmt.setString(1, updatedArticle.getAuthor() + "_updated_test");
            updatedStmt.setString(2, updatedArticle.getUrl());
            updatedStmt.setString(3, id);
            return updatedStmt;
        };
        jdbcTemplate.update(creator);
    }
}