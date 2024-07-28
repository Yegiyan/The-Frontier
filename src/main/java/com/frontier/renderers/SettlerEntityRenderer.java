package com.frontier.renderers;

import java.awt.Color;

import com.frontier.entities.settler.SettlerEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class SettlerEntityRenderer extends MobEntityRenderer<SettlerEntity, PlayerEntityModel<SettlerEntity>> 
{
    private final PlayerEntityModel<SettlerEntity> slim;
    private final PlayerEntityModel<SettlerEntity> normal;
    
    private final MinecraftClient client = MinecraftClient.getInstance();
    private static final double SHOW_NAME_DISTANCE = 10D;

    public SettlerEntityRenderer(EntityRendererFactory.Context context) 
    {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5F);
        normal = model;
        slim = new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER_SLIM), false);
    }
    
    @Override
    public void render(SettlerEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {
        String gender = entity.getSettlerGender();
        model = gender.equals("Male") ? normal : slim;
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
  
        double distance = client.player.distanceTo(entity);
        if (distance <= SHOW_NAME_DISTANCE)
        {
        	renderText(Text.literal("Current Goal"), Colors.WHITE, 0.80D, entity, matrices, vertexConsumers, light);
        	renderText(entity.getName(), new Color(255, 255, 255, 255).getRGB(), 0.50D, entity, matrices, vertexConsumers, light);
        }
    }

    @Override
    public Identifier getTexture(SettlerEntity entity)
    {
        int index = entity.getSettlerTexture();
        String gender = entity.getSettlerGender();
        String profession = entity.getSettlerProfession();
        
        switch(profession)
        {
        	case "Nomad":
        		if (gender.equals("Male"))
                    return new Identifier("frontier", "textures/entity/nomad/nomad_male_" + index + ".png");
                else
                    return new Identifier("frontier", "textures/entity/nomad/nomad_female_" + index + ".png");
        	case "Architect":
        		if (gender.equals("Male"))
                    return new Identifier("frontier", "textures/entity/architect/architect_male_" + index + ".png");
                else
                    return new Identifier("frontier", "textures/entity/architect/architect_female_" + index + ".png");
        } 
        return null;
    }
    
    private void renderText(Text text, int textColor, double height, SettlerEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {
    	if (MinecraftClient.getInstance().options.hudHidden)
            return;

        int textHighlightOpacity = new Color(0, 0, 0, 60).getRGB();

        matrices.push();
        matrices.translate(0.0D, entity.getHeight() + height, 0.0D);
        matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);

        TextRenderer textRenderer = this.getTextRenderer();
        float width = (float) (-textRenderer.getWidth(text) / 2);
        textRenderer.draw(text, width, 0, textColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, textHighlightOpacity, light);

        matrices.pop();
    }
}