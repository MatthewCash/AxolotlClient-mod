package io.github.axolotlclient.mixin;

import io.github.axolotlclient.util.Hooks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public float yaw;

    @Shadow public float pitch;

    @Inject(method = "increaseTransforms", at = @At("HEAD"))
    private void updateLookDirection(float yaw, float pitch, CallbackInfo ci) {
        if(yaw == 0 && pitch == 0) {
            return;
        }

        float prevPitch = this.pitch;
        float prevYaw = this.yaw;
        pitch = (float)((double)prevPitch - (double)pitch * 0.15);
        yaw = (float)((double)prevYaw + (double)yaw * 0.15);
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
        Hooks.PLAYER_DIRECTION_CHANGE.invoker().onChange(prevPitch, prevYaw, pitch, yaw);
    }
}