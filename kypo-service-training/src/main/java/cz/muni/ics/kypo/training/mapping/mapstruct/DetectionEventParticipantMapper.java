package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.persistence.model.detection.DetectionEventParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DetectionEventParticipantMapper extends ParentMapper {
    DetectionEventParticipant mapToEntity(DetectionEventParticipantDTO dto);

    DetectionEventParticipantDTO mapToDTO(DetectionEventParticipant entity);

    List<DetectionEventParticipant> mapToList(Collection<DetectionEventParticipantDTO> dtos);

    List<DetectionEventParticipantDTO> mapToListDTO(Collection<DetectionEventParticipant> entities);

    default Page<DetectionEventParticipant> mapToPage(Page<DetectionEventParticipantDTO> objects) {
        List<DetectionEventParticipant> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<DetectionEventParticipantDTO> mapToPageResultResource(Page<DetectionEventParticipant> objects) {
        List<DetectionEventParticipantDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
