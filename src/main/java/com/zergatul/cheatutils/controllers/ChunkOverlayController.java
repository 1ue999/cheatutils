package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.chunkoverlays.AbstractChunkOverlay;
import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.chunkoverlays.NewChunksOverlay;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.GuiUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.MouseScrollEvent;
import com.zergatul.cheatutils.wrappers.events.PostRenderGuiEvent;
import com.zergatul.cheatutils.wrappers.events.PreRenderGuiOverlayEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ChunkOverlayController {

    public static final ChunkOverlayController instance = new ChunkOverlayController();

    // store texture of 16x16 chunks
    private static final int SegmentSize = 16;
    // 250ms
    private static final long UpdateDelay = 250L * 1000000;
    private static final int TranslateZ = 250;
    private static final float MinScale = 1 * SegmentSize;
    private static final float MaxScale = 32 * SegmentSize;
    private static final float ScaleStep = 1.3f;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final List<AbstractChunkOverlay> overlays = new ArrayList<>();
    private float scale = 16 * SegmentSize;

    private ChunkOverlayController() {
        register(new ExplorationMiniMapChunkOverlay(SegmentSize, UpdateDelay));
        register(new NewChunksOverlay(SegmentSize, UpdateDelay));

        ChunkController.instance.addOnChunkLoadedHandler(this::onChunkLoaded);
        ChunkController.instance.addOnBlockChangedHandler(this::onBlockChanged);
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.PostRenderGui.add(this::render);
        ModApiWrapper.PreRenderGuiOverlay.add(this::onPreRenderGameOverlay);
        ModApiWrapper.MouseScroll.add(this::onMouseScroll);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractChunkOverlay> T ofType(Class<T> clazz) {
        return (T) overlays.stream().filter(o -> o.getClass() == clazz).findFirst().orElse(null);
    }

    private void render(PostRenderGuiEvent event) {
        if (!isSomeOverlayEnabled()) {
            return;
        }

        for (AbstractChunkOverlay overlay: overlays) {
            overlay.onPreRender();
        }

        if (!mc.options.playerListKey.isPressed()) {
            return;
        }

        if (Screen.hasAltDown()) {
            return;
        }

        if (mc.player == null || mc.world == null) {
            return;
        }

        float frameTime = event.getTickDelta();
        float xp = (float) MathHelper.lerp(frameTime, mc.player.prevX, mc.player.getX());
        float zp = (float) MathHelper.lerp(frameTime, mc.player.prevZ, mc.player.getZ());
        float xc = (float) mc.gameRenderer.getCamera().getPos().x;
        float zc = (float) mc.gameRenderer.getCamera().getPos().z;
        float yRot = mc.gameRenderer.getCamera().getYaw();

        event.getMatrixStack().push();
        event.getMatrixStack().loadIdentity();
        event.getMatrixStack().translate(1d * mc.getWindow().getScaledWidth() / 2, 1d * mc.getWindow().getScaledHeight() / 2, TranslateZ);
        event.getMatrixStack().multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(yRot));
        event.getMatrixStack().multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(180));
        event.getMatrixStack().multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(180));
        RenderSystem.applyModelViewMatrix();

        //RenderSystem.enableDepthTest();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        //RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 0.5f);

        float multiplier = 1f / (16 * SegmentSize) * scale;
        Dimension dimension = Dimension.get(mc.world);

        //RenderSystem.enableTexture();

        for (AbstractChunkOverlay overlay: overlays) {
            int z = overlay.getTranslateZ();
            for (AbstractChunkOverlay.Segment segment: overlay.getSegments(dimension)) {
                if (segment.texture == null) {
                    continue;
                }

                /**/
                //RenderSystem.bindTextureForSetup(segment.texture.getId());
                //RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                //RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                /**/

                RenderSystem.setShaderTexture(0, segment.texture.getGlId());

                float x = (segment.pos.x * 16 * SegmentSize - xc) * multiplier;
                float y = (segment.pos.z * 16 * SegmentSize - zc) * multiplier;

                GuiUtils.drawTexture(
                        event.getMatrixStack().peek().getPositionMatrix(),
                        x, y, scale, scale, z,
                        0, 0, 16 * SegmentSize, 16 * SegmentSize,
                        16 * SegmentSize, 16 * SegmentSize);
            }
        }

        for (AbstractChunkOverlay overlay: overlays) {
            overlay.onPostDrawSegments(dimension, event.getMatrixStack(), xp, zp, xc, zc, multiplier);
        }

        event.getMatrixStack().pop();
        RenderSystem.applyModelViewMatrix();
    }

    private void onPreRenderGameOverlay(PreRenderGuiOverlayEvent event) {
        if (event.getGuiOverlayType() == PreRenderGuiOverlayEvent.GuiOverlayType.PLAYER_LIST) {
            if (!isSomeOverlayEnabled()) {
                return;
            }
            if (Screen.hasAltDown()) {
                return;
            }
            event.cancel();
        }
    }

    private void onMouseScroll(MouseScrollEvent event) {
        if (!isSomeOverlayEnabled()) {
            return;
        }

        if (!mc.options.playerListKey.isPressed()) {
            return;
        }

        event.cancel();

        if (event.getScrollDelta() >= 1.0d) {
            if (scale < MaxScale) {
                scale *= ScaleStep;
            }
        }

        if (event.getScrollDelta() <= -1.0d) {
            if (scale > MinScale) {
                scale /= ScaleStep;
            }
        }
    }

    private void register(AbstractChunkOverlay overlay) {
        overlays.add(overlay);
    }

    private void onChunkLoaded(Dimension dimension, WorldChunk chunk) {
        for (AbstractChunkOverlay overlay: overlays) {
            overlay.onChunkLoaded(dimension, chunk);
        }
    }

    private void onBlockChanged(Dimension dimension, BlockPos pos, BlockState state) {
        for (AbstractChunkOverlay overlay: overlays) {
            overlay.onBlockChanged(dimension, pos, state);
        }
    }

    private void onClientTickEnd() {
        for (AbstractChunkOverlay overlay: overlays) {
            overlay.onClientTickEnd();
        }
    }

    private boolean isSomeOverlayEnabled() {
        return overlays.stream().anyMatch(AbstractChunkOverlay::isEnabled);
    }
}