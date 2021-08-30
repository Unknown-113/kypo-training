package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PoolInfoDTO;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.AccessTokenRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.apache.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingInstanceServiceTest {

    @Autowired
    TestDataFactory testDataFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingInstanceService trainingInstanceService;

    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private TrainingRunRepository trainingRunRepository;
    @Mock
    private UserRefRepository organizerRefRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserService userService;
    @Mock
    private TrainingDefinition trainingDefinition;
    private TrainingInstance trainingInstance1, trainingInstance2;
    private TrainingRun trainingRun1, trainingRun2;
    private UserRef user;
    private UserRefDTO userRefDTO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceService = new TrainingInstanceService(trainingInstanceRepository, accessTokenRepository,
                trainingRunRepository, organizerRefRepository, securityService, userService);

        trainingInstance1 = testDataFactory.getConcludedInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTrainingDefinition(trainingDefinition);

        user = new UserRef();

        trainingInstance2 = testDataFactory.getFutureInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTrainingDefinition(trainingDefinition);
        trainingInstance2.setPoolId(null);

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setTrainingInstance(trainingInstance1);

        userRefDTO = testDataFactory.getUserRefDTO1();

        given(userService.getUserRefFromUserAndGroup()).willReturn(userRefDTO);
    }

    @Test
    public void findById() {
        given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));

        TrainingInstance instance = trainingInstanceService.findById(trainingInstance1.getId());
        deepEquals(trainingInstance1, instance);
        then(trainingInstanceRepository).should().findById(trainingInstance1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findByIdNotFound() {
        trainingInstanceService.findById(10L);
    }

    @Test
    public void findByIdIncludingDefinition() {
        given(trainingInstanceRepository.findByIdIncludingDefinition(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));
        TrainingInstance result = trainingInstanceService.findByIdIncludingDefinition(trainingInstance1.getId());
        deepEquals(trainingInstance1, result);
        assertEquals(trainingDefinition, result.getTrainingDefinition());
        then(trainingInstanceRepository).should().findByIdIncludingDefinition(trainingInstance1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findByIdIncludingDefinitionNotFound() {
        trainingInstanceService.findByIdIncludingDefinition(10L);
    }

    @Test
    public void findAll() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page<TrainingInstance> p = new PageImpl<TrainingInstance>(expected);
        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page<TrainingInstance> pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findAllByLoggedInUser() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page<TrainingInstance> p = new PageImpl<>(expected);
        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class), anyLong())).willReturn(p);

        Page<TrainingInstance> pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2), 1L);
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void createTrainingInstanceCreateOrganizer() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);

        TrainingInstance instance = trainingInstanceService.create(trainingInstance2);
        deepEquals(trainingInstance2, instance);
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }
    @Test
    public void createTrainingInstanceOrganizerIsCreated() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.findUserByUserRefId(user.getUserRefId())).willReturn(Optional.of(user));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(user.getUserRefId());

        TrainingInstance instance = trainingInstanceService.create(trainingInstance2);
        deepEquals(trainingInstance2, instance);
        assertEquals(userRefDTO.getUserRefFullName(), trainingInstance2.getLastEditedBy());
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }

    @Test(expected = EntityConflictException.class)
    public void createTrainingInstanceWithInvalidTimes() {
        trainingInstance1.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstance1.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(10L));
        trainingInstanceService.create(trainingInstance1);
    }

    @Test
    public void updateTrainingInstanceCreateOrganizer() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);
        given(trainingInstanceRepository.save(any(TrainingInstance.class))).willReturn(trainingInstance2);
        assertNotEquals(userRefDTO.getUserRefFullName(), trainingInstance2.getLastEditedBy());
        String token = trainingInstanceService.update(trainingInstance2);

        assertEquals(userRefDTO.getUserRefFullName(), trainingInstance2.getLastEditedBy());
        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test
    public void updateTrainingInstanceAlreadyCreatedOrganizer() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.findUserByUserRefId(user.getUserRefId())).willReturn(Optional.of(user));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(user.getUserRefId());
        given(trainingInstanceRepository.save(any(TrainingInstance.class))).willReturn(trainingInstance2);

        String token = trainingInstanceService.update(trainingInstance2);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test
    public void updateTrainingInstanceGenerateNewToken() {
        String newToken = "new-token";
        TrainingInstance trainingInstanceWithNewToken = new TrainingInstance();
        trainingInstanceWithNewToken.setId(trainingInstance2.getId());
        trainingInstanceWithNewToken.setAccessToken(newToken);
        trainingInstanceWithNewToken.setStartTime(trainingInstance2.getStartTime());
        trainingInstanceWithNewToken.setEndTime(trainingInstance2.getEndTime());
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.findUserByUserRefId(user.getUserRefId())).willReturn(Optional.of(user));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(user.getUserRefId());
        given(trainingInstanceRepository.save(any(TrainingInstance.class))).willReturn(trainingInstance2);

        String token = trainingInstanceService.update(trainingInstanceWithNewToken);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(any(TrainingInstance.class));
        then(accessTokenRepository).should().findOneByAccessToken(anyString());
        then(accessTokenRepository).should().saveAndFlush(any(AccessToken.class));
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingInstanceWithInvalidTimes() {
        trainingInstance1.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstance1.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(10L));
        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        trainingInstanceService.update(trainingInstance1);

        then(trainingInstanceRepository).should(never()).save(any(TrainingInstance.class));
    }

    @Test
    public void deleteTrainingInstance() {
        trainingInstanceService.delete(trainingInstance2);
        then(trainingInstanceRepository).should().delete(trainingInstance2);
    }

    @Test
    public void deleteTrainingInstanceById() {
        trainingInstanceService.deleteById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().deleteById(trainingInstance2.getId());
    }

    @Test
    public void findTrainingRunsByTrainingInstance() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> page = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(page);
        Page<TrainingRun> resultPage = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), null, PageRequest.of(0, 2));
        assertEquals(2, resultPage.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_OnlyActive() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> page = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllActiveByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(page);
        Page<TrainingRun> resultPage = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), true, PageRequest.of(0, 2));
        assertEquals(2, resultPage.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_OnlyInActive() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> page = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllInactiveByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(page);
        Page<TrainingRun> resultPage = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), false, PageRequest.of(0, 2));
        assertEquals(2, resultPage.getTotalElements());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findTrainingRunsByTrainingInstance_NotFound() {
        trainingInstanceService.findTrainingRunsByTrainingInstance(10L, null, PageRequest.of(0, 2));
    }

    @After
    public void after() {
        reset(trainingInstanceRepository);
    }

    private void deepEquals(TrainingInstance expected, TrainingInstance actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(object);
    }
}
