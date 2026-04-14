package com.green.eats.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass //Entity 부모 역할
@EntityListeners(AuditingEntityListener.class) //MySQL로 치면 CurrentTimestamp 역할
public class CreatedAt {
    @CreatedDate // insert시 현재 일시 값이 삽입
    @Column(nullable = false) // 컬럼의 속성값을 줄때 사용. NOT NULL
    private LocalDateTime createdAt; // 타입, 이름으로 컬럼이 된다. LocalDateTime > DATETIME, createdAt > created_at
}