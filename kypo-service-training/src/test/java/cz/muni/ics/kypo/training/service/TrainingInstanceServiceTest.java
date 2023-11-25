package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.AccessTokenRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = {TestDataFactory.class})
public class TrainingInstanceServiceTest {

    @Autowired
    TestDataFactory testDataFactory;
    private TrainingInstanceService trainingInstanceService;

    @MockBean
    private TrainingInstanceRepository trainingInstanceRepository;
    @MockBean
    private AccessTokenRepository accessTokenRepository;
    @MockBean
    private TrainingRunRepository trainingRunRepository;
    @MockBean
    private UserRefRepository organizerRefRepository;
    @MockBean
    private SecurityService securityService;
    @MockBean
    private UserService userService;
    @MockBean
    private TrainingDefinition trainingDefinition;
    private TrainingInstance trainingInstance1, trainingInstance2;
    private TrainingRun trainingRun1, trainingRun2;
    private UserRef user;
    private UserRefDTO userRefDTO;
    private LockedPoolInfo lockedPoolInfo;
    private PoolInfoDTO poolInfoDTO;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
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

        lockedPoolInfo = testDataFactory.getLockedPoolInfo();
        poolInfoDTO = testDataFactory.getPoolInfoDTO();

        given(userService.getUserRefFromUserAndGroup()).willReturn(userRefDTO);
    }

    @Test
    public void findById() {
        given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));

        TrainingInstance tI = trainingInstanceService.findById(trainingInstance1.getId());
        deepEquals(trainingInstance1, tI);
        then(trainingInstanceRepository).should().findById(trainingInstance1.getId());
    }

    @Test
    public void findById_NotFound() {
        assertThrows(EntityNotFoundException.class, () -> trainingInstanceService.findById(10L));
    }

    @Test
    public void findByIdIncludingDefinition() {
        given(trainingInstanceRepository.findByIdIncludingDefinition(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));
        TrainingInstance result = trainingInstanceService.findByIdIncludingDefinition(trainingInstance1.getId());
        deepEquals(trainingInstance1, result);
        assertEquals(trainingDefinition, result.getTrainingDefinition());
        then(trainingInstanceRepository).should().findByIdIncludingDefinition(trainingInstance1.getId());
    }

    @Test
    public void findByIdIncludingDefinition_NotFound() {
        assertThrows(EntityNotFoundException.class, () -> trainingInstanceService.findByIdIncludingDefinition(10L));
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
    public void createTrainingInstance_createAuthor() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);

        TrainingInstance tI = trainingInstanceService.create(trainingInstance2);
        deepEquals(trainingInstance2, tI);
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }
    @Test
    public void createTrainingInstance_authorIsCreated() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.findUserByUserRefId(user.getUserRefId())).willReturn(Optional.of(user));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(user.getUserRefId());

        TrainingInstance tI = trainingInstanceService.create(trainingInstance2);
        deepEquals(trainingInstance2, tI);
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }

    @Test
    public void createTrainingInstanceWithInvalidTimes() {
        trainingInstance1.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstance1.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(10L));
        assertThrows(EntityConflictException.class, () -> trainingInstanceService.create(trainingInstance1));
    }

    @Test
    public void createTrainingInstanceWithTrimmedAccessToken() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.findUserByUserRefId(user.getUserRefId())).willReturn(Optional.of(user));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(user.getUserRefId());

        // multiple spaces for emphasis
        String accessToken = "    chonk   ";
        given(accessTokenRepository.findOneByAccessToken(accessToken)).willReturn(null);
        trainingInstance2.setAccessToken(accessToken);
        trainingInstanceService.create(trainingInstance2);

        String accessTokenWithoutPin = trainingInstance2.getAccessToken().split("-")[0];
        assertEquals(accessToken.trim(), accessTokenWithoutPin);
    }

    @Test
    public void updateTrainingInstance_createUser() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);
        given(trainingInstanceRepository.save(any(TrainingInstance.class))).willReturn(trainingInstance2);

        String token = trainingInstanceService.update(trainingInstance2);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test
    public void updateTrainingInstance_alreadyCreatedUser() {
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
    public void updateTrainingInstanceWithInvalidTimes() {
        trainingInstance1.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstance1.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(10L));
        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        assertThrows(EntityConflictException.class, () -> trainingInstanceService.update(trainingInstance1));

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

//    @Test
//    public void lockPool() throws Exception {
//        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(lockedPoolInfo));
//        LockedPoolInfo result = trainingInstanceService.lockPool(lockedPoolInfo.getPoolId());
//        assertEquals(lockedPoolInfo, result);
//    }
//
//    @Test(expected = MicroserviceApiException.class)
//    public void lockPool_MicroserviceError() throws Exception {
//        willThrow(new CustomWebClientException(HttpStatus.CONFLICT, PythonApiError.of("Error when trying to lock pool."))).given(exchangeFunction).exchange(any(ClientRequest.class));
//        trainingInstanceService.lockPool(lockedPoolInfo.getPoolId());
//    }
//
//    @Test
//    public void unlockPool() throws Exception {
//        poolInfoDTO.setId(trainingInstance1.getPoolId());
//        ArgumentMatcher<ClientRequest> poolsRequest =
//                clientRequest -> clientRequest.url().equals(URI.create("/pools/" + poolInfoDTO.getId()));
//        ArgumentMatcher<ClientRequest> deleteRequest =
//                clientRequest -> clientRequest.url().equals(URI.create("/pools/"+ poolInfoDTO.getId() +"/locks/"+ poolInfoDTO.getLockId() ));
//        doReturn(buildMockResponse(poolInfoDTO)).when(exchangeFunction).exchange(argThat(poolsRequest));
//        doReturn(buildMockResponse(null)).when(exchangeFunction).exchange(argThat(deleteRequest));
//        trainingInstanceService.unlockPool(trainingInstance1.getPoolId());
//    }

//    @Test(expected = MicroserviceApiException.class)
//    public void unlockPool_GetLockIdMicroserviceError() throws Exception {
//        willThrow(new CustomWebClientException(HttpStatus.CONFLICT, PythonApiError.of("Cannot get lock id."))).given(exchangeFunction).exchange(any(ClientRequest.class));
//        trainingInstanceService.unlockPool(trainingInstance1.getPoolId());
//    }

    @Test
    public void findTrainingRunsByTrainingInstance() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page<TrainingRun> pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), null, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_OnlyActive() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllActiveByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page<TrainingRun> pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), true, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_OnlyInActive() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllInactiveByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page<TrainingRun> pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), false, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_NotFound() {
        assertThrows(EntityNotFoundException.class, () -> trainingInstanceService.findTrainingRunsByTrainingInstance(10L, null, PageRequest.of(0, 2)));
    }

    @AfterEach
    public void after() {
        reset(trainingInstanceRepository);
    }

    private void deepEquals(TrainingInstance expected, TrainingInstance actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(object);
    }

    private Mono<ClientResponse> buildMockResponse(Object body) throws IOException {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .body(convertObjectToJsonBytes(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }
}
