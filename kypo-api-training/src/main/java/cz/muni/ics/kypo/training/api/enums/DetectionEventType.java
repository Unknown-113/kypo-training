package cz.muni.ics.kypo.training.api.enums;

/**
 * The enumeration of Cheat types.
 */
public enum DetectionEventType {

    /**
     * cheat type representing two players having the same IP address.
     */
    LOCATION_SIMILARITY,
    /**
     * cheat type representing a player submitting an answer that
     * was generated for another player.
     */
    ANSWER_SIMILARITY,
    /**
     * cheat type representing a player solving a task in less than
     * minimal possible solve time.
     */
    MINIMAL_SOLVE_TIME,
    /**
     * cheat type representing two players submitting their levels
     * at similar times.
     */
    TIME_PROXIMITY,
    /**
     * cheat type representing a player not using any commands to solve
     * a task - this counts as a cheat only if the corresponding level
     * has value of XXX set to True.
     */
    NO_COMMANDS,
    /**
     * cheat type representing a player using commands that are forbidden.
     */
    FORBIDDEN_COMMANDS;
}