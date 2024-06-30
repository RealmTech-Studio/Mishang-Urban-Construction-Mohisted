package pers.solid.mishang.uc.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.fluid.FluidState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.WorldView;
import net.minecraft.world.tick.OrderedTick;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.solid.mishang.uc.block.ColumnBuildingToolBlock;
import pers.solid.mishang.uc.entity.FallingBuildingBlockEntity;
import pers.solid.mishang.uc.item.rule.ForcePlacingToolItem;
import pers.solid.mishang.uc.registry.MishangucBlocks;
import pers.solid.mishang.uc.registry.MishangucEntities;
import pers.solid.mishang.uc.util.BoundingBoxUtils;
import pers.solid.mishang.uc.util.MishangucEntityComponents;
import pers.solid.mishang.uc.util.ModelJsonBuilder;
import pers.solid.mishang.uc.util.WorldRenderContext;
import pers.solid.mishang.uc.util.block.BlockHitResultUtils;
import pers.solid.mishang.uc.util.block.state.MishangucBlockState;

import java.util.List;

public class ColumnBuildingTool extends Item implements ForcePlacingToolItem {
    public ColumnBuildingTool(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // 添加物品提示信息
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // 使用物品的逻辑
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public boolean renderBlockOutline(PlayerEntity player, ItemStack itemStack, WorldRenderContext worldRenderContext, WorldRenderContext.BlockOutlineContext blockOutlineContext, Hand hand) {
        // 渲染物品轮廓的逻辑
        return true;
    }

    @Override
    public void renderBeforeOutline(WorldRenderContext context, HitResult hitResult, ClientPlayerEntity player, Hand hand) {
        // 渲染物品轮廓之前的逻辑
    }

    @Override
    public ModelJsonBuilder getItemModel() {
        // 获取物品模型的逻辑
        return new ModelJsonBuilder();
    }

    @Override
    public @NotNull ActionResult attackEntityCallback(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        // 攻击实体回调的逻辑
        return ActionResult.PASS;
    }
}
