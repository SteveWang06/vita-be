package com.vita.post_service.repository;

import com.vita.post_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    // Find posts by PostType ID
    List<Post> findByPostType_Id(String postTypeId);
    
    // Find posts by PostType Code
    List<Post> findByPostType_Code(String postTypeCode);
    
    // Count posts by PostType ID (useful for checking if PostType is in use)
    long countByPostType_Id(String postTypeId);
    
    // Find posts without PostType (null PostType)
    List<Post> findByPostTypeIsNull();
    
    // Find posts by userId and PostType ID
    List<Post> findByUserIdAndPostType_Id(String userId, String postTypeId);
}
