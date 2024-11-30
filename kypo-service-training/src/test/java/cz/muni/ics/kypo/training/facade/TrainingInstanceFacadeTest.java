package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.*;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.BadRequestException;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.api.SandboxApiService;
import cz.muni.ics.kypo.training.service.detection.CheatingDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = {
        TestDataFactory.class,
        TrainingInstanceMapperImpl.class,
        TrainingRunMapperImpl.class,
        TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class,
        BetaTestingGroupMapperImpl.class,
        AttachmentMapperImpl.class
})
public class TrainingInstanceFacadeTest {

    private TrainingInstanceFacade trainingInstanceFacade;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private TrainingRunMapperImpl trainingRunMapper;

    @Autowired
    private TrainingInstanceMapper trainingInstanceMapper;

    @MockBean
    private TrainingInstanceService trainingInstanceService;
    @MockBean
    private TrainingRunService trainingRunService;
    @MockBean
    private CheatingDetectionService cheatingDetectionService;
    @MockBean
    private ElasticsearchApiService elasticsearchApiService;
    @MockBean
    private TrainingDefinitionService trainingDefinitionService;
    @MockBean
    private SecurityService securityService;
    @MockBean
    private UserService userService;
    @MockBean
    private SandboxApiService sandboxApiService;
    @MockBean
    private TrainingFeedbackApiService trainingFeedbackApiService;

    private TrainingInstance trainingInstance1, trainingInstance2;
    private TrainingInstanceCreateDTO trainingInstanceCreate;
    private TrainingInstanceUpdateDTO trainingInstanceUpdate;
    private TrainingDefinition trainingDefinition;
    private UserRef organizer1, organizer2, organizer3;
    private UserRefDTO organizerDTO1, organizerDTO2, organizerDTO3;
    private Pageable pageable;
    private PageResultResource.Pagination pagination;
    private LockedPoolInfo lockedPoolInfo;
    private TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        trainingInstanceFacade = new TrainingInstanceFacade(trainingInstanceService, trainingDefinitionService, trainingRunService,
                cheatingDetectionService, userService, elasticsearchApiService, securityService, sandboxApiService, trainingInstanceMapper,
                trainingRunMapper, trainingFeedbackApiService);

        pageable = PageRequest.of(0, 5);

        organizer1 = new UserRef();
        organizer1.setId(1L);
        organizer1.setUserRefId(10L);
        organizer2 = new UserRef();
        organizer2.setId(2L);
        organizer2.setUserRefId(20L);
        organizer3 = new UserRef();
        organizer3.setId(3L);
        organizer3.setUserRefId(30L);

        organizerDTO1 = createUserRefDTO(10L, "Bc. Alexander Howell", "Howell", "Alexander", "mail1@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO2 = createUserRefDTO(20L, "Bc. Peter Reeves", "Reeves", "Peter", "mail2@muni.cz", "https://oidc.muni.cz/oidc", null);
        organizerDTO3 = createUserRefDTO(30L, "Ing. Lee Nicholls", "Nicholls", "Lee", "mail3@muni.cz", "https://oidc.muni.cz/oidc", null);

        trainingInstance1 = testDataFactory.getConcludedInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setOrganizers(new HashSet<>(Set.of(organizer1, organizer2)));
        trainingInstance1.setAccessToken("abc-1234");

        trainingInstance2 = testDataFactory.getFutureInstance();
        trainingInstance2.setId(2L);

        trainingInstanceCreate = testDataFactory.getTrainingInstanceCreateDTO();
        trainingInstanceCreate.setTitle("test");
        trainingInstanceCreate.setTrainingDefinitionId(1L);

        trainingInstanceUpdate = testDataFactory.getTrainingInstanceUpdateDTO();
        trainingInstanceUpdate.setId(1L);
        trainingInstanceUpdate.setTrainingDefinitionId(1L);

        trainingDefinition = testDataFactory.getReleasedDefinition();

        lockedPoolInfo = new LockedPoolInfo();
        lockedPoolInfo.setId(1L);
        lockedPoolInfo.setPoolId(1L);

        trainingInstanceAssignPoolIdDTO = new TrainingInstanceAssignPoolIdDTO();
        trainingInstanceAssignPoolIdDTO.setPoolId(1L);

        given(userService.getUserRefFromUserAndGroup()).willReturn(organizerDTO1);

    }

    @Test
    public void findTrainingInstanceById() {
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        given(trainingInstanceService.findByIdIncludingDefinition(trainingInstance1.getId())).willReturn(trainingInstance1);
        trainingInstanceFacade.findById(trainingInstance1.getId());
        then(trainingInstanceService).should().findByIdIncludingDefinition(trainingInstance1.getId());
    }

    @Test
    public void findAllTrainingInstances() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page p = new PageImpl<TrainingInstance>(expected);

        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)).willReturn(true);
        given(trainingInstanceService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        PageResultResource<TrainingInstanceFindAllResponseDTO> trainingInstanceDTO = trainingInstanceFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEqualsTrainingInstanceFindAllView(trainingInstance1, trainingInstanceDTO.getContent().get(0));
        deepEqualsTrainingInstanceFindAllView(trainingInstance2, trainingInstanceDTO.getContent().get(1));

        then(trainingInstanceService).should().findAll(predicate, PageRequest.of(0, 2));
    }

    @Test
    public void createTrainingInstance() {
        given(trainingInstanceService.create(any(TrainingInstance.class))).willReturn(trainingInstance1);
        given(trainingDefinitionService.findById(any(Long.class))).willReturn(new TrainingDefinition());
        given(trainingInstanceService.findUserRefsByUserRefIds(any(Set.class))).willReturn(new HashSet());
        given(userService.getUsersRefDTOByGivenUserIds(anyList(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        trainingInstanceCreate.setPoolId(anyLong());
        trainingInstanceFacade.create(trainingInstanceCreate);
        then(trainingInstanceService).should().create(any(TrainingInstance.class));
    }

    @Test
    public void updateTrainingInstance() {
        given(userService.getUsersRefDTOByGivenUserIds(anyList(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        given(trainingInstanceService.findById(trainingInstanceUpdate.getId())).willReturn(trainingInstance1);
        TrainingDefinition trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);
        given(trainingDefinitionService.findById(trainingInstanceUpdate.getTrainingDefinitionId())).willReturn(trainingDefinition);
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        trainingInstanceUpdate.setPoolId(anyLong());
        trainingInstanceFacade.update(trainingInstanceUpdate);
        then(trainingInstanceService).should().update(any(TrainingInstance.class));
    }

    @Test
    public void deleteTrainingInstance() {
        trainingInstance1.setPoolId(null);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        trainingInstanceFacade.delete(trainingInstance1.getId(), false);
        then(trainingInstanceService).should().delete(trainingInstance1);
    }

    @Test
    public void deleteTrainingInstanceWithUnfinishedInstanceAndRuns(){
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        given(trainingInstanceService.checkIfInstanceIsFinished(anyLong())).willReturn(false);
        given(trainingRunService.existsAnyForTrainingInstance(anyLong())).willReturn(true);
        assertThrows(EntityConflictException.class, () -> trainingInstanceFacade.delete(1L, false));
    }

    @Test
    public void deleteTrainingInstanceWithLockedPool(){
        trainingInstance1.setPoolId(1L);
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        assertThrows(EntityConflictException.class, () -> trainingInstanceFacade.delete(1L, false));
    }

    @Test
    public void assignPoolToTrainingInstance() {
        trainingInstance1.setPoolId(null);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(sandboxApiService.lockPool(trainingInstance1.getPoolId(), trainingInstance1.getAccessToken())).willReturn(lockedPoolInfo);
        trainingInstanceFacade.assignPoolToTrainingInstance(trainingInstance1.getId(), trainingInstanceAssignPoolIdDTO);
        then(sandboxApiService).should().lockPool(trainingInstance1.getId(), trainingInstance1.getAccessToken());
    }

    @Test
    public void allocateSandboxesWithServiceException() {
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        willThrow(EntityConflictException.class).given(sandboxApiService).lockPool(anyLong(), eq(trainingInstance1.getAccessToken()));
        assertThrows(EntityConflictException.class, () -> trainingInstanceFacade.assignPoolToTrainingInstance(trainingInstance1.getId(), trainingInstanceAssignPoolIdDTO));
    }

    @Test
    public void unassignPool(){
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        trainingInstanceFacade.unassignPoolInTrainingInstance(trainingInstance1.getId());
        then(sandboxApiService).should().unlockPool(anyLong());
        then(trainingInstanceService).should().auditAndSave(any(TrainingInstance.class));
    }

    @Test
    public void unassignPoolWithNullPoolId() {
        trainingInstance1.setPoolId(null);
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        assertThrows(EntityConflictException.class, () -> trainingInstanceFacade.unassignPoolInTrainingInstance(trainingInstance1.getId()));
    }

    @Test
    public void checkIfInstanceCanBeDeleted_TRUE(){
        given(trainingInstanceService.checkIfInstanceIsFinished(anyLong())).willReturn(true);
        TrainingInstanceIsFinishedInfoDTO info = trainingInstanceFacade.checkIfInstanceCanBeDeleted(1L);
        assertTrue(info.isHasFinished());
        assertEquals("Training instance has already finished and can be safely deleted.", info.getMessage());
    }

    @Test
    public void checkIfInstanceCanBeDeleted_FALSE(){
        given(trainingInstanceService.checkIfInstanceIsFinished(anyLong())).willReturn(false);
        TrainingInstanceIsFinishedInfoDTO info = trainingInstanceFacade.checkIfInstanceCanBeDeleted(1L);
        assertFalse(info.isHasFinished());
        assertEquals("WARNING: Training instance is still running! Are you sure you want to delete it?", info.getMessage());
    }

    @Test
    public void getOrganizersOfTrainingInstance() {
        pagination = new PageResultResource.Pagination(0, 2, 5, 2, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer2.getUserRefId(), organizer1.getUserRefId()), pageable, null, null)).willReturn(
                new PageResultResource<>(List.of(organizerDTO1, organizerDTO2), pagination));
        PageResultResource<UserRefDTO> organizersOfTrainingInstance = trainingInstanceFacade.getOrganizersOfTrainingInstance(trainingInstance1.getId(), pageable, null, null);
        assertEquals(pagination.toString(), organizersOfTrainingInstance.getPagination().toString());
        assertTrue(organizersOfTrainingInstance.getContent().containsAll(Set.of(organizerDTO1, organizerDTO2)));
    }

    @Test
    public void getOrganizersNotInGivenTrainingInstance() {
        pagination = new PageResultResource.Pagination(0, 1, 1, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, Set.of(organizer1.getUserRefId(), organizer2.getUserRefId()), pageable, null, null)).willReturn(
                new PageResultResource<>(List.of(organizerDTO3), pagination));
        PageResultResource<UserRefDTO> organizersNotInTrainingInstance = trainingInstanceFacade.getOrganizersNotInGivenTrainingInstance(trainingInstance1.getId(), pageable, null, null);
        assertEquals(pagination.toString(), organizersNotInTrainingInstance.getPagination().toString());
        assertTrue(organizersNotInTrainingInstance.getContent().contains(organizerDTO3));
    }

    @Test
    public void editOrganizers() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.createOrGetUserRef(organizer1.getUserRefId())).willReturn(organizer1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.createOrGetUserRef(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer2.getUserRefId())));
        assertEquals(2, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer3)));
    }

    @Test
    public void editOrganizersRemoveLoggedInOrganizer() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.createOrGetUserRef(organizer1.getUserRefId())).willReturn(organizer1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.createOrGetUserRef(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer1.getUserRefId())));
        assertEquals(3, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersConcurrentlyRemoveAndAddOrganizerWhoIsNotInTrainingInstance() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.createOrGetUserRef(organizer1.getUserRefId())).willReturn(organizer1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.createOrGetUserRef(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer3.getUserRefId())), new HashSet<>(Set.of(organizer3.getUserRefId())));
        assertEquals(3, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersConcurrentlyRemoveAndAddOrganizerWhoIsInTrainingInstance() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.createOrGetUserRef(organizer1.getUserRefId())).willReturn(organizer1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer2.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO2), pagination));
        given(userService.createOrGetUserRef(organizer2.getUserRefId())).willReturn(organizer2);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId())), new HashSet<>(Set.of(organizer2.getUserRefId())));
        assertEquals(2, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2)));
    }

    @Test
    public void editOrganizersWithEmptySetOfRemovalAndAdditionSets() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3), pagination));
        given(userService.getUserByUserRefId(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(), new HashSet<>());
        assertEquals(2, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2)));
    }

    @Test
    public void editOrganizersRemove() {
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(), new HashSet<>(Set.of(organizer1.getUserRefId(), organizer2.getUserRefId())));
        assertEquals(1, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().contains(organizer1));
    }

    @Test
    public void editOrganizersAdd() {
        trainingInstance1.removeOrganizersByUserRefIds(Set.of(organizer2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.createOrGetUserRef(organizer1.getUserRefId())).willReturn(organizer1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer2.getUserRefId(), organizer3.getUserRefId()), PageRequest.of(0, 999), null, null))
                .willReturn(new PageResultResource<>(List.of(organizerDTO3, organizerDTO2), pagination));
        given(userService.createOrGetUserRef(organizer2.getUserRefId())).willReturn(organizer2);
        given(userService.createOrGetUserRef(organizer3.getUserRefId())).willReturn(organizer3);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId(), organizer3.getUserRefId())), new HashSet<>());
        assertEquals(3, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void editOrganizersAddUserRefNotInDB() {
        trainingInstance1.removeOrganizersByUserRefIds(Set.of(organizer2.getUserRefId()));
        PageResultResource.Pagination pagination = new PageResultResource.Pagination(0, 1, 999, 1, 1);
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(trainingInstance1);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(organizer1.getUserRefId());
        given(userService.createOrGetUserRef(organizer1.getUserRefId())).willReturn(organizer1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(organizer2.getUserRefId(), organizer3.getUserRefId()), PageRequest.of(0, 999), null, null)).willReturn(new PageResultResource<>(List.of(organizerDTO3, organizerDTO2), pagination));
        given(userService.createOrGetUserRef(organizer3.getUserRefId())).willReturn(organizer3);
        willThrow(EntityNotFoundException.class).given(userService).getUserByUserRefId(organizer2.getUserRefId());
        given(userService.createOrGetUserRef(organizer2.getUserRefId())).willReturn(organizer2);
        trainingInstanceFacade.editOrganizers(trainingInstance1.getId(), new HashSet<>(Set.of(organizer2.getUserRefId(), organizer3.getUserRefId())), new HashSet<>());
        assertEquals(3, trainingInstance1.getOrganizers().size());
        assertTrue(trainingInstance1.getOrganizers().containsAll(Set.of(organizer1, organizer2, organizer3)));
    }

    @Test
    public void createNonLocalInstanceWithNoPool() {
        trainingInstanceCreate.setLocalEnvironment(false);
        trainingInstanceCreate.setPoolId(null);

        assertThrows(BadRequestException.class, () -> trainingInstanceFacade.create(trainingInstanceCreate));
    }

    @Test
    public void updateNonLocalInstanceWithNoPool() {
        given(userService.getUsersRefDTOByGivenUserIds(anyList(), any(Pageable.class), anyString(), anyString())).willReturn(new PageResultResource<>(new ArrayList<>()));
        given(trainingInstanceService.findById(trainingInstanceUpdate.getId())).willReturn(trainingInstance1);
        TrainingDefinition trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        trainingInstance1.setPoolId(null);
        trainingInstance1.setLocalEnvironment(false);
        assertThrows(BadRequestException.class, () -> trainingInstanceFacade.update(trainingInstanceUpdate));
    }

    private void deepEqualsTrainingInstanceFindAllView(TrainingInstance expected, TrainingInstanceFindAllResponseDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    private UserRefDTO createUserRefDTO(Long userRefId, String fullName, String familyName, String givenName, String sub, String iss, byte[] picture) {
        UserRefDTO userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefId(userRefId);
        userRefDTO.setUserRefFullName(fullName);
        userRefDTO.setUserRefFamilyName(familyName);
        userRefDTO.setUserRefGivenName(givenName);
        userRefDTO.setUserRefSub(sub);
        userRefDTO.setIss(iss);
        userRefDTO.setPicture(picture);
        return userRefDTO;
    }

}
