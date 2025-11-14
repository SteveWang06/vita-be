package com.vita.rental_service.mapper;

import com.vita.rental_service.domain.entity.Review;
import com.vita.rental_service.dto.request.CreateReviewRequest;
import com.vita.rental_service.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rentalId", ignore = true)
    @Mapping(target = "reviewerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toEntity(CreateReviewRequest request);
    
    ReviewResponse toResponse(Review review);
}

