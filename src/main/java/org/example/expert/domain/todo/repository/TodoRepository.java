package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

// [수정] TodoRepositoryCustom 추가 상속
// QueryDSL로 구현된 findByIdWithUser를 사용하기 위해 커스텀 인터페이스 상속
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    // [제거] findByIdWithUser는 이제 TodoRepositoryImpl에서 QueryDSL로 구현됨
    // 기존 JPQL 쿼리 삭제

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u " +
            "WHERE (:weather IS NULL OR t.weather = :weather) " +
            "AND (:startDate IS NULL OR t.modifiedAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.modifiedAt <= :endDate) " +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findByConditions(
            @Param("weather") String weather,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}