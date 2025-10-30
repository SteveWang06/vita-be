package com.vita.post_service.service;

import com.vita.post_service.dto.request.CreatePostTypeRequest;
import com.vita.post_service.dto.request.UpdatePostTypeRequest;
import com.vita.post_service.dto.response.PostTypeResponse;
import com.vita.post_service.entity.PostType;
import com.vita.post_service.exception.DuplicatePostTypeException;
import com.vita.post_service.exception.PostTypeInUseException;
import com.vita.post_service.exception.PostTypeNotFoundException;
import com.vita.post_service.repository.PostRepository;
import com.vita.post_service.repository.PostTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostTypeService {

    private final PostTypeRepository postTypeRepository;
    private final PostRepository postRepository;

    @Transactional
    public PostTypeResponse createPostType(CreatePostTypeRequest request, String createdBy) {
        // Validate name uniqueness
        if (postTypeRepository.existsByName(request.getName())) {
            throw new DuplicatePostTypeException("name", request.getName());
        }

        // Validate code uniqueness
        if (postTypeRepository.existsByCode(request.getCode())) {
            throw new DuplicatePostTypeException("code", request.getCode());
        }

        PostType postType = PostType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .code(request.getCode().toUpperCase())
                .isActive(true)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        PostType savedPostType = postTypeRepository.save(postType);
        return PostTypeResponse.fromEntity(savedPostType);
    }

    public PostTypeResponse getPostTypeById(String id) {
        PostType postType = postTypeRepository.findById(id)
                .orElseThrow(() -> new PostTypeNotFoundException(id, "id"));
        return PostTypeResponse.fromEntity(postType);
    }

    public PostTypeResponse getPostTypeByCode(String code) {
        PostType postType = postTypeRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new PostTypeNotFoundException(code, "code"));
        return PostTypeResponse.fromEntity(postType);
    }

    public List<PostTypeResponse> getAllPostTypes(Boolean isActive) {
        List<PostType> postTypes;
        if (isActive != null) {
            postTypes = postTypeRepository.findByIsActive(isActive);
        } else {
            postTypes = postTypeRepository.findAll();
        }
        return postTypes.stream()
                .map(PostTypeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostTypeResponse updatePostType(String id, UpdatePostTypeRequest request, String updatedBy) {
        PostType postType = postTypeRepository.findById(id)
                .orElseThrow(() -> new PostTypeNotFoundException(id, "id"));

        // Validate name uniqueness if name is being changed
        if (request.getName() != null && !request.getName().equals(postType.getName())) {
            if (postTypeRepository.existsByName(request.getName())) {
                throw new DuplicatePostTypeException("name", request.getName());
            }
            postType.setName(request.getName());
        }

        // Validate code uniqueness if code is being changed
        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(postType.getCode())) {
            String upperCode = request.getCode().toUpperCase();
            if (postTypeRepository.existsByCode(upperCode)) {
                throw new DuplicatePostTypeException("code", upperCode);
            }
            postType.setCode(upperCode);
        }

        if (request.getDescription() != null) {
            postType.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            postType.setIsActive(request.getIsActive());
        }

        postType.setUpdatedBy(updatedBy);
        postType.setUpdatedAt(LocalDateTime.now());
        PostType updatedPostType = postTypeRepository.save(postType);
        return PostTypeResponse.fromEntity(updatedPostType);
    }

    @Transactional
    public void deletePostType(String id) {
        PostType postType = postTypeRepository.findById(id)
                .orElseThrow(() -> new PostTypeNotFoundException(id, "id"));

        // Check if PostType is being used by any posts
        long postCount = postRepository.countByPostType_Id(id);
        if (postCount > 0) {
            throw new PostTypeInUseException(id, postCount);
        }

        postTypeRepository.delete(postType);
    }

    // Helper method for internal use
    public PostType getPostTypeEntityById(String id) {
        return postTypeRepository.findById(id)
                .orElseThrow(() -> new PostTypeNotFoundException(id, "id"));
    }

    public PostType getPostTypeEntityByCode(String code) {
        return postTypeRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new PostTypeNotFoundException(code, "code"));
    }
}

