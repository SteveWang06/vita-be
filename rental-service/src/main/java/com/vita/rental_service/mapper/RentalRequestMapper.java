package com.vita.rental_service.mapper;

import com.vita.rental_service.domain.entity.RentalRequest;
import com.vita.rental_service.dto.request.CreateRentalRequestDto;
import com.vita.rental_service.dto.response.RentalRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalRequestMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "renterId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RentalRequest toEntity(CreateRentalRequestDto request);
    
    RentalRequestResponse toResponse(RentalRequest rentalRequest);
}

