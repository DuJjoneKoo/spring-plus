package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        // [변경] 기존 JPQL -> QueryDSL로 변경
        // 기존: LEFT JOIN t.user (FETCH 없어서 N+1 발생 가능)
        // 변경: fetchJoin()으로 Todo와 User를 한 번의 쿼리로 함께 조회
        Todo todo = queryFactory
                .selectFrom(QTodo.todo)
                .leftJoin(QTodo.todo.user, QUser.user).fetchJoin()
                .where(QTodo.todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(todo);
    }
}