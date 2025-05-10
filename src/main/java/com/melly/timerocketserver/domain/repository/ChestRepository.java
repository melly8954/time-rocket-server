package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.ChestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChestRepository extends JpaRepository<ChestEntity, Long> {

}
