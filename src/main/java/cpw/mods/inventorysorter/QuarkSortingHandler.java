package cpw.mods.inventorysorter;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Predicate;

public class QuarkSortingHandler {
	public static final Comparator<ItemStack> FALLBACK_COMPARATOR = jointComparator(Arrays.asList(
			Comparator.comparingInt((ItemStack s) -> Item.getId(s.getItem())),
			QuarkSortingHandler::damageCompare,
			(ItemStack s1, ItemStack s2) -> s2.getCount() - s1.getCount(),
			QuarkSortingHandler::nameCompare,
			QuarkSortingHandler::fallbackNBTCompare,
			(ItemStack s1, ItemStack s2) -> s2.hashCode() - s1.hashCode()));

	private static final Comparator<ItemStack> FOOD_COMPARATOR = jointComparator(Arrays.asList(
			QuarkSortingHandler::foodHealCompare,
			QuarkSortingHandler::foodSaturationCompare));

	private static final Comparator<ItemStack> TOOL_COMPARATOR = jointComparator(Arrays.asList(
			QuarkSortingHandler::toolPowerCompare,
			QuarkSortingHandler::enchantmentCompare,
			QuarkSortingHandler::damageCompare));

	private static final Comparator<ItemStack> SWORD_COMPARATOR = jointComparator(Arrays.asList(
			QuarkSortingHandler::swordPowerCompare,
			QuarkSortingHandler::enchantmentCompare,
			QuarkSortingHandler::damageCompare));

	private static final Comparator<ItemStack> ARMOR_COMPARATOR = jointComparator(Arrays.asList(
			QuarkSortingHandler::armorSlotAndToughnessCompare,
			QuarkSortingHandler::enchantmentCompare,
			QuarkSortingHandler::damageCompare));

	private static final Comparator<ItemStack> BOW_COMPARATOR = jointComparator(Arrays.asList(
			QuarkSortingHandler::enchantmentCompare,
			QuarkSortingHandler::damageCompare));

	private static final Comparator<ItemStack> POTION_COMPARATOR = jointComparator(Arrays.asList(
			QuarkSortingHandler::potionComplexityCompare,
			QuarkSortingHandler::potionTypeCompare));

	private static final Comparator<ItemStack> ENCHANTED_BOOK_COMPARATOR = QuarkSortingHandler::enchantmentCompare;

	public static int stackCompare(ItemStack stack1, ItemStack stack2) {
		ItemType type1 = getType(stack1);
		ItemType type2 = getType(stack2);

		if (type1 == type2)
			return type1.comparator.compare(stack1, stack2);

		return type1.ordinal() - type2.ordinal();
	}

	private static ItemType getType(ItemStack stack) {
		for (ItemType type : ItemType.values())
			if (type.fitsInType(stack))
				return type;

		throw new RuntimeException("Having an ItemStack that doesn't fit in any type is impossible.");
	}

	private static Predicate<ItemStack> classPredicate(Class<? extends Item> clazz) {
		return (ItemStack s) -> !s.isEmpty() && clazz.isInstance(s.getItem());
	}

	private static Predicate<ItemStack> inverseClassPredicate(Class<? extends Item> clazz) {
		return classPredicate(clazz).negate();
	}

	private static Predicate<ItemStack> itemPredicate(List<Item> list) {
		return (ItemStack s) -> !s.isEmpty() && list.contains(s.getItem());
	}

	public static Comparator<ItemStack> jointComparator(Comparator<ItemStack> finalComparator, List<Comparator<ItemStack>> otherComparators) {
		if (otherComparators == null)
			return jointComparator(List.of(finalComparator));

		List<Comparator<ItemStack>> newList = new ArrayList<>(otherComparators);
		newList.add(finalComparator);
		return jointComparator(newList);
	}

	public static Comparator<ItemStack> jointComparator(List<Comparator<ItemStack>> comparators) {
		return jointComparatorFallback((ItemStack s1, ItemStack s2) -> {
			for (Comparator<ItemStack> comparator : comparators) {
				if (comparator == null)
					continue;

				int compare = comparator.compare(s1, s2);
				if (compare == 0)
					continue;

				return compare;
			}

			return 0;
		}, FALLBACK_COMPARATOR);
	}

	private static Comparator<ItemStack> jointComparatorFallback(Comparator<ItemStack> comparator, Comparator<ItemStack> fallback) {
		return (ItemStack s1, ItemStack s2) -> {
			int compare = comparator.compare(s1, s2);
			if (compare == 0)
				return fallback == null ? 0 : fallback.compare(s1, s2);

			return compare;
		};
	}

	private static Comparator<ItemStack> listOrderComparator(List<Item> list) {
		return (ItemStack stack1, ItemStack stack2) -> {
			Item i1 = stack1.getItem();
			Item i2 = stack2.getItem();
			if (list.contains(i1)) {
				if (list.contains(i2))
					return list.indexOf(i1) - list.indexOf(i2);
				return 1;
			}

			if (list.contains(i2))
				return -1;

			return 0;
		};
	}

	private static List<Item> list(Object... items) {
		List<Item> itemList = new ArrayList<>();
		for (Object o : items)
			if (o != null) {
				if (o instanceof Item item)
					itemList.add(item);
				else if (o instanceof Block block)
					itemList.add(block.asItem());
				else if (o instanceof ItemStack stack)
					itemList.add(stack.getItem());
				else if (o instanceof String s) {
					Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
					if (item != null)
						itemList.add(item);
				}
			}

		return itemList;
	}

	private static int nutrition(FoodProperties properties) {
		if (properties == null)
			return 0;
		return properties.getNutrition();
	}

	private static int foodHealCompare(ItemStack stack1, ItemStack stack2) {
		return nutrition(stack2.getItem().getFoodProperties()) - nutrition(stack1.getItem().getFoodProperties());
	}

	private static float saturation(FoodProperties properties) {
		if (properties == null)
			return 0;
		return Math.min(20, properties.getNutrition() * properties.getSaturationModifier() * 2);
	}

	private static int foodSaturationCompare(ItemStack stack1, ItemStack stack2) {
		return (int) (saturation(stack2.getItem().getFoodProperties()) - saturation(stack1.getItem().getFoodProperties()));
	}

	private static int enchantmentCompare(ItemStack stack1, ItemStack stack2) {
		return enchantmentPower(stack2) - enchantmentPower(stack1);
	}

	private static int enchantmentPower(ItemStack stack) {
		if (!stack.isEnchanted())
			return 0;

		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
		int total = 0;

		for (Integer i : enchantments.values())
			total += i;

		return total;
	}

	private static int toolPowerCompare(ItemStack stack1, ItemStack stack2) {
		Tier mat1 = ((DiggerItem) stack1.getItem()).getTier();
		Tier mat2 = ((DiggerItem) stack2.getItem()).getTier();
		return (int) (mat2.getSpeed() * 100 - mat1.getSpeed() * 100);
	}

	private static int swordPowerCompare(ItemStack stack1, ItemStack stack2) {
		Tier mat1 = ((SwordItem) stack1.getItem()).getTier();
		Tier mat2 = ((SwordItem) stack2.getItem()).getTier();
		return (int) (mat2.getAttackDamageBonus() * 100 - mat1.getAttackDamageBonus() * 100);
	}

	private static int armorSlotAndToughnessCompare(ItemStack stack1, ItemStack stack2) {
		ArmorItem armor1 = (ArmorItem) stack1.getItem();
		ArmorItem armor2 = (ArmorItem) stack2.getItem();

		EquipmentSlot slot1 = armor1.getEquipmentSlot();
		EquipmentSlot slot2 = armor2.getEquipmentSlot();

		if (slot1 == slot2)
			return armor2.getMaterial().getDefenseForType(armor2.getType()) - armor2.getMaterial().getDefenseForType(armor1.getType());

		return slot2.getIndex() - slot1.getIndex();
	}

	public static int damageCompare(ItemStack stack1, ItemStack stack2) {
		return stack1.getDamageValue() - stack2.getDamageValue();
	}

	public static int fallbackNBTCompare(ItemStack stack1, ItemStack stack2) {
		boolean hasTag1 = stack1.hasTag();
		boolean hasTag2 = stack2.hasTag();

		if (hasTag2 && !hasTag1)
			return -1;
		if (hasTag1 && !hasTag2)
			return 1;
		if (!hasTag1)
			return 0;

		return stack2.getTag().toString().hashCode() - stack1.getTag().toString().hashCode();
	}

	public static int potionComplexityCompare(ItemStack stack1, ItemStack stack2) {
		List<MobEffectInstance> effects1 = PotionUtils.getCustomEffects(stack1);
		List<MobEffectInstance> effects2 = PotionUtils.getCustomEffects(stack2);

		int totalPower1 = 0;
		int totalPower2 = 0;
		for (MobEffectInstance inst : effects1)
			totalPower1 += inst.getAmplifier() * inst.getDuration();
		for (MobEffectInstance inst : effects2)
			totalPower2 += inst.getAmplifier() * inst.getDuration();

		return totalPower2 - totalPower1;
	}

	public static int potionTypeCompare(ItemStack stack1, ItemStack stack2) {
		Potion potion1 = PotionUtils.getPotion(stack1);
		Potion potion2 = PotionUtils.getPotion(stack2);

		return BuiltInRegistries.POTION.getId(potion2) - BuiltInRegistries.POTION.getId(potion1);
	}

	public static int nameCompare(ItemStack stack1, ItemStack stack2) {
		boolean hasCustomHoverName1 = stack1.hasCustomHoverName();
		boolean hasCustomHoverName2 = stack2.hasCustomHoverName();

		if (hasCustomHoverName2 && !hasCustomHoverName1)
			return 1;
		if (hasCustomHoverName1 && !hasCustomHoverName2)
			return -1;

		String name1 = stack1.getHoverName().getString();
		String name2 = stack1.getHoverName().getString();

		return name1.compareTo(name2);
	}

	private enum ItemType {

		TORCH(list(Blocks.TORCH)),
		FOOD(ItemStack::isEdible, FOOD_COMPARATOR),
		TOOL_PICKAXE(classPredicate(PickaxeItem.class), TOOL_COMPARATOR),
		TOOL_SHOVEL(classPredicate(ShovelItem.class), TOOL_COMPARATOR),
		TOOL_AXE(classPredicate(AxeItem.class), TOOL_COMPARATOR),
		TOOL_SWORD(classPredicate(SwordItem.class), SWORD_COMPARATOR),
		TOOL_GENERIC(classPredicate(DiggerItem.class), TOOL_COMPARATOR),
		ARMOR(classPredicate(ArmorItem.class), ARMOR_COMPARATOR),
		BOW(classPredicate(BowItem.class), BOW_COMPARATOR),
		CROSSBOW(classPredicate(CrossbowItem.class), BOW_COMPARATOR),
		TRIDENT(classPredicate(TridentItem.class), BOW_COMPARATOR),
		ARROWS(classPredicate(ArrowItem.class)),
		TIPPED_ARROW(classPredicate(TippedArrowItem.class), POTION_COMPARATOR),
		POTION(classPredicate(PotionItem.class), POTION_COMPARATOR),
		ENCHANTED_BOOK(classPredicate(EnchantedBookItem.class), ENCHANTED_BOOK_COMPARATOR),
		MINECART(classPredicate(MinecartItem.class)),
		RAIL(list(Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.ACTIVATOR_RAIL)),
		DYE(classPredicate(DyeItem.class)),
		ANY(inverseClassPredicate(BlockItem.class)),
		BLOCK(classPredicate(BlockItem.class));

		private final Predicate<ItemStack> predicate;
		private final Comparator<ItemStack> comparator;

		ItemType(List<Item> list) {
			this(itemPredicate(list), jointComparator(listOrderComparator(list), new ArrayList<>()));
		}

		ItemType(Predicate<ItemStack> predicate) {
			this(predicate, FALLBACK_COMPARATOR);
		}

		ItemType(Predicate<ItemStack> predicate, Comparator<ItemStack> comparator) {
			this.predicate = predicate;
			this.comparator = comparator;
		}

		public boolean fitsInType(ItemStack stack) {
			return predicate.test(stack);
		}

	}

}
