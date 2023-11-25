package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.utils.AbstractFileExtensions;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * The controller for export/import.
 */
@Api(value = "/", tags = "Export Imports",
     consumes = MediaType.APPLICATION_JSON_VALUE,
     authorizations = @Authorization(value = "bearerAuth"))
@ApiResponses(value = {
        @ApiResponse(code = 401, message = "Full authentication is required to access this resource.", response = ApiError.class),
        @ApiResponse(code = 403, message = "The necessary permissions are required for a resource.", response = ApiError.class)
})
@RestController
public class ExportImportRestController {

    private ExportImportFacade exportImportFacade;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new Export import rest controller.
     *
     * @param exportImportFacade the export import facade
     * @param objectMapper       the object mapper
     */
    @Autowired
    public ExportImportRestController(ExportImportFacade exportImportFacade,
                                      ObjectMapper objectMapper) {
        this.exportImportFacade = exportImportFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports training definition and levels.
     *
     * @param trainingDefinitionId the training definition id
     * @return Exported training definition and levels.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get exported training definitions and levels.",
            response = ExportTrainingDefinitionAndLevelsDTO.class,
            nickname = "getExportedTrainingDefinitionAndLevels",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definitions and levels found and exported.", response = ExportTrainingDefinitionAndLevelsDTO.class),
            @ApiResponse(code = 404, message = "Training definition not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/exports/training-definitions/{definitionId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getExportedTrainingDefinitionAndLevels(
            @ApiParam(value = "Id of training definition", required = true)
            @PathVariable("definitionId") Long trainingDefinitionId) {
        FileToReturnDTO file = exportImportFacade.dbExport(trainingDefinitionId);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "octet-stream"));
        header.set("Content-Disposition", "inline; filename=" + file.getTitle() + AbstractFileExtensions.JSON_FILE_EXTENSION);
        header.setContentLength(file.getContent().length);
        return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
    }

    /**
     * Import training definition response entity.
     *
     * @param importTrainingDefinitionDTO the training definition to be imported
     * @param fields                      attributes of the object to be returned as the result.
     * @return the new imported definition
     */
    @ApiOperation(httpMethod = "POST",
            value = "Import training definition with levels.",
            response = TrainingDefinitionByIdDTO.class,
            nickname = "importTrainingDefinition",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training definition imported.", response = TrainingDefinitionByIdDTO.class),
            @ApiResponse(code = 422, message = "Sum of hints penalties in imported training level is greater than maximal score.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PostMapping(path = "/imports/training-definitions", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> importTrainingDefinition(
            @ApiParam(value = "Training definition to be imported", required = true)
            @Valid @RequestBody ImportTrainingDefinitionDTO importTrainingDefinitionDTO,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        TrainingDefinitionByIdDTO trainingDefinitionResource = exportImportFacade.dbImport(importTrainingDefinitionDTO);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, trainingDefinitionResource));
    }

    /**
     * Archive training instance
     *
     * @param trainingInstanceId the training instance id
     * @return file containing wanted training instance
     */
    @ApiOperation(httpMethod = "GET",
            value = "Archive training instance",
            response = TrainingInstanceArchiveDTO.class,
            nickname = "archiveTrainingInstance",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training instance archived.", response = TrainingInstanceArchiveDTO.class),
            @ApiResponse(code = 404, message = "Training instance not found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Cannot archive instance that is not finished.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/exports/training-instances/{instanceId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> archiveTrainingInstance(
            @ApiParam(value = "Id of training instance", required = true)
            @PathVariable("instanceId") Long trainingInstanceId) {
        FileToReturnDTO file = exportImportFacade.archiveTrainingInstance(trainingInstanceId);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "octet-stream"));
        header.set("Content-Disposition", "inline; filename=" + file.getTitle() + AbstractFileExtensions.ZIP_FILE_EXTENSION);
        header.setContentLength(file.getContent().length);
        return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
    }

    /**
     * Export user scores from a specific training instance
     * @param trainingInstanceId id of the training instance
     * @return CSV file containing user scores
     */
    @ApiOperation(httpMethod = "GET",
                    value = "Export training instance scores",
                    response = String.class,
                    nickname = "exportTrainingInstanceScores",
                    produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Training instance score exported", response = String.class),
            @ApiResponse(code = 404, message = "Training instance not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/exports/training-instances/{instanceId}/scores", produces = "text/plain")
    public ResponseEntity<byte[]> exportTrainingInstanceScores(
            @ApiParam(value = "Id of training instance", required = true)
            @PathVariable("instanceId") Long trainingInstanceId) {
        FileToReturnDTO file = exportImportFacade.exportUserScoreFromTrainingInstance(trainingInstanceId);
        HttpHeaders header = fileHttpHeader(new MediaType("text", "plain"),
                file.getTitle() + AbstractFileExtensions.CSV_FILE_EXTENSION, file.getContent().length);
        return new ResponseEntity<>(file.getContent(), header, HttpStatus.OK);
    }

    /**
     * Create a http header for file exports
     * @param type      media type of the file
     * @param fileName  name of the file
     * @param length    size of the file
     * @return corresponding {@link HttpHeaders}
     */
    private HttpHeaders fileHttpHeader(MediaType type, String fileName, int length) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(type);
        header.setAccessControlExposeHeaders(List.of("Content-Disposition"));
        header.set("Content-Disposition", "inline; filename=" + fileName);
        header.setContentLength(length);
        return header;
    }
}
