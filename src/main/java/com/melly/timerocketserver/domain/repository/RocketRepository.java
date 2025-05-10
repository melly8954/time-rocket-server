package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.RocketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RocketRepository extends JpaRepository<RocketEntity, Long> {
}
