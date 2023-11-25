package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.persistence.model.detection.CheatingDetection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {ForbiddenCommandMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheatingDetectionMapper extends ParentMapper {

    CheatingDetectionDTO mapToDTO(CheatingDetection entity);

    CheatingDetection mapToEntity(CheatingDetectionDTO dto);

    List<CheatingDetectionDTO> mapToList(Collection<CheatingDetectionDTO> dtos);

    List<CheatingDetectionDTO> mapToListDTO(Collection<CheatingDetectionDTO> entities);

    Set<CheatingDetectionDTO> mapToSet(Collection<CheatingDetectionDTO> dtos);

    Set<CheatingDetectionDTO> mapToSetDTO(Collection<CheatingDetectionDTO> entities);

    default Page<CheatingDetectionDTO> mapToPageDTO(Page<CheatingDetectionDTO> objects) {
        List<CheatingDetectionDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<CheatingDetectionDTO> mapToPage(Page<CheatingDetectionDTO> objects) {
        List<CheatingDetectionDTO> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<CheatingDetectionDTO> mapToPageResultResource(Page<CheatingDetection> objects) {
        List<CheatingDetectionDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
