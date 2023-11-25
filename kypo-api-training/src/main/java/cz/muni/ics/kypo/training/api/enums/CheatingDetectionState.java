package cz.muni.ics.kypo.training.api.enums;

public enum CheatingDetectionState {
    /**
     * represents a queued up state.
     */
    QUEUED,
    /**
     * represents a running state.
     */
    RUNNING,
    /**
     * represents a disabled state(for disabled detection methods).
     */
    DISABLED,
    /**
     * represents a finished state.
     */
    FINISHED,
}
