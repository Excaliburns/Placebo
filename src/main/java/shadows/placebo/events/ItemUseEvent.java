package shadows.placebo.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.LogicalSide;

/**
 * This event is fired when an item would be used via {@link Item#useOn}<br>
 * It allows for the usage to be changed or cancelled.<br>
 * This event allows finer-tuned control over the actual item usage that cannot be achieved by using {@link RightClickBlock}
 */
@Cancelable
public class ItemUseEvent extends PlayerEvent {

	private final InteractionHand hand;
	private final BlockPos pos;
	@Nullable
	private final Direction face;
	private InteractionResult cancellationResult = InteractionResult.PASS;
	private final UseOnContext ctx;

	public ItemUseEvent(UseOnContext ctx) {
		super(Preconditions.checkNotNull(ctx.getPlayer(), "Null player in PlayerInteractEvent!"));
		this.hand = Preconditions.checkNotNull(ctx.getHand(), "Null hand in PlayerInteractEvent!");
		this.pos = Preconditions.checkNotNull(ctx.getClickedPos(), "Null position in PlayerInteractEvent!");
		this.face = ctx.getClickedFace();
		this.ctx = ctx;
	}

	public UseOnContext getContext() {
		return this.ctx;
	}

	/**
	 * @return The hand involved in this interaction. Will never be null.
	 */
	@Nonnull
	public InteractionHand getHand() {
		return hand;
	}

	/**
	 * @return The itemstack involved in this interaction, {@code ItemStack.EMPTY} if the hand was empty.
	 */
	@Nonnull
	public ItemStack getItemStack() {
		return getEntity().getItemInHand(hand);
	}

	/**
	 * If the interaction was on an entity, will be a BlockPos centered on the entity.
	 * If the interaction was on a block, will be the position of that block.
	 * Otherwise, will be a BlockPos centered on the player.
	 * Will never be null.
	 * @return The position involved in this interaction.
	 */
	@Nonnull
	public BlockPos getPos() {
		return pos;
	}

	/**
	 * @return The face involved in this interaction. For all non-block interactions, this will return null.
	 */
	@Nullable
	public Direction getFace() {
		return face;
	}

	/**
	 * @return Convenience method to get the world of this interaction.
	 */
	public Level getWorld() {
		return getEntity().getCommandSenderWorld();
	}

	/**
	 * @return The effective, i.e. logical, side of this interaction. This will be {@link LogicalSide#CLIENT} on the client thread, and {@link LogicalSide#SERVER} on the server thread.
	 */
	public LogicalSide getSide() {
		return getWorld().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
	}

	/**
	 * @return The InteractionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
	 * method of the event. By default, this is {@link InteractionResult#PASS}, meaning cancelled events will cause
	 * the client to keep trying more interactions until something works.
	 */
	public InteractionResult getCancellationResult() {
		return cancellationResult;
	}

	/**
	 * Set the InteractionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
	 * method of the event.
	 * Note that this only has an effect on {@link RightClickBlock}, {@link RightClickItem}, {@link EntityInteract}, and {@link EntityInteractSpecific}.
	 */
	public void setCancellationResult(InteractionResult result) {
		this.cancellationResult = result;
	}
}
