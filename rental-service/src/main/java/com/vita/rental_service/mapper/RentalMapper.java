package com.vita.rental_service.mapper;

import com.vita.rental_service.domain.entity.Rental;
import com.vita.rental_service.dto.request.CreateRentalRequest;
import com.vita.rental_service.dto.request.UpdateRentalRequest;
import com.vita.rental_service.dto.response.RentalResponse;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Rental toEntity(CreateRentalRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(UpdateRentalRequest request, @MappingTarget Rental rental);
    
    @Mapping(target = "photoUrls", source = "photos", qualifiedByName = "parsePhotos")
    RentalResponse toResponse(Rental rental);
    
    @Named("parsePhotos")
    default List<String> parsePhotos(String photos) {
        if (photos == null || photos.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(photos.split(","));
    }
}

