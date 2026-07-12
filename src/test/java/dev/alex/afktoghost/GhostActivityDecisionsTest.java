package dev.alex.afktoghost;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostActivityDecisionsTest {
    @Test
    void emptyInputIsNotActivity() {
        assertFalse(GhostActivityDecisions.hasIntentionalMovementInput(0.0f, 0.0f, false, false));
    }

    @Test
    void directionalAndActionInputsAreActivity() {
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(1.0f, 0.0f, false, false));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(-1.0f, 0.0f, false, false));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(0.0f, 1.0f, false, false));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(0.0f, -1.0f, false, false));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(0.0f, 0.0f, true, false));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(0.0f, 0.0f, false, true));
    }

    @Test
    void positionOnlyMovementPacketsAreNotLookActivity() {
        assertFalse(GhostActivityDecisions.hasIntentionalLookChange(false, 90.0f, 30.0f, 0.0f, 0.0f));
    }

    @Test
    void smallRotationNoiseIsNotLookActivity() {
        assertFalse(GhostActivityDecisions.hasIntentionalLookChange(true, 0.05f, 0.05f, 0.0f, 0.0f));
    }

    @Test
    void lookRotationIsActivity() {
        assertTrue(GhostActivityDecisions.hasIntentionalLookChange(true, 2.0f, 0.0f, 0.0f, 0.0f));
        assertTrue(GhostActivityDecisions.hasIntentionalLookChange(true, 359.0f, 0.0f, 1.0f, 0.0f));
    }

    @Test
    void boatPaddleInputIsActivityOnlyWhenPaddling() {
        assertFalse(GhostActivityDecisions.hasIntentionalBoatPaddleInput(false, false));
        assertTrue(GhostActivityDecisions.hasIntentionalBoatPaddleInput(true, false));
        assertTrue(GhostActivityDecisions.hasIntentionalBoatPaddleInput(false, true));
    }
}
