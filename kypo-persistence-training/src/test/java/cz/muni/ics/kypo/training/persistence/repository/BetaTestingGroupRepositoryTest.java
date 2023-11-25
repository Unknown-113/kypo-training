package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.BetaTestingGroup;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class BetaTestingGroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BetaTestingGroupRepository viewGroupRepository;

    private BetaTestingGroup viewGroup1, viewGroup2;
    private TrainingDefinition trainingDefinition1, trainingDefinition2;

    @BeforeEach
    public void init() {
        viewGroup1 = new BetaTestingGroup();

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setTitle("Best training definition");
        trainingDefinition1.setState(TDState.UNRELEASED);
        trainingDefinition1.setBetaTestingGroup(viewGroup1);
        trainingDefinition1.setLastEdited(LocalDateTime.now());
        trainingDefinition1.setLastEditedBy("Jane Doe");
        trainingDefinition1.setCreatedAt(LocalDateTime.now());

        viewGroup2 = new BetaTestingGroup();

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setTitle("Very good training definition");
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setBetaTestingGroup(viewGroup2);
        trainingDefinition2.setLastEdited(LocalDateTime.now());
        trainingDefinition2.setLastEditedBy("John Doe");
        trainingDefinition2.setCreatedAt(LocalDateTime.now());

        entityManager.persist(trainingDefinition1);
        entityManager.persist(trainingDefinition2);
    }

    @Test
    public void findById() {
        Optional<BetaTestingGroup> betaTestingGroupOptional = viewGroupRepository.findById(viewGroup2.getId());
        assertTrue(betaTestingGroupOptional.isPresent());
        assertEquals(viewGroup2, betaTestingGroupOptional.get());

        Optional<BetaTestingGroup> tdViewGroupOptional2 = viewGroupRepository.findById(viewGroup1.getId());
        assertTrue(tdViewGroupOptional2.isPresent());
        assertEquals(viewGroup1, tdViewGroupOptional2.get());

    }
}
