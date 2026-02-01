// https://github.com/kkllffaa/meteor-litematica-printer/blob/main/src/main/java/com/kkllffaa/meteor_litematica_printer/Printer.java
package com.LitematicaPrinterPlus.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
/// ^^^ MODULE BASIC IMPORTS ^^^

import com.LitematicaPrinterPlus.addon.BuildUtils; 

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.client.option.KeyBinding;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import net.minecraft.client.input.KeyInput;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import net.minecraft.client.util.InputUtil;

public class Printer extends Module {
	private final SettingGroup sgGeneral = settings.getDefaultGroup();
	private final SettingGroup sgWhitelist = settings.createGroup("Whitelist");
    private final SettingGroup sgRendering = settings.createGroup("Rendering");

	private final Setting<Integer> printing_range = sgGeneral.add(new IntSetting.Builder()
			.name("printing-range")
			.description("The block place range.")
			.defaultValue(2)
			.min(1).sliderMin(1)
			.max(6).sliderMax(6)
			.build()
	);

	private final Setting<Integer> printing_delay = sgGeneral.add(new IntSetting.Builder()
			.name("printing-delay")
			.description("Delay between printing blocks in ticks.")
			.defaultValue(2)
			.min(0).sliderMin(0)
			.max(100).sliderMax(40)
			.build()
	);

	private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
			.name("blocks/tick")
			.description("How many blocks place per tick.")
			.defaultValue(1)
			.min(1).sliderMin(1)
			.max(100).sliderMax(100)
			.build()
	);
	
	private final Setting<Integer> min_blocks_inv = sgGeneral.add(new IntSetting.Builder()
			.name("items to leave in inventory")
			.description("Module wouldn't use every item from the slot. Leaving the slot at some (recommended) low amount of items. Intended for refill purposes")
			.defaultValue(-1)
			.min(-1).sliderMin(-1)
			.max(65).sliderMax(65)
			.build()
	);

	private final Setting<Boolean> advanced = sgGeneral.add(new BoolSetting.Builder()
			.name("advanced")
			.description("Respect block rotation (places blocks in weird places in singleplayer, multiplayer should work fine).")
			.defaultValue(false)
			.build()
	);
	
	private final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
			.name("air-place")
			.description("Allow the bot to place in the air.")
			.defaultValue(true)
			.build()
	);
	
	private final Setting<Boolean> placeThroughWall = sgGeneral.add(new BoolSetting.Builder()
			.name("Place Through Wall")
			.description("Allow the bot to place through walls.")
			.defaultValue(true)
			.build()
	);

	private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
			.name("swing")
			.description("Swing hand when placing.")
			.defaultValue(false)
			.build()
	);

    private final Setting<Boolean> returnHand = sgGeneral.add(new BoolSetting.Builder()
			.name("return-slot")
			.description("Return to old slot.")
			.defaultValue(false)
			.build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
			.name("rotate")
			.description("Rotate to the blocks being placed.")
			.defaultValue(false)
			.build()
    );

    private final Setting<Boolean> clientSide = sgGeneral.add(new BoolSetting.Builder()
			.name("Client side Rotation")
			.description("Rotate to the blocks being placed on client side.")
			.defaultValue(false)
			.visible(rotate::get)
			.build()
    );
	
	private final Setting<Boolean> stopOnPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("stop-on-place")
        .description("Player stops when placing blocks")
        .defaultValue(false)
        .build()
    );
	
	private final Setting<Boolean> heatMode = sgGeneral.add(new BoolSetting.Builder()
        .name("heat-mode")
        .description("todo")
        .defaultValue(false)
		.visible(stopOnPlace::get)
        .build()
    );
	
	private final Setting<Integer> heatLimit = sgGeneral.add(new IntSetting.Builder()
			.name("heat-limit")
			.description("to-do")
			.defaultValue(10)
			.visible(heatMode::get)
			.min(0).sliderMin(0)
			.max(10000).sliderMax(100)
			.build()
	);
	
	private final Setting<Integer> priority = sgGeneral.add(new IntSetting.Builder()
			.name("rotation priority")
			.description("")
			.defaultValue(50)
			.visible(rotate::get)
			.min(-1000 * 1000).sliderMin(-1)
			.max(1000 * 1000).sliderMax(100)
			.build()
	);

	private final Setting<Boolean> dirtgrass = sgGeneral.add(new BoolSetting.Builder()
			.name("dirt-as-grass")
			.description("Use dirt instead of grass.")
			.defaultValue(true)
			.build()
	);

    private final Setting<SortAlgorithm> firstAlgorithm = sgGeneral.add(new EnumSetting.Builder<SortAlgorithm>()
			.name("first-sorting-mode")
			.description("The blocks you want to place first.")
			.defaultValue(SortAlgorithm.None)
			.build()
	);

    private final Setting<SortingSecond> secondAlgorithm = sgGeneral.add(new EnumSetting.Builder<SortingSecond>()
			.name("second-sorting-mode")
			.description("Second pass of sorting eg. place first blocks higher and closest to you.")
			.defaultValue(SortingSecond.None)
			.visible(()-> firstAlgorithm.get().applySecondSorting)
			.build()
	);

    private final Setting<Boolean> whitelistenabled = sgWhitelist.add(new BoolSetting.Builder()
			.name("whitelist-enabled")
			.description("Only place selected blocks.")
			.defaultValue(false)
			.build()
	);

    private final Setting<List<Block>> whitelist = sgWhitelist.add(new BlockListSetting.Builder()
			.name("whitelist")
			.description("Blocks to place.")
			.visible(whitelistenabled::get)
			.build()
	);

    private final Setting<Boolean> renderBlocks = sgRendering.add(new BoolSetting.Builder()
        .name("render-placed-blocks")
        .description("Renders block placements.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRendering.add(new IntSetting.Builder()
        .name("fade-time")
        .description("Time for the rendering to fade, in ticks.")
        .defaultValue(3)
        .min(1).sliderMin(1)
        .max(1000).sliderMax(20)
        .visible(renderBlocks::get)
        .build()
    );

    private final Setting<SettingColor> colour = sgRendering.add(new ColorSetting.Builder()
        .name("colour")
        .description("The cubes colour.")
        .defaultValue(new SettingColor(95, 190, 255))
        .visible(renderBlocks::get)
        .build()
    );

    private int timer;
    private int usedSlot = -1;
    private final List<BlockPos> toSort = new ArrayList<>();
    private final List<Pair<Integer, BlockPos>> placed_fade = new ArrayList<>();
	private int heat = 0;

	// TODO: Add an option for smooth rotation. Make it look legit. 
	// Might use liquidbounce RotationUtils to make it happen.	
	// https://github.com/CCBlueX/LiquidBounce/blob/nextgen/src/main/kotlin/net/ccbluex/liquidbounce/utils/aiming/RotationsUtil.kt#L257

	public Printer(Category cat) {
		super(cat, "litematica-printer-plus", "Automatically prints open schematics");
	}

    @Override
    public void onActivate() {
        onDeactivate();
    }

	@Override
    public void onDeactivate() {
		placed_fade.clear();
	}

	@EventHandler
	private void onTick(TickEvent.Post event) {
		
		
		
		if (mc.player == null || mc.world == null) {
			placed_fade.clear();
			return;
		}
		
		if (stopOnPlace.get()){
		
			set(mc.options.forwardKey, Input.isPressed(mc.options.forwardKey));
			set(mc.options.backKey, Input.isPressed(mc.options.backKey));
			set(mc.options.leftKey, Input.isPressed(mc.options.leftKey));
			set(mc.options.rightKey, Input.isPressed(mc.options.rightKey));

			set(mc.options.jumpKey, Input.isPressed(mc.options.jumpKey));
			set(mc.options.sneakKey, Input.isPressed(mc.options.sneakKey));
			set(mc.options.sprintKey, Input.isPressed(mc.options.sprintKey));
		}
		placed_fade.forEach(s -> s.setLeft(s.getLeft() - 1));
		placed_fade.removeIf(s -> s.getLeft() <= 0);

		WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
		if (worldSchematic == null) {
			placed_fade.clear();
			toggle();
			return;
		}

		toSort.clear();


		if (timer >= printing_delay.get()) {
			BlockIterator.register(printing_range.get() + 1, printing_range.get() + 1, (pos, blockState) -> {
				BlockState required = worldSchematic.getBlockState(pos);

				if (
						mc.player.getBlockPos().isWithinDistance(pos, printing_range.get())
						&& blockState.isReplaceable()
						&& !required.isLiquid()
						&& !required.isAir()
						&& blockState.getBlock() != required.getBlock()
						&& DataManager.getRenderLayerRange().isPositionWithinRange(pos)
						&& !mc.player.getBoundingBox().intersects(Vec3d.of(pos), Vec3d.of(pos).add(1, 1, 1))
						&& required.canPlaceAt(mc.world, pos)
					) {
					boolean isBlockInLineOfSight = BuildUtils.isBlockInLineOfSight(pos, required);
			    	SlabType wantedSlabType = advanced.get() && required.contains(Properties.SLAB_TYPE) ? required.get(Properties.SLAB_TYPE) : null;
			    	BlockHalf wantedBlockHalf = advanced.get() && required.contains(Properties.BLOCK_HALF) ? required.get(Properties.BLOCK_HALF) : null;
			    	Direction wantedHorizontalOrientation = advanced.get() && required.contains(Properties.HORIZONTAL_FACING) ? required.get(Properties.HORIZONTAL_FACING) : null;
			    	Axis wantedAxies = advanced.get() && required.contains(Properties.AXIS) ? required.get(Properties.AXIS) : null;
			    	Direction wantedHopperOrientation = advanced.get() && required.contains(Properties.HOPPER_FACING) ? required.get(Properties.HOPPER_FACING) : null;

					if(
						airPlace.get()
						&& placeThroughWall.get()
						|| !airPlace.get()
						&& !placeThroughWall.get()
						&&  isBlockInLineOfSight
						&& BuildUtils.getVisiblePlaceSide(
							pos,
							required,
							wantedSlabType, 
							wantedBlockHalf,
							wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
							wantedAxies,
							printing_range.get(),
							advanced.get() ? dir(required) : null
						) != null
						|| airPlace.get()
						&& !placeThroughWall.get()
						&& isBlockInLineOfSight
						|| !airPlace.get()
						&& placeThroughWall.get()
						&& BlockUtils.getPlaceSide(pos) != null
					) {
						if (!whitelistenabled.get() || whitelist.get().contains(required.getBlock())) {
							toSort.add(new BlockPos(pos));
						}
					}
				}
			});

			BlockIterator.after(() -> {
				//if (!tosort.isEmpty()) info(tosort.toString());

				if (firstAlgorithm.get() != SortAlgorithm.None) {
					if (firstAlgorithm.get().applySecondSorting) {
						if (secondAlgorithm.get() != SortingSecond.None) {
							toSort.sort(secondAlgorithm.get().algorithm);
						}
					}
					toSort.sort(firstAlgorithm.get().algorithm);
				}


				int placed = 0;
				for (BlockPos pos : toSort) {

					BlockState state = worldSchematic.getBlockState(pos);
					Item item = state.getBlock().asItem();

					if (dirtgrass.get() && item == Items.GRASS_BLOCK)
						item = Items.DIRT;
					if (switchItem(item, state, () -> place(state, pos))) {
						timer = 0;
						placed++;
						if (renderBlocks.get()) {
							placed_fade.add(new Pair<>(fadeTime.get(), new BlockPos(pos)));
						}
						if (placed >= bpt.get()) {
							break;
						}
					}
				}
				
				if (placed >= bpt.get()) return;
				heat = 0;
				
			});


		} else timer++;
	}
	
	public boolean placeholder(){
		return true;
	}
	
	private void set(KeyBinding bind, boolean pressed) {
        boolean wasPressed = bind.isPressed();
        bind.setPressed(pressed);

        InputUtil.Key key = ((KeyBindingAccessor) bind).meteor$getKey();
        if (wasPressed != pressed && key.getCategory() == InputUtil.Type.KEYSYM) {
            MeteorClient.EVENT_BUS.post(
			KeyEvent.get(
				new KeyInput(
					key.getCode(), // key
					0,             // scancode
					0              // modifiers
				),
				pressed ? KeyAction.Press : KeyAction.Release
			)
		);
        }
    }
	
	private void unpress() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }
	
	public boolean place(BlockState required, BlockPos pos) {

		if (mc.player == null || mc.world == null) return false;
		if (!mc.world.getBlockState(pos).isReplaceable()) return false;

		Direction wantedSide = advanced.get() ? dir(required) : null;
    	SlabType wantedSlabType = advanced.get() && required.contains(Properties.SLAB_TYPE) ? required.get(Properties.SLAB_TYPE) : null;
    	BlockHalf wantedBlockHalf = advanced.get() && required.contains(Properties.BLOCK_HALF) ? required.get(Properties.BLOCK_HALF) : null;
    	Direction wantedHorizontalOrientation = advanced.get() && required.contains(Properties.HORIZONTAL_FACING) ? required.get(Properties.HORIZONTAL_FACING) : null;
    	Axis wantedAxies = advanced.get() && required.contains(Properties.AXIS) ? required.get(Properties.AXIS) : null;
    	Direction wantedHopperOrientation = advanced.get() && required.contains(Properties.HOPPER_FACING) ? required.get(Properties.HOPPER_FACING) : null;
    	//Direction wantedFace = advanced.get() && required.contains(Properties.FACING) ? required.get(Properties.FACING) : null;
    	
		if (stopOnPlace.get()){
			heat++;
			
			if (heatMode.get()){
				
				if (heat < heatLimit.get()) return true;
				
				
			}
			
			unpress();
		}
		
    	Direction placeSide = placeThroughWall.get() ?
    						BuildUtils.getPlaceSide(
    								pos,
    								required,
    								wantedSlabType, 
    								wantedBlockHalf,
    								wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
    								wantedAxies,
    								wantedSide)
    						: BuildUtils.getVisiblePlaceSide(
    								pos,
    								required,
    								wantedSlabType, 
    								wantedBlockHalf,
    								wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
    								wantedAxies,
    								printing_range.get(),
    								wantedSide
							);
    	

        return BuildUtils.place(pos, placeSide, wantedSlabType, wantedBlockHalf, wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation, wantedAxies, airPlace.get(), swing.get(), rotate.get(), clientSide.get(), printing_range.get(), priority.get());
	}
	
	/// Дорти 19.01.2024 TO-DO: убрать нестинг
	private int check_item(ItemStack looking_stack, Item item, Supplier<Boolean> action){
		boolean isCreative = mc.player.getAbilities().creativeMode;
		if ( !(!isCreative && looking_stack.getItem() == item && looking_stack.getCount() >= min_blocks_inv.get() ||
			isCreative && looking_stack.getItem() == item )) { return -1;}
		
		///InvUtils.swap(usedSlot, returnHand.get());
		if (action.get()){ 
			usedSlot = mc.player.getInventory().getSelectedSlot();
			return 1;
		}
		/// InvUtils.swap(selectedSlot, returnHand.get());
		return 0;

		
	}
	
	/// Добавляем условие на количество предметов в стаке
	public FindItemResult special_find(Item... items) {
        return InvUtils.find(itemStack -> {
            for (Item item : items) {
                if (itemStack.getItem() == item && itemStack.getCount() >= min_blocks_inv.get()) return true;
            }
            return false;
        });
    }
	
	private boolean switchItem(Item item, BlockState state, Supplier<Boolean> action) {
		if (mc.player == null) return false;
		
		int selectedSlot = mc.player.getInventory().getSelectedSlot();
		boolean isCreative = mc.player.getAbilities().creativeMode;
		FindItemResult result = special_find(item); 
		
		
		// TODO: Check if ItemStack nbt has BlockStateTag == BlockState required when in creative
		
		// сначала из основной руки
		int callback = check_item(mc.player.getMainHandStack(), item, action);
		if (callback != -1) return (callback == 0) ? false : true;
		
		// затем из слота из которого уже пользовались??? (Зачем это было задумано автором, ведь использованный слот и есть основная рука...)
		if (usedSlot != -1){
			callback = check_item(mc.player.getInventory().getStack(usedSlot), item, action);
			if (callback != -1) return (callback == 0) ? false : true;
		}
		
		/// Этот ужас я когда-нибудь потом уберу. Что тут делал изначальный автор оставлю на размышление...
		 if (
			result.found() && !isCreative ||
			result.found() && isCreative
		) {
			if (result.isHotbar()) {
				InvUtils.swap(result.slot(), returnHand.get());

				if (action.get()) {
					usedSlot = mc.player.getInventory().getSelectedSlot();
					return true;
				} else {
					InvUtils.swap(selectedSlot, returnHand.get());
					return false;
				}

			} else if (result.isMain()) {
				FindItemResult empty = InvUtils.findEmpty();

				if (empty.found() && empty.isHotbar()) {
					InvUtils.move().from(result.slot()).toHotbar(empty.slot());
					InvUtils.swap(empty.slot(), returnHand.get());

					if (action.get()) {
						usedSlot = mc.player.getInventory().getSelectedSlot();
						return true;
					} else {
						InvUtils.swap(selectedSlot, returnHand.get());
						return false;
					}

				} else if (usedSlot != -1) {
					///InvUtils.move().from(result.slot()).toHotbar(usedSlot); /// подозреваю что именно эта строчка вызывает проблемы ...
					
					/// UPD. Ага. Это реально вызывает проблемы. Если в хотбаре мало предметов (~10), а в инвентаре есть тот же предмет, но его больше
					/// то модуль попробует переместить предметы из инвентаря в хотбар. Теоретически уничтожая инвариант наличия 2 стопок. 
					/// Я поменял на quickSwap, надеюсь это пофиксит проблему
					InvUtils.quickSwap().fromId(usedSlot).to(result.slot());
					InvUtils.swap(usedSlot, returnHand.get());

					if (action.get()) {
						return true;
					} else {
						InvUtils.swap(selectedSlot, returnHand.get());
						return false;
					}

				} else return false;
			} else return false;
		} else if (isCreative) {
			int slot = 0;
            FindItemResult fir = InvUtils.find(ItemStack::isEmpty, 0, 8);
            if (fir.found()) {
                slot = fir.slot();
            }
			mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, item.getDefaultStack()));
			InvUtils.swap(slot, returnHand.get());
            return true;
		} else return false;
	}

	private Direction dir(BlockState state) {
		if (state.contains(Properties.FACING)) return state.get(Properties.FACING);
		else if (state.contains(Properties.AXIS)) return Direction.from(state.get(Properties.AXIS), Direction.AxisDirection.POSITIVE);
		else if (state.contains(Properties.HORIZONTAL_AXIS)) return Direction.from(state.get(Properties.HORIZONTAL_AXIS), Direction.AxisDirection.POSITIVE);
		else return Direction.UP;
	}

	@EventHandler
	private void onRender(Render3DEvent event) {
		placed_fade.forEach(s -> {
			Color a = new Color(colour.get().r, colour.get().g, colour.get().b, (int) (((float)s.getLeft() / (float) fadeTime.get()) * colour.get().a));
			event.renderer.box(s.getRight(), a, null, ShapeMode.Sides, 0);
		});
	}

	@SuppressWarnings("unused")
	public enum SortAlgorithm {
		None(false, (a, b) -> 0),
		TopDown(true, Comparator.comparingInt(value -> value.getY() * -1)),
		DownTop(true, Comparator.comparingInt(Vec3i::getY)),
		Nearest(false, Comparator.comparingDouble(value -> MeteorClient.mc.player != null ? Utils.squaredDistance(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ(), value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5) : 0)),
		Furthest(false, Comparator.comparingDouble(value -> MeteorClient.mc.player != null ? (Utils.squaredDistance(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ(), value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5)) * -1 : 0));


		final boolean applySecondSorting;
		final Comparator<BlockPos> algorithm;

		SortAlgorithm(boolean applySecondSorting, Comparator<BlockPos> algorithm) {
			this.applySecondSorting = applySecondSorting;
			this.algorithm = algorithm;
		}
	}

	@SuppressWarnings("unused")
	public enum SortingSecond {
		None(SortAlgorithm.None.algorithm),
		Nearest(SortAlgorithm.Nearest.algorithm),
		Furthest(SortAlgorithm.Furthest.algorithm);

		final Comparator<BlockPos> algorithm;

		SortingSecond(Comparator<BlockPos> algorithm) {
			this.algorithm = algorithm;
		}
	}
}