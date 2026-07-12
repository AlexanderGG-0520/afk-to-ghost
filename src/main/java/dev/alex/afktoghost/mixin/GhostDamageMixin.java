package dev.alex.afktoghost.mixin;

import dev.alex.afktoghost.AfkToGhostMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class GhostDamageMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void afkToGhost$blockGhostDamage(
            DamageSource damageSource,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (AfkToGhostMod.shouldBlockDamage(player)) {
            player.clearFire();
            player.resetFallDistance();
            cir.setReturnValue(false);
        }
    }
}
