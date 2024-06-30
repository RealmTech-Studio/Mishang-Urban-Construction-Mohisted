package pers.solid.mishang.uc.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.WorldEvent;
import net.minecraft.world.fluid.FluidState;
import net.minecraft.world.render.VertexConsumer;
import net.minecraft.world.render.VertexConsumerProvider;
import net.minecraft.world.render.WorldRendererInvoker;
import net.minecraft.world.render.RenderLayer;
import net.minecraft.world.render.shape.VoxelShapes;
import net.minecraft.text.Text;
import net.minecraft.world.BlockView;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ColumnBuildingTool extends Item {
    public static boolean suppressOnBlockAdded = false;

    public ColumnBuildingTool(Settings settings) {
        super(settings);
    }

    public ActionResult useOnBlock(ItemStack stack, PlayerEntity player, World world, BlockHitResult blockHitResult, Hand hand, boolean fluidIncluded) {
        blockPlacementContext.playSound();
        int flags = getFlags(stack);
        suppressOnBlockAdded = true;
        blockPlacementContext.setBlockState(flags);
        suppressOnBlockAdded = false;
        blockPlacementContext.setBlockEntity();
        return ActionResult.success(world.isClient);
    }

    public ActionResult beginAttackBlock(ItemStack stack, PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction, boolean fluidIncluded) {
        final BlockState blockState = world.getBlockState(pos);
        world.syncWorldEvent(player, 2001, pos, Block.getRawIdFromState(world.getBlockState(pos)));
        FluidState fluidState = blockState.getFluidState();
        world.removeBlockEntity(pos);
        int flags = getFlags(stack);
        world.setBlockState(pos, fluidIncluded ? Blocks.AIR.getDefaultState() : fluidState.getBlockState(), flags);
        return ActionResult.success(world.isClient);
    }

    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(TextBridge.translatable("item.mishanguc.force_placing_tool.tooltip.1").formatted(Formatting.GRAY));
        tooltip.add(TextBridge.translatable("item.mishanguc.force_placing_tool.tooltip.2").formatted(Formatting.GRAY));
        if (Boolean.TRUE.equals(includesFluid(stack))) {
            tooltip.add(TextBridge.translatable("item.mishanguc.force_placing_tool.tooltip.3").formatted(Formatting.GRAY));
        }
        if ((getFlags(stack) & 128) != 0) {
            tooltip.add(TextBridge.translatable("item.mishanguc.force_placing_tool.tooltip.4").formatted(Formatting.GRAY));
        }
    }

    public boolean renderBlockOutline(PlayerEntity player, ItemStack itemStack, WorldRenderContext worldRenderContext, WorldRenderContext.BlockOutlineContext blockOutlineContext, Hand hand) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if (!hasAccess(player, worldRenderContext.world(), false)) {
            return false;
        } else {
            final Item item = player.getMainHandStack().getItem();
            if (hand == Hand.OFF_HAND && (item instanceof BlockItem || item instanceof CarryingToolItem)) {
                return false;
            }
        }
        final VertexConsumerProvider consumers = worldRenderContext.consumers();
        if (consumers == null) {
            return false;
        }
        final VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.LINES);
        final BlockHitResult blockHitResult;
        final MatrixStack matrices = worldRenderContext.matrixStack();
        HitResult crosshairTarget = client.crosshairTarget;
        if (crosshairTarget instanceof BlockHitResult) {
            blockHitResult = (BlockHitResult) crosshairTarget;
        } else {
            return false;
        }
        final boolean includesFluid = this.includesFluid(itemStack, player.isSneaking());
        final BlockPlacementContext blockPlacementContext = new BlockPlacementContext(worldRenderContext.world(), blockOutlineContext.blockPos(), player, itemStack, blockHitResult, includesFluid);
        WorldRendererInvoker.drawCuboidShapeOutline(matrices, vertexConsumer, blockPlacementContext.stateToPlace.getOutlineShape(blockPlacementContext.world, blockPlacementContext.posToPlace, ShapeContext.of(player)), blockPlacementContext.posToPlace.getX() - blockOutlineContext.cameraX(), blockPlacementContext.posToPlace.getY() - blockOutlineContext.cameraY(), blockPlacementContext.posToPlace.getZ() - blockOutlineContext.cameraZ(), 0, 1, 1, 0.8f);
        if (includesFluid) {
            WorldRendererInvoker.drawCuboidShapeOutline(matrices, vertexConsumer, fluidState.getOutlineShape(blockPlacementContext.world, blockPlacementContext.posToPlace, ShapeContext.of(player)), blockPlacementContext.posToPlace.getX() - blockOutlineContext.cameraX(), blockPlacementContext.posToPlace.getY() - blockOutlineContext.cameraY(), blockPlacementContext.posToPlace.getZ() - blockOutlineContext.cameraZ(), 0, 1, 1, 0.8f);
        }
        return true;
    }

    public @NotNull ActionResult attackEntityCallback(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (!world.isClient) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.damage(DamageSource.player(player), 1.0F);
            }
            player.getItemCooldownManager().set(this, 10);
        }
        return ActionResult.success(world.isClient);
    }

    private static boolean hasAccess(PlayerEntity player, World world, boolean warn) {
        // Your access logic here
        return true;
    }

    public void renderBeforeOutline(WorldRenderContext context, HitResult hitResult, ClientPlayerEntity player, Hand hand) {
        final MatrixStack matrices = context.matrixStack();
        final VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;
        final VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.getLines());
        final Vec3d cameraPos = context.camera().getPos();
        if (hitResult instanceof EntityHitResult entityHitResult) {
            final Entity entity = entityHitResult.getEntity();
            WorldRendererInvoker.drawCuboidShapeOutline(matrices, vertexConsumer, VoxelShapes.cuboid(entity.getBoundingBox()), -cameraPos.x, -cameraPos.y, -cameraPos.z, 1.0f, 0f, 0f, 0.8f);
        }
    }

    public ModelJsonBuilder getItemModel() {
        // Your model building logic here
        return new ModelJsonBuilder();
    }
}
