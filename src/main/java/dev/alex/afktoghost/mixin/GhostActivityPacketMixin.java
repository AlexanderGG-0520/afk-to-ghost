package dev.alex.afktoghost.mixin;

import dev.alex.afktoghost.AfkToGhostMod;
import dev.alex.afktoghost.GhostActivityDecisions;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class GhostActivityPacketMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerInput", at = @At("HEAD"))
    private void afkToGhost$markKeyboardInput(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        if (GhostActivityDecisions.hasIntentionalMovementInput(
                packet.getXxa(), packet.getZza(), packet.isJumping(), packet.isShiftKeyDown())) {
            AfkToGhostMod.recordActivity(player, "player input");
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void afkToGhost$markLookInput(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (!packet.hasRotation()) {
            return;
        }

        float yaw = packet.getYRot(player.getYRot());
        float pitch = packet.getXRot(player.getXRot());
        if (GhostActivityDecisions.hasIntentionalLookChange(true, yaw, pitch, player.getYRot(), player.getXRot())) {
            AfkToGhostMod.recordActivity(player, "look input");
        }
    }

    @Inject(method = "handlePaddleBoat", at = @At("HEAD"))
    private void afkToGhost$markBoatPaddleInput(ServerboundPaddleBoatPacket packet, CallbackInfo ci) {
        if (GhostActivityDecisions.hasIntentionalBoatPaddleInput(packet.getLeft(), packet.getRight())) {
            AfkToGhostMod.recordActivity(player, "boat paddle input");
        }
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"))
    private void afkToGhost$markPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "player action");
    }

    @Inject(method = "handleSetCarriedItem", at = @At("HEAD"))
    private void afkToGhost$markHotbarSelection(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "hotbar selection");
    }

    @Inject(method = "handleAnimate", at = @At("HEAD"))
    private void afkToGhost$markSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "swing");
    }

    @Inject(method = "handlePlayerCommand", at = @At("HEAD"))
    private void afkToGhost$markPlayerCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "player command");
    }

    @Inject(method = "handleContainerClick", at = @At("HEAD"))
    private void afkToGhost$markContainerClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "container click");
    }

    @Inject(method = "handleContainerButtonClick", at = @At("HEAD"))
    private void afkToGhost$markContainerButton(ServerboundContainerButtonClickPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "container button");
    }

    @Inject(method = "handleContainerClose", at = @At("HEAD"))
    private void afkToGhost$markContainerClose(ServerboundContainerClosePacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "container close");
    }

    @Inject(method = "handlePlaceRecipe", at = @At("HEAD"))
    private void afkToGhost$markPlaceRecipe(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "place recipe");
    }

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"))
    private void afkToGhost$markCreativeSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "creative slot");
    }

    @Inject(method = "handleRenameItem", at = @At("HEAD"))
    private void afkToGhost$markRenameItem(ServerboundRenameItemPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "rename item");
    }

    @Inject(method = "handlePickItem", at = @At("HEAD"))
    private void afkToGhost$markPickItem(ServerboundPickItemPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "pick item");
    }

    @Inject(method = "handleEditBook", at = @At("HEAD"))
    private void afkToGhost$markEditBook(ServerboundEditBookPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "edit book");
    }

    @Inject(method = "handleSelectTrade", at = @At("HEAD"))
    private void afkToGhost$markSelectTrade(ServerboundSelectTradePacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "select trade");
    }

    @Inject(method = "handleSetBeaconPacket", at = @At("HEAD"))
    private void afkToGhost$markSetBeacon(ServerboundSetBeaconPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "set beacon");
    }

    @Inject(method = "handlePlayerAbilities", at = @At("HEAD"))
    private void afkToGhost$markPlayerAbilities(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordActivity(player, "player abilities");
    }

    @Inject(method = "handleClientInformation", at = @At("HEAD"))
    private void afkToGhost$captureLocale(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        AfkToGhostMod.recordLocale(player, packet.language());
    }

}
