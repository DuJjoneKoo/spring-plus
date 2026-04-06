package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;

import java.util.Optional;

// QueryDSL 커스텀 메소드를 정의하는 인터페이스
// TodoRepository가 이 인터페이스를 상속받아 사용함
public interface TodoRepositoryCustom {
    Optional<Todo> findByIdWithUser(Long todoId);
}