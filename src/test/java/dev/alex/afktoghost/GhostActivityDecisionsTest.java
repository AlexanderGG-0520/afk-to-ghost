package dev.alex.afktoghost;

import net.minecraft.world.entity.player.Input;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostActivityDecisionsTest {
    @Test
    void emptyInputIsNotActivity() {
        assertFalse(GhostActivityDecisions.hasIntentionalMovementInput(Input.EMPTY));
    }

    @Test
    void directionalAndActionInputsAreActivity() {
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(new Input(true, false, false, false, false, false, false)));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(new Input(false, false, false, false, true, false, false)));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(new Input(false, false, false, false, false, true, false)));
        assertTrue(GhostActivityDecisions.hasIntentionalMovementInput(new Input(false, false, false, false, false, false, true)));
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
