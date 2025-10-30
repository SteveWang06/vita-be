package com.vita.post_service.repository;

import com.vita.post_service.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTypeRepository extends JpaRepository<PostType, String> {
    Optional<PostType> findByCode(String code);
    
    boolean existsByName(String name);
    
    boolean existsByCode(String code);
    
    List<PostType> findByIsActive(Boolean isActive);
}

