package pers.solid.mishang.uc.util;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.solid.mishang.uc.MishangUtils;
//import pers.solid.mishang.uc.item.CarryingToolItem;
import pers.solid.mishang.uc.mixin.BucketItemAccessor;
import pers.solid.mishang.uc.mixin.ItemUsageContextInvoker;

import java.util.Objects;

/**
 * 用于预测方块放置时的位置以及方块状态，同时处理方块实体。主要用于强制放置工具和快速建造工具，既可用于放置和破坏，也可用于相应的轮廓绘制。
 */
public class BlockPlacementContext {
  public final @NotNull World world;
  public final @NotNull BlockPos blockPos;
  public final @NotNull PlayerEntity player;
  public final @NotNull ItemStack stack;
  public final @NotNull BlockHitResult hit;
  /**
   * {@link #hit} 中的方块状态。<br>
   * The {@link BlockState} in the {@link #hit}.
   */
  public final @NotNull BlockState hitState;
  /**
   * {@link #hit} 中的方块实体。<br>
   * The {@link BlockEntity} in the {@link #hit}.
   */
  public final @Nullable BlockEntity hitEntity;
  /**
   * 放置之前，{@link #posToPlace} 位置处的方块。该方块将会被 {@link #stateToPlace} 替换掉。<br>
   * The block at {@link #posToPlace} before placing. The block will be replaced with {@link
   * #stateToPlace}.
   */
  public final @NotNull BlockState stateToReplace;
  /**
   * 是否会连同流体一起放置与破坏。
   */
  public final boolean includesFluid;


  public final @NotNull ItemPlacementContext placementContext;
  /**
   * 如果需要放置方块，则方块放置在此位置。<br>
   * The {@link BlockPos} to place the block if to place it.
   */
  public final @NotNull BlockPos posToPlace;
  /**
   * 需要放置的方块状态。<br>
   * The {@link BlockState} to place in the {@link #posToPlace}.
   */
  public final @NotNull BlockState stateToPlace;
  /**
   * 拿着方块物品的手。<br>
   * The hand that holds the BlockItem.
   */
  public @Nullable Hand hand;
  /**
   * 手中的物品堆。该物品堆的物品必须是方块物品，或者是 {@link CarryingToolItem}。如果手中的物品堆是空的，或者不是方块，则该值为 {@code null}。<br>
   * The {@link ItemStack} in the {@code hand}. The item in the <code>ItemStack</code> must be a
   * {@link BlockItem} or {@link CarryingToolItem}. If the item stack in hand is not block item, or is null, then the value is {@code null}.
   */
  public final @Nullable ItemStack stackInHand;

  /**
   * 请留意这个 {@link #player} 如果是 <code>null</code> 将会抛出异常！因此构造时请一定留意！ Please pay attention when
   * constructing because it throws exceptions when {@link #player} is <code>null</code>!
   */
  public BlockPlacementContext(ItemUsageContext context, boolean includesFluid) {
    this(
        context.getWorld(),
        context.getBlockPos(),
        Objects.requireNonNull(context.getPlayer()),
        context.getStack(),
        ((ItemUsageContextInvoker) context).invokeGetHitResult(),
        includesFluid);
  }

  /**
   * 根据已有的 {@link BlockPlacementContext}，获得一个偏移到 <code>offsetPos</code> 坐标处的新的 <code>
   * BlockPlacementContext</code>. <br>
   * Get a new {@link BlockPlacementContext} from an old context with an <code>offsetPos</code>.
   */
  public BlockPlacementContext(@NotNull BlockPlacementContext old, @NotNull BlockPos offsetPos) {
    this(
        old.world,
        offsetPos,
        old.player,
        old.stack,
        new BlockHitResult(old.hit.getPos().add(
            offsetPos.getX() - old.hit.getBlockPos().getX(),
            offsetPos.getY() - old.hit.getBlockPos().getY(),
            offsetPos.getZ() - old.hit.getBlockPos().getZ()), old.hit.getSide(), offsetPos, old.hit.isInsideBlock()),
        old.includesFluid);
  }

  public BlockPlacementContext(
      World world,
      @NotNull BlockPos blockPos,
      @NotNull PlayerEntity player,
      @NotNull ItemStack stack,
      BlockHitResult hit,
      boolean includesFluid) {
    this.world = world;
    this.blockPos = blockPos;
    this.player = player;
    this.stack = stack;
    this.hit = hit;
    this.includesFluid = includesFluid;

    // 需要被替换的方块
    hitState = world.getBlockState(hit.getBlockPos());
    hitEntity = world.getBlockEntity(hit.getBlockPos());

    // 需要放置的方块
    @Nullable BlockState stateToPlace1 = null;
    @Nullable ItemStack stackInHand1 = null;
    ItemPlacementContext placementContext1 = null;

    for (@NotNull Hand hand1 : Hand.values()) {
      ItemStack stackInHand0 = this.player.getStackInHand(hand1);
      if (stackInHand0.getItem() instanceof final BlockItem blockItem) {
        // 若手中持有方块物品，则 stateToPlace 为该物品
        /*
          手中物品堆中的方块物品对应的方块。
         */
        final @Nullable Block handBlock = blockItem.getBlock();
        placementContext1 = new ItemPlacementContext(player, hand1, stackInHand0, hit);
        stateToPlace1 = handBlock == null ? null : handBlock.getPlacementState(placementContext1);
        if (stateToPlace1 == null) {
          placementContext1 = null;
          continue;
        }

        // 尝试 placeFromTag
        final NbtCompound blockStateTag = stackInHand0.getSubNbt("BlockStateTag");
        if (blockStateTag != null) {
          final StateManager<Block, BlockState> stateManager = handBlock.getStateManager();
          for (String key : blockStateTag.getKeys()) {
            final Property<?> property = stateManager.getProperty(key);
            if (property != null) {
              stateToPlace1 = MishangUtils.with(stateToPlace1, property, blockStateTag.getString(key));
            }
          }
        }
        stackInHand1 = stackInHand0;
        hand = hand1;
        break;
      } else if (stackInHand0.getItem() instanceof FlintAndSteelItem) {
        stateToPlace1 = Blocks.FIRE.getDefaultState();
      } else if (stackInHand0.getItem() instanceof BucketItem bucketItem) {
        stateToPlace1 = ((BucketItemAccessor) bucketItem).getFluid().getDefaultState().getBlockState();
      }
    }

    stackInHand = stackInHand1;
    placementContext = placementContext1 == null ? new ItemPlacementContext(player, hand, hitState.getBlock().asItem().getDefaultStack(), hit) : placementContext1;
    final boolean tweakSlabPlacement;
    if (placementContext.getStack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SlabBlock) {
      tweakSlabPlacement = !BlockPos.ofFloored(hit.getPos().offset(hit.getSide(), 0.25)).equals(hit.getBlockPos());
    } else {
      tweakSlabPlacement = false;
    }
    posToPlace = (includesFluid || tweakSlabPlacement) ? blockPos.offset(hit.getSide()) : placementContext.getBlockPos();
    stateToReplace = world.getBlockState(posToPlace);
    if (stateToPlace1 == null) {
      // 手中没有有效的方块物品，则使用 hitState。
      boolean canReplaceExisting = placementContext.canReplaceExisting() && !includesFluid;
      stateToPlace1 = canReplaceExisting ? hitState.getBlock().getPlacementState(placementContext) : null;
    }
    if (stateToPlace1 == null) {
      stateToPlace1 = hitState;
    }

    // 尝试放置含水
    if (!includesFluid && stateToPlace1.getProperties().contains(Properties.WATERLOGGED)) {
      stateToPlace1 = stateToPlace1.with(Properties.WATERLOGGED, stateToReplace.getFluidState().getFluid() == Fluids.WATER);
    }

    // 对台阶进行修改
    if (tweakSlabPlacement && stateToPlace1.contains(SlabBlock.TYPE) && stateToPlace1.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
      if (hitState.getBlock() instanceof SlabBlock && hitState.contains(SlabBlock.TYPE)) {
        stateToPlace1 = stateToPlace1.with(SlabBlock.TYPE, hitState.get(SlabBlock.TYPE));
      } else {
        stateToPlace1 = stateToPlace1.with(SlabBlock.TYPE, (placementContext.getHitPos().y - blockPos.getY() > 0.5) ? SlabType.TOP : SlabType.BOTTOM);
      }
    }

    // 此时终于确定好了 stateToPlace
    this.stateToPlace = stateToPlace1;
  }

  /**
   * 放置方块。
   */
  public boolean setBlockState(int flags) {
    return world.setBlockState(posToPlace, stateToPlace, flags);
  }

  /**
   * 放置方块实体。
   */
  public void setBlockEntity() {
    BlockEntity entityToPlace = world.getBlockEntity(posToPlace);
    if (stackInHand != null) {
      BlockItem.writeNbtToBlockEntity(world, player, posToPlace, stackInHand);
    } else if (hitEntity != null && entityToPlace != null) {
      entityToPlace.readNbt(hitEntity.createNbt());
      entityToPlace.markDirty();
      world.updateListeners(posToPlace, entityToPlace.getCachedState(), entityToPlace.getCachedState(), Block.NOTIFY_ALL);
    }
  }

  /**
   * 检查方块能否放置。例如，如果 {@link #stateToPlace} 是花，且 {@link #posToPlace} 是悬在半空中的，则该方法返回 <code>false</code>
   * 。<br>
   * Checks if the {@link #stateToPlace} can be placed at {@link #posToPlace}. For example, if
   * {@link #stateToPlace} is a flower, and {@link #posToPlace} is hung in the midair, the methods
   * returns <code>false</code>.<br>
   * Calls {@link BlockState#canPlaceAt}.
   */
  public boolean canPlace() {
    if (stateToPlace.getBlock() instanceof OperatorBlock && !player.hasPermissionLevel(2)) {
      return false;
    }
    return stateToPlace.canPlaceAt(world, posToPlace);
  }

  /**
   * 检查方块能否被替换。例如，如果 {@link #stateToReplace} 是草或者水，则返回 <code>true</code>。<br>
   * Checks if the {@link #stateToReplace} can be replaced in the placement-context. For example, if
   * the {@link #stateToReplace} is a grass or water, then it returns <code>true</code>.<br>
   * Calls {@link BlockState#canReplace}.
   */
  public boolean canReplace() {
    return stateToReplace.canReplace(placementContext);
  }

  /**
   * 播放声音。该函数不会检查 {@link #world} 是否为客户端世界，需要在调用时手动检查。<br>
   * Play the sound of the block placed. Does not check if {@link #world} is client world. You may
   * manually check it.
   */
  public void playSound() {
    BlockSoundGroup blockSoundGroup = stateToPlace.getSoundGroup();
    world.playSound(
        player,
        posToPlace,
        blockSoundGroup.getPlaceSound(),
        SoundCategory.BLOCKS,
        (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
        blockSoundGroup.getPitch() * 0.8F);
  }
}
