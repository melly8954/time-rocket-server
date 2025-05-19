package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RocketFileRepository extends JpaRepository<RocketFileEntity,Long> {
}
