package com.frontier.renderers;

import com.frontier.entities.settler.SettlerEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SettlerEntityRenderer extends MobEntityRenderer<SettlerEntity, PlayerEntityModel<SettlerEntity>> 
{
    private final PlayerEntityModel<SettlerEntity> slim;
    private final PlayerEntityModel<SettlerEntity> normal;

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
}
