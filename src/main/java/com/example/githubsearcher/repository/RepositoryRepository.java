package com.example.githubsearcher.repository;

import com.example.githubsearcher.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RepositoryRepository
        extends JpaRepository<RepositoryEntity, Long>, JpaSpecificationExecutor<RepositoryEntity> {
}
