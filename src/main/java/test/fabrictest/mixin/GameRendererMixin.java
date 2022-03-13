package test.fabrictest.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gl.Program;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import test.fabrictest.FabricTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "loadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;clearShaders()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void registerShaders(ResourceManager resourceManager, CallbackInfo ci, List<Program> programList, List<Pair<Shader, Consumer<Shader>>> shaderList) {
        List<Pair<Shader, Consumer<Shader>>> extraShaderList = new ArrayList<>();
        try {
            extraShaderList.add(Pair.of(new Shader(resourceManager, "test_color", VertexFormats.POSITION_COLOR), shader -> FabricTest.colorShader = shader));
            extraShaderList.add(Pair.of(new Shader(resourceManager, "test_tex", VertexFormats.POSITION_TEXTURE), shader -> FabricTest.texShader = shader));
        } catch (IOException e) {
            extraShaderList.forEach(pair -> pair.getFirst().close());
            throw new RuntimeException("could not reload shaders", e);
        }
        shaderList.addAll(extraShaderList);
    }
}
