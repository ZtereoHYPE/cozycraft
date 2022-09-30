package codes.ztereohype.example.mixin;

import codes.ztereohype.example.ExampleMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelRenderer.class)
public abstract class MixinStarRendering {
    @Shadow private VertexBuffer starBuffer;
    @Shadow private int ticks;
    @Shadow private ClientLevel level;

    @Inject(at = @At("HEAD"), method = "createStars", cancellable = true)
    private void generateStars(CallbackInfo ci) {
        ExampleMod.skyManager.generateSky(123L);
        starBuffer = new VertexBuffer();
        ExampleMod.skyManager.tick(ticks, starBuffer);
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tickStars(CallbackInfo ci) {
        if (this.level.getStarBrightness(0) < 0.0F) return;
        ExampleMod.skyManager.tick(ticks, starBuffer);
    }

    @ModifyArg(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lcom/mojang/math/Matrix4f;Lcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V", ordinal = 1),
            index = 2
    )
    private ShaderInstance injectStarColour(ShaderInstance shaderInstance) {
        return GameRenderer.getPositionColorShader();
    }

    @Inject(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableTexture()V", ordinal = 2, shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void drawSkybox(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean bl, Runnable skyFogSetup, CallbackInfo ci, FogType fogType, Vec3 vec3, float f, float g, float h, BufferBuilder bufferBuilder, ShaderInstance shaderInstance, float[] fs, float i, Matrix4f matrix4f2, float k, int r, int s, int m, float t, float o, float p, float q) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, level.getStarBrightness(0));
        ExampleMod.skyManager.getSkybox().render(poseStack, projectionMatrix);
    }
}
