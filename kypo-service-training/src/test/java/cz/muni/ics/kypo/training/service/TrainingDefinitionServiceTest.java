package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingDefinitionServiceTest {

    @Autowired
    public TestDataFactory testDataFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingDefinitionService trainingDefinitionService;

    @Mock
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Mock
    private AbstractLevelRepository abstractLevelRepository;
    @Mock
    private TrainingLevelRepository trainingLevelRepository;
    @Mock
    private InfoLevelRepository infoLevelRepository;
    @Mock
    private AssessmentLevelRepository assessmentLevelRepository;
    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private UserRefRepository userRefRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserService userService;

    private final ModelMapper modelMapper = new ModelMapper();

    private TrainingDefinition unreleasedDefinition, releasedDefinition;
    private AssessmentLevel assessmentLevel;
    private TrainingLevel trainingLevel;
    private InfoLevel infoLevel;
    private UserRefDTO userRefDTO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionService = new TrainingDefinitionService(trainingDefinitionRepository, abstractLevelRepository,
                infoLevelRepository, trainingLevelRepository, assessmentLevelRepository, trainingInstanceRepository, userRefRepository,
                securityService, userService, modelMapper);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(1L);
        infoLevel.setOrder(0);

        trainingLevel = testDataFactory.getPenalizedLevel();
        trainingLevel.setId(2L);
        trainingLevel.setOrder(1);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(3L);
        assessmentLevel.setOrder(2);

        unreleasedDefinition = testDataFactory.getUnreleasedDefinition();
        unreleasedDefinition.setId(1L);

        releasedDefinition = testDataFactory.getReleasedDefinition();
        releasedDefinition.setId(2L);

        userRefDTO = testDataFactory.getUserRefDTO1();

        given(userService.getUserRefFromUserAndGroup()).willReturn(userRefDTO);
    }

    @Test
    public void getTrainingDefinitionById() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        TrainingDefinition definition = trainingDefinitionService.findById(unreleasedDefinition.getId());
        assertEquals(definition, unreleasedDefinition);
        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNonexistentTrainingDefinitionById() {
        Long id = 1000L;
        trainingDefinitionService.findById(id);
    }

    @Test
    public void findAll() {
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(unreleasedDefinition);
        expected.add(releasedDefinition);

        Page<TrainingDefinition> page = new PageImpl<>(expected);
        given(trainingDefinitionRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(page);
        Page<TrainingDefinition> resultPage = trainingDefinitionService.findAll(Expressions.booleanOperation(Ops.IS_NOT_NULL), PageRequest.of(0, 2));
        assertEquals(2, resultPage.getTotalElements());
    }

    @Test
    public void createTrainingDefinitionWithKnownUser() {
        UserRef user = new UserRef();
        user.setUserRefId(1L);
        given(trainingDefinitionRepository.save(unreleasedDefinition)).willReturn(unreleasedDefinition);
        given(userRefRepository.findUserByUserRefId(anyLong())).willReturn(Optional.of(user));
        assertNotEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
        TrainingDefinition definition = trainingDefinitionService.create(unreleasedDefinition);
        assertEquals(unreleasedDefinition, definition);
        assertTrue(definition.getAuthors().contains(user));
        assertEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
        then(trainingDefinitionRepository).should(times(1)).save(unreleasedDefinition);
    }

    @Test
    public void createTrainingDefinitionWithUnknownUser(){
        UserRef user = new UserRef();
        user.setUserRefId(1L);
        given(trainingDefinitionRepository.save(unreleasedDefinition)).willReturn(unreleasedDefinition);
        given(userRefRepository.findUserByUserRefId(anyLong())).willReturn(Optional.empty());
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);
        assertNotEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
        TrainingDefinition definition = trainingDefinitionService.create(unreleasedDefinition);
        assertEquals(unreleasedDefinition, definition);
        assertTrue(definition.getAuthors().contains(user));
        assertEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
        then(trainingDefinitionRepository).should(times(1)).save(unreleasedDefinition);
    }

    @Test
    public void updateTrainingDefinition() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(new UserRef());
        assertNotEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
        trainingDefinitionService.update(unreleasedDefinition);
        assertEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(trainingDefinitionRepository).should().save(unreleasedDefinition);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingReleasedDefinition() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.update(releasedDefinition);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingDefinitionWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.update(unreleasedDefinition);
    }

    @Test
    public void cloneTrainingDefinition() {
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(new UserRef());
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(anyLong()))
                .willReturn(List.of(infoLevel, trainingLevel));

        TrainingDefinition clonedDefinition = new TrainingDefinition();
        String newTitle = "new Title";
        modelMapper.map(releasedDefinition, clonedDefinition);
        clonedDefinition.setId(null);
        clonedDefinition.setBetaTestingGroup(null);
        clonedDefinition.setTitle(newTitle);
        clonedDefinition.setState(TDState.UNRELEASED);
        clonedDefinition.setAuthors(new HashSet<>());

        trainingDefinitionService.clone(releasedDefinition.getId(), newTitle);
        then(trainingDefinitionRepository).should().findById(releasedDefinition.getId());
        then(trainingDefinitionRepository).should().save(clonedDefinition);

        infoLevel.setOrder(0);
        infoLevel.setTrainingDefinition(releasedDefinition);
        trainingLevel.setOrder(1);
        trainingLevel.setTrainingDefinition(releasedDefinition);

        InfoLevel clonedInfoLevel = new InfoLevel();
        modelMapper.map(infoLevel, clonedInfoLevel);
        clonedInfoLevel.setId(null);
        clonedInfoLevel.setTrainingDefinition(null);

        TrainingLevel clonedTrainingLevel = new TrainingLevel();
        modelMapper.map(trainingLevel, clonedTrainingLevel);
        clonedTrainingLevel.setTrainingDefinition(null);
        clonedTrainingLevel.setId(null);

        then(infoLevelRepository).should().save(clonedInfoLevel);
        then(trainingLevelRepository).should().save(clonedTrainingLevel);
    }

    @Test
    public void swapLevels(){
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(false);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        infoLevel.setOrder(0);
        trainingLevel.setOrder(1);
        trainingDefinitionService.swapLevels(unreleasedDefinition.getId(), infoLevel.getId(), trainingLevel.getId());
        assertEquals(1, infoLevel.getOrder());
        assertEquals(0, trainingLevel.getOrder());
    }

    @Test(expected = EntityConflictException.class)
    public void swapLevelsInReleasedDefinition(){
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.swapLevels(releasedDefinition.getId(), 1L, 2L);
    }

    @Test(expected = EntityConflictException.class)
    public void swapLevelsInDefinitionWithInstances(){
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(true);
        trainingDefinitionService.swapLevels(unreleasedDefinition.getId(), 1L, 2L);
    }

    @Test
    public void moveLevel(){
        trainingLevel.setOrder(1);
        trainingLevel.setTrainingDefinition(unreleasedDefinition);
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(2);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(false);
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        assertNotEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());

        trainingDefinitionService.moveLevel(unreleasedDefinition.getId(), trainingLevel.getId(), 2);
        assertEquals(2, trainingLevel.getOrder());
        assertEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
    }

    @Test
    public void moveLevelWithBiggerOrderNumber(){
        trainingLevel.setOrder(1);
        trainingLevel.setTrainingDefinition(unreleasedDefinition);
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(2);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(false);
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        assertNotEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());

        trainingDefinitionService.moveLevel(unreleasedDefinition.getId(), trainingLevel.getId(), 100);
        assertEquals(2, trainingLevel.getOrder());
        assertEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
    }

    @Test
    public void moveLevelWithNegativeOrderNumber(){
        trainingLevel.setOrder(1);
        trainingLevel.setTrainingDefinition(unreleasedDefinition);
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(2);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(false);
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        assertNotEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());

        trainingDefinitionService.moveLevel(unreleasedDefinition.getId(), trainingLevel.getId(), -100);
        assertEquals(0, trainingLevel.getOrder());
        assertEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());
    }

    @Test(expected = EntityConflictException.class)
    public void moveLevelFromReleasedDefinition(){
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(2);
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.moveLevel(releasedDefinition.getId(), 1L, 2);
    }

    @Test(expected = EntityConflictException.class)
    public void moveLevelFromDefinitionWithInstances(){
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(2);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.moveLevel(unreleasedDefinition.getId(), 1L, 2);
    }

    @Test
    public void delete() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(unreleasedDefinition.getId()))
                .willReturn(List.of(trainingLevel, assessmentLevel, infoLevel));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        given(abstractLevelRepository.findById(assessmentLevel.getId())).willReturn(Optional.of(assessmentLevel));

        trainingDefinitionService.delete(unreleasedDefinition.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(trainingDefinitionRepository).should().delete(unreleasedDefinition);
        then(assessmentLevelRepository).should().delete(assessmentLevel);
        then(trainingLevelRepository).should().delete(trainingLevel);
        then(infoLevelRepository).should().delete(infoLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteWithCannotBeDeletedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.delete(releasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void deleteWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.delete(unreleasedDefinition.getId());
    }

    @Test
    public void deleteOneLevel() {
        infoLevel.setOrder(0);
        assessmentLevel.setOrder(1);
        unreleasedDefinition.setEstimatedDuration(15);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(unreleasedDefinition.getId())).willReturn(List.of(assessmentLevel));
        assertNotEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());

        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(), infoLevel.getId());
        assertEquals(LocalDateTime.now(Clock.systemUTC()).getSecond(), unreleasedDefinition.getLastEdited().getSecond());
        assertEquals(15-infoLevel.getEstimatedDuration(), unreleasedDefinition.getEstimatedDuration());
        assertEquals(0, assessmentLevel.getOrder());
        assertEquals(userRefDTO.getUserRefFullName(), unreleasedDefinition.getLastEditedBy());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should().findById(any(Long.class));
        then(infoLevelRepository).should().delete(infoLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteOneLevelWithReleasedDefinition() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.deleteOneLevel(releasedDefinition.getId(), any(Long.class));
    }

    @Test
    public void updateTrainingLevel() {
        TrainingLevel updatedTrainingLevel = new TrainingLevel();
        updatedTrainingLevel.setTitle("Updated Ttile");
        updatedTrainingLevel.setMaxScore(20);
        updatedTrainingLevel.setAnswer("updatedAnswer");
        updatedTrainingLevel.setAnswerVariableName("updatedVariableName");
        trainingLevel.setTrainingDefinition(unreleasedDefinition);
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingLevelRepository.findById(anyLong())).willReturn(Optional.of(trainingLevel));
        trainingDefinitionService.updateTrainingLevel(unreleasedDefinition.getId(), trainingLevel);

        then(trainingDefinitionRepository).should().findById(anyLong());
        then(trainingLevelRepository).should().save(trainingLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingLevelWithReleasedDefinition() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.updateTrainingLevel(releasedDefinition.getId(), any(TrainingLevel.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateTrainingLevelWithLevelNotInDefinition() {
        TrainingLevel level = new TrainingLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.updateTrainingLevel(unreleasedDefinition.getId(), level);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.updateTrainingLevel(unreleasedDefinition.getId(), trainingLevel);
    }

    @Test
    public void updateAssessmentLevel() {
        assessmentLevel.setTrainingDefinition(unreleasedDefinition);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(assessmentLevelRepository.findById(any(Long.class))).willReturn(Optional.of(assessmentLevel));

        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), assessmentLevel);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(assessmentLevelRepository).should().save(assessmentLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void updateAssessmentLevelWithReleasedDefinition() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.updateAssessmentLevel(releasedDefinition.getId(), any(AssessmentLevel.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateAssessmentLevelWithLevelNotInDefinition() {
        AssessmentLevel level = new AssessmentLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        given(abstractLevelRepository.findById(assessmentLevel.getId())).willReturn(Optional.of(assessmentLevel));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level);
    }

    @Test(expected = EntityConflictException.class)
    public void updateAssessmentLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), assessmentLevel);
    }

    @Test
    public void updateInfoLevel() {
        infoLevel.setTrainingDefinition(unreleasedDefinition);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(infoLevelRepository.findById(any(Long.class))).willReturn(Optional.of(infoLevel));

        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), infoLevel);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(infoLevelRepository).should().save(infoLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void updateInfoLevelWithReleasedDefinition() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.updateInfoLevel(releasedDefinition.getId(), any(InfoLevel.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateInfoLevelWithLevelNotInDefinition() {
        InfoLevel level = new InfoLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(trainingLevel.getId())).willReturn(Optional.of(trainingLevel));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), level);
    }

    @Test(expected = EntityConflictException.class)
    public void updateInfoLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), infoLevel);
    }

    @Test
    public void createTrainingLevel() {
        TrainingLevel newTrainingLevel = new TrainingLevel();
        newTrainingLevel.setId(10L);
        newTrainingLevel.setMaxScore(100);
        newTrainingLevel.setTitle("Title of training level");
        newTrainingLevel.setIncorrectAnswerLimit(100);
        newTrainingLevel.setAnswer("Secret answer");
        newTrainingLevel.setSolution("Solution of the training should be here");
        newTrainingLevel.setSolutionPenalized(true);
        newTrainingLevel.setEstimatedDuration(1);
        newTrainingLevel.setOrder(10);
        newTrainingLevel.setContent("The test entry should be here");

        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingLevelRepository.save(any(TrainingLevel.class))).willReturn(newTrainingLevel);
        TrainingLevel createdLevel = trainingDefinitionService.createTrainingLevel(unreleasedDefinition.getId());

        assertEquals(newTrainingLevel, createdLevel);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createTrainingLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.createTrainingLevel(releasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createTrainingLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.createTrainingLevel(unreleasedDefinition.getId());
    }

    @Test
    public void createInfoLevel() {
        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setId(11L);
        newInfoLevel.setTitle("Title of info Level");
        newInfoLevel.setMaxScore(20);
        newInfoLevel.setContent("Content of info level should be here.");
        newInfoLevel.setOrder(11);

        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(infoLevelRepository.save(any(InfoLevel.class))).willReturn(newInfoLevel);
        InfoLevel createdLevel = trainingDefinitionService.createInfoLevel(unreleasedDefinition.getId());

        assertNotNull(createdLevel);
        assertEquals(newInfoLevel, createdLevel);
        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createInfoLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.createInfoLevel(releasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createInfoLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.createInfoLevel(unreleasedDefinition.getId());
    }

    @Test
    public void createAssessmentLevel() {
        AssessmentLevel newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setId(12L);
        newAssessmentLevel.setTitle("Title of assessment level");
        newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        newAssessmentLevel.setMaxScore(0);
        newAssessmentLevel.setInstructions("Instructions should be here");
        newAssessmentLevel.setQuestions(new ArrayList<>());
        newAssessmentLevel.setOrder(12);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(assessmentLevelRepository.save(any(AssessmentLevel.class))).willReturn(newAssessmentLevel);
        AssessmentLevel createdLevel = trainingDefinitionService.createAssessmentLevel(unreleasedDefinition.getId());

        assertNotNull(createdLevel);
        assertEquals(newAssessmentLevel, createdLevel);
        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createAssessmentLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.createAssessmentLevel(releasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createAssessmentLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.createAssessmentLevel(unreleasedDefinition.getId());
    }

    @Test
    public void findLevelById() {
        given(abstractLevelRepository.findByIdIncludingDefinition(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(infoLevel.getId());
        assertTrue(abstractLevel instanceof InfoLevel);
        assertEquals(infoLevel.getId(), abstractLevel.getId());
        then(abstractLevelRepository).should().findByIdIncludingDefinition(infoLevel.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findLevelByIdNotExisting() {
        trainingDefinitionService.findLevelById(555L);
    }

    @Test
    public void switchState_UNRELEASEDtoRELEASED() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.switchState(unreleasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.RELEASED);
        assertEquals(TDState.RELEASED, unreleasedDefinition.getState());
    }

    @Test
    public void switchState_RELEASEDtoARCHIVED() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.switchState(releasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED);
        assertEquals(TDState.ARCHIVED, releasedDefinition.getState());
    }

    @Test
    public void switchState_RELEASEDtoUNRELEASED() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(false);
        trainingDefinitionService.switchState(releasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        assertEquals(TDState.UNRELEASED, releasedDefinition.getState());
    }

    @Test(expected = EntityConflictException.class)
    public void switchState_RELEASEDtoUNRELEASED_withCreatedInstances() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(true);
        trainingDefinitionService.switchState(releasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
    }

    @After
    public void after() {
        reset(trainingDefinitionRepository);
    }
}
