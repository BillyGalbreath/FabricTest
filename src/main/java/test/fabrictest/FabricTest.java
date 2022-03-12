package test.fabrictest;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class FabricTest implements ClientModInitializer {
    private ShaderEffect testShader;
    private Framebuffer testBuffer;

    private final Identifier texture = new Identifier("fabrictest", "icon.png");

    private void setup(MinecraftClient client) {
        // copied the entity outline shader/framebuffer from WorldRenderer
        try {
            this.testShader = new ShaderEffect(client.getTextureManager(), client.getResourceManager(), client.getFramebuffer(), new Identifier("shaders/post/test.json"));
            this.testShader.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            this.testBuffer = this.testShader.getSecondaryTarget("final");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((matrixStack, delta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (this.testBuffer == null) {
                setup(client);
            }

            // none of this shit works.. commented it all out, so you can see what i'm drawing
            // to the screen. the ultimate goal of this test is to make the fragment shader
            // draw these two boxes pure white so i know the shader is working on _just_ those
            // two boxes and not the entire screen. if anyone can figure this out, let me know.

            //this.testBuffer.beginWrite(true);
            //this.testShader.render(delta);

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawBox(matrixStack, 50, 50, 100, 100, 0xFFFF00FF);
            drawTex(matrixStack, 110, 50, 160, 100);
            RenderSystem.disableBlend();

            //this.testBuffer.draw(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight(), false);
            //this.testShader.render(delta);

            //client.getFramebuffer().beginWrite(true);
            //this.testBuffer.beginRead();
        });
    }

    private void drawBox(MatrixStack matrixStack, float x0, float y0, float x1, float y1, int color) {
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(matrix, x1, y0, 0F).color(color).next();
        buf.vertex(matrix, x0, y0, 0F).color(color).next();
        buf.vertex(matrix, x0, y1, 0F).color(color).next();
        buf.vertex(matrix, x1, y1, 0F).color(color).next();
        buf.end();
        BufferRenderer.draw(buf);
        RenderSystem.enableTexture();
    }

    public void drawTex(MatrixStack matrixStack, float x0, float y0, float x1, float y1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);
        Matrix4f model = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(model, x0, y0, 0F).texture(0, 0).next();
        bufferBuilder.vertex(model, x0, y1, 0F).texture(0, 1).next();
        bufferBuilder.vertex(model, x1, y1, 0F).texture(1, 1).next();
        bufferBuilder.vertex(model, x1, y0, 0F).texture(1, 0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }
}
