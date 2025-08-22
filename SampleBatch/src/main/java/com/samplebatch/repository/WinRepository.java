package com.samplebatch.repository;

import com.samplebatch.entity.WinEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WinRepository extends JpaRepository<WinEntity, Long> {

    /**
     * org.springframework.data.domain.Page의 page
     * */
    Page<WinEntity> findByWinGreaterThanEqual(Long win, Pageable pageable);
}
