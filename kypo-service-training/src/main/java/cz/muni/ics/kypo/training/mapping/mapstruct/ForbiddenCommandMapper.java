package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.cheatingdetection.ForbiddenCommandDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.persistence.model.detection.ForbiddenCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ForbiddenCommandMapper extends ParentMapper {

    ForbiddenCommand mapToEntity(ForbiddenCommandDTO dto);

    ForbiddenCommandDTO mapToDTO(ForbiddenCommand entity);

    List<ForbiddenCommand> mapToList(Collection<ForbiddenCommandDTO> dtos);

    List<ForbiddenCommandDTO> mapToListDTO(Collection<ForbiddenCommand> entities);

    default Page<ForbiddenCommand> mapToPage(Page<ForbiddenCommandDTO> objects) {
        List<ForbiddenCommand> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<ForbiddenCommandDTO> mapToPageResultResource(Page<ForbiddenCommand> objects) {
        List<ForbiddenCommandDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
