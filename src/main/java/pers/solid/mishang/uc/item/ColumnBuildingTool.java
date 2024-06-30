package pers.solid.mishang.uc.item;
import net.minecraft.model.ModelJsonBuilder;
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

    public ActionResult useOnBlock(ItemStack stack, PlayerEntity player, World world, BlockHitResult blockHitResult, Hand hand, boolean fluidIncluded) {
        // your block placement logic
        return ActionResult.success(world.isClient);
    }

    public ActionResult beginAttackBlock(ItemStack stack, PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction, boolean fluidIncluded) {
        // your block attack logic
        return ActionResult.success(world.isClient);
    }

    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // your tooltip logic
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

    public ModelJsonBuilder getItemModel() {
        // your model building logic here
        return new ModelJsonBuilder();
    }
}
