package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.converters.ReferenceSolutionConverter;

import javax.persistence.*;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Class specifying Abstract level as training level.
 * Training levels contain tasks for trainees to solve.
 */
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "training_level")
@PrimaryKeyJoinColumn(name = "id")
public class TrainingLevel extends AbstractLevel {

    @Column(name = "answer")
    private String answer;
    @Column(name = "answer_variable_name")
    private String answerVariableName;
    @Lob
    @Column(name = "content", nullable = false)
    private String content;
    @Lob
    @Column(name = "solution", nullable = false, columnDefinition = "TEXT")
    private String solution;
    @Column(name = "solution_penalized", nullable = false)
    private boolean solutionPenalized;
    @OneToMany(
            mappedBy = "trainingLevel",
            orphanRemoval = true
    )
    private Set<Attachment> attachments = new HashSet<>();
    @OneToMany(
            mappedBy = "trainingLevel",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Hint> hints = new HashSet<>();
    @Column(name = "reference_solution")
    @Convert(converter = ReferenceSolutionConverter.class)
    private Set<ReferenceSolutionNode> referenceSolution = new HashSet<>();
    @Column(name = "incorrect_answer_limit")
    private int incorrectAnswerLimit;
    @Column(name = "variant_answers", nullable = false)
    private boolean variantAnswers;
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE} , fetch = FetchType.LAZY)
    @JoinTable(
            name = "training_level_mitre_technique",
            joinColumns = { @JoinColumn(name = "training_level_id") },
            inverseJoinColumns = { @JoinColumn(name = "mitre_technique_id")}
    )
    private Set<MitreTechnique> mitreTechniques = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "expected_commands",
            joinColumns = @JoinColumn(name = "training_level_id")
    )
    private Set<ExpectedCommand> expectedCommands;
    @Column(name = "commands_required", nullable = false)
    private boolean commandsRequired = true;

    /**
     * Used to fix missing foreign key in the child (Hint) of @OneToMany association.
     * <p>
     * Hint entity was missing foreign key to TrainingLevel after persisting it.
     */
    @PrePersist
    private void prePersist() {
        hints.forEach(hint -> hint.setTrainingLevel(this));
    }

    /**
     * Gets answer that needs to be found by trainee to complete level
     *
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets answer that needs to be found by trainee to complete level
     *
     * @param answer the answer
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * Gets answer identifier that is used to obtain answer from remote storage.
     *
     * @return the answer identifier
     */
    public String getAnswerVariableName() {
        return answerVariableName;
    }

    /**
     * Sets answer identifier that is used to obtain answer from remote storage.
     *
     * @param answerVariableName the answer variable name
     */
    public void setAnswerVariableName(String answerVariableName) {
        this.answerVariableName = answerVariableName;
    }

    /**
     * Gets text assignment of task that needs to be performed by trainee
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets text assignment of task that needs to be performed by trainee
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets solution to the level that is shown if trainee fails or if they request it
     *
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }

    /**
     * Sets solution to the level that is shown if trainee fails or if they request it
     *
     * @param solution the solution
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    /**
     * Gets if solution is penalized. If true, points for solving level will be decreased to 1 after trainee displays solution
     *
     * @return the boolean
     */
    public boolean isSolutionPenalized() {
        return solutionPenalized;
    }

    /**
     * Sets if solution is penalized. If true, points for solving level will be decreased to 1 after trainee displays solution
     *
     * @param solutionPenalized the solution penalized
     */
    public void setSolutionPenalized(boolean solutionPenalized) {
        this.solutionPenalized = solutionPenalized;
    }

    /**
     * Gets attachments.
     *
     * @return the attachments
     */
    public Set<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments.
     *
     * @param attachments the attachments
     */
    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Add attachment.
     *
     * @param attachment the attachment
     */
    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
    }

    /**
     * Gets hints associated with training level
     *
     * @return the hints
     */
    public Set<Hint> getHints() {
        return Collections.unmodifiableSet(hints);
    }

    /**
     * Adds hint to be associated with training level
     *
     * @param hint the hint
     */
    public void addHint(Hint hint) {
        this.hints.add(hint);
    }

    /**
     * Sets hints associated with training level
     *
     * @param hints the hints
     */
    public void setHints(Set<Hint> hints) {
        this.hints = hints;
    }

    public Set<ReferenceSolutionNode> getReferenceSolution() {
        return referenceSolution;
    }

    public void setReferenceSolution(Set<ReferenceSolutionNode> referenceSolution) {
        this.referenceSolution = referenceSolution;
    }

    /**
     * Gets number of attempts available to trainee to input incorrect answer before the solution is displayed
     *
     * @return the incorrect answer limit
     */
    public int getIncorrectAnswerLimit() {
        return incorrectAnswerLimit;
    }

    /**
     * Sets number of attempts available to trainee to input incorrect answer before the solution is displayed
     *
     * @param incorrectAnswerLimit the incorrect answer limit
     */
    public void setIncorrectAnswerLimit(int incorrectAnswerLimit) {
        this.incorrectAnswerLimit = incorrectAnswerLimit;
    }

    /**
     * Gets if answer are different for each trainee.
     *
     * @return true if answers are variant, false otherwise
     */
    public boolean isVariantAnswers() {
        return variantAnswers;
    }

    /**
     * Sets if level answers are different for each trainee.
     *
     * @param variantAnswers true if answers are variant
     */
    public void setVariantAnswers(boolean variantAnswers) {
        this.variantAnswers = variantAnswers;
    }

    /**
     * Gets set of MITRE techniques used in the training level
     *
     * @return set of MITRE techniques
     */
    public Set<MitreTechnique> getMitreTechniques() {
        return mitreTechniques;
    }

    /**
     * Sets set of MITRE techniques used in the training level
     *
     * @param mitreTechniques set of MITRE techniques
     */
    public void setMitreTechniques(Set<MitreTechnique> mitreTechniques) {
        this.mitreTechniques = mitreTechniques;
    }

    public void addMitreTechnique(MitreTechnique mitreTechnique) {
        this.mitreTechniques.add(mitreTechnique);
        mitreTechnique.addTrainingLevel(this);
    }

    public void removeMitreTechnique(MitreTechnique mitreTechnique) {
        this.mitreTechniques.remove(mitreTechnique);
        mitreTechnique.removeTrainingLevel(this);
    }

    /**
     * Gets set of expected commands executed in the training level
     *
     * @return set of expected commands
     */
    public Set<ExpectedCommand> getExpectedCommands() {
        return expectedCommands;
    }

    /**
     * Sets set of expected commands executed in the training level
     *
     * @param expectedCommands set of expected commands
     */
    public void setExpectedCommands(Set<ExpectedCommand> expectedCommands) {
        this.expectedCommands = expectedCommands;
    }

    /**
     * Gets boolean if at least one command has to be executed to complete the training level
     *
     * @return true if commands are required, false otherwise
     */
    public boolean isCommandsRequired() {
        return commandsRequired;
    }

    /**
     * Sets a boolean if at least one command has to be executed to complete the training level
     *
     * @param commandsRequired boolean value
     */
    public void setCommandsRequired(boolean commandsRequired) {
        this.commandsRequired = commandsRequired;
    }
}
