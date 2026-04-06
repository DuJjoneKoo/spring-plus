package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // [수정] JOIN -> JOIN FETCH로 변경
    // 기존: JOIN만 사용하면 Comment만 조회하고 User는 나중에 접근할 때마다
    //       별도 쿼리가 실행됨 -> Comment가 N개면 쿼리가 N+1번 실행되는 N+1 문제 발생
    // 변경: JOIN FETCH를 사용하면 Comment와 User를 한 번의 쿼리로 함께 조회하여
    //       N+1 문제가 해결됨
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
}