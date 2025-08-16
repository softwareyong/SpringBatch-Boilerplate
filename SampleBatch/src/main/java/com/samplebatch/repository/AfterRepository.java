package com.samplebatch.repository;

import com.samplebatch.entity.AfterEntity;
import com.samplebatch.entity.BeforeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AfterRepository extends JpaRepository<AfterEntity, Long> {
}
