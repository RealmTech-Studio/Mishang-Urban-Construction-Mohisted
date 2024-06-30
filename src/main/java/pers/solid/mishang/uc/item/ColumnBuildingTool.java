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
import net.minecraft.fluid.FluidState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.text.Text;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ColumnBuildingTool extends Item {
    public static boolean suppressOnBlockAdded = false;

    public ColumnBuildingTool(Settings settings) {
        super(settings);
    }

      @Override
  public ActionResult useOnBlock(ItemStack stack, PlayerEntity player, World world, BlockHitResult blockHitResult, Hand hand, boolean fluidIncluded) {
    if (!player.isCreative()) {
      // 仅限创造模式玩家使用。
      return ActionResult.PASS;
    }
    final Direction side = blockHitResult.getSide();
    final BlockPos originBlockPos = blockHitResult.getBlockPos();
   // final BlockPlacementContext blockPlacementContext = new BlockPlacementContext(world, originBlockPos, player, stack, blockHitResult, fluidIncluded);
    final int length = this.getLength(stack);
    boolean soundPlayed = false;
    final BlockPos.Mutable posToPlace = new BlockPos.Mutable().set(blockPlacementContext.posToPlace);
    if (blockPlacementContext.canPlace()) {
      for (int i = 0; i < length; i++) {
        if (world.getBlockState(posToPlace).canReplace(blockPlacementContext.placementContext)) {
          if (!world.isClient) {
           // world.setBlockState(posToPlace, blockPlacementContext.stateToPlace, 0b1011);
            BlockEntity entityToPlace = world.getBlockEntity(posToPlace);
            if (blockPlacementContext.stackInHand != null) {
             // BlockItem.writeNbtToBlockEntity(world, player, posToPlace, blockPlacementContext.stackInHand);
            } else if (blockPlacementContext.hitEntity != null && entityToPlace != null) {
              entityToPlace.readNbt(blockPlacementContext.hitEntity.createNbt());
              entityToPlace.markDirty();
              world.updateListeners(posToPlace, entityToPlace.getCachedState(), entityToPlace.getCachedState(), Block.NOTIFY_ALL);
            }
          }
          /*if (!soundPlayed) blockPlacementContext.playSound();
          soundPlayed = true;
        } else {
          posToPlace.move(side, -1);
          break;
        }*/
        posToPlace.move(side);
      } // end for
    }
   
    return ActionResult.SUCCESS;
  }

 
/*
  @Override
  public ActionResult beginAttackBlock(ItemStack stack, PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction, boolean fluidIncluded) {
    @Nullable BlockBox lastPlacedBox = null;
    @Nullable Block lastPlacedBlock = null;

    // 检查是否存在上次记录的区域。如果有，且点击的方块在该区域内，则直接删除这个区域的方块。
    // 注意：只要点击了，即使点击的位置不在该区域内，也会清除有关的记录。
    if (!world.isClient) {
      final Triple<ServerWorld, Block, BlockBox> pair = tempMemory.get(((ServerPlayerEntity) player));
      if (pair != null && pair.getLeft().equals(world) && pair.getRight().contains(pos)) {
        lastPlacedBox = pair.getRight();
        lastPlacedBlock = pair.getMiddle();
      }
      tempMemory.remove(player);
    } else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      if (clientTempMemory != null && clientTempMemory.getLeft().equals(world) && clientTempMemory.getRight().contains(pos)) {
        lastPlacedBox = clientTempMemory.getRight();
        lastPlacedBlock = clientTempMemory.getMiddle();
      }
      clientTempMemory = null;
    }
    if (lastPlacedBox != null && lastPlacedBlock != null && !world.isClient) {
      for (BlockPos posToRemove : BlockPos.iterate(lastPlacedBox.getMinX(), lastPlacedBox.getMinY(), lastPlacedBox.getMinZ(), lastPlacedBox.getMaxX(), lastPlacedBox.getMaxY(), lastPlacedBox.getMaxZ())) {
        final BlockState existingState = world.getBlockState(posToRemove);
        if (lastPlacedBlock.equals(existingState.getBlock()) && !(existingState.getBlock() instanceof OperatorBlock && !player.hasPermissionLevel(2))) {
          // 非管理员不应该破坏管理方块。
          if (fluidIncluded) {
            world.setBlockState(posToRemove, Blocks.AIR.getDefaultState());
          } else {
            world.removeBlock(posToRemove, false);
          }
        }
      }
      return ActionResult.SUCCESS;
    }
    return ActionResult.PASS;
  }

    public boolean renderBlockOutline(PlayerEntity player, ItemStack itemStack, MatrixStack matrices, VertexConsumerProvider consumers, BlockPos pos, VoxelShape shape) {
        // your render logic
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
        // your access logic here
        return true;
    }

    public void renderBeforeOutline(MatrixStack matrices, VertexConsumerProvider consumers, HitResult hitResult, PlayerEntity player, Hand hand) {
        final VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.getLines());
        final Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        if (hitResult instanceof EntityHitResult entityHitResult) {
            final Entity entity = entityHitResult.getEntity();
            WorldRenderer.drawBox(matrices, vertexConsumer, entity.getBoundingBox().offset(-cameraPos.x, -cameraPos.y, -cameraPos.z), 1.0f, 0f, 0f, 0.8f);
        }
    }

      */
   
}
