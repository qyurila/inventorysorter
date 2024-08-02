/*
 *     Copyright © 2016 cpw
 *     This file is part of Inventorysorter.
 *
 *     Inventorysorter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Inventorysorter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Inventorysorter.  If not, see <http://www.gnu.org/licenses/>.
 */

package cpw.mods.inventorysorter;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.primitives.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.*;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

/**
 * @author cpw
 */
public enum InventoryHandler
{
    INSTANCE;

    public Multiset<ItemStackHolder> getInventoryContent(ContainerContext context)
    {
        int slotLow = context.slotMapping.begin;
        int slotHigh = context.slotMapping.end + 1;
        Multiset<ItemStackHolder> itemcounts = HashMultiset.create();
        for (int i = slotLow; i < slotHigh; i++)
        {
            final Slot slot = context.player.containerMenu.getSlot(i);
            if (!slot.mayPickup(context.player)) continue;
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty())
            {
                ItemStackHolder holder = new ItemStackHolder(stack.copy());
                itemcounts.add(holder, stack.getCount());
            }
        }
        final SortedMultiset<ItemStackHolder> entries = TreeMultiset.create(new ItemStackComparator());
        for (Multiset.Entry<ItemStackHolder> entry : itemcounts.entrySet())
        {
            entries.add(entry.getElement(), entry.getCount());
        }
        return entries;
    }

    public static class ItemStackComparator implements Comparator<ItemStackHolder>
    {
        @Override
        public int compare(ItemStackHolder holder1, ItemStackHolder holder2)
        {
            ItemStack stack1 = holder1.itemStack;
            ItemStack stack2 = holder2.itemStack;

            if (stack1.isEmpty()) return -1;
            if (stack2.isEmpty()) return 1;
            if (holder1 == holder2) return 0;

            int compareResult = 0;
            switch (Config.CLIENT.sortOrder.get()) {
                case QUARK:
                    compareResult = QuarkSortingHandler.stackCompare(stack1, stack2);
                    break;
                case CREATIVE:
                    CreativeModeTab category1 = stack1.getItem().getItemCategory();
                    CreativeModeTab category2 = stack2.getItem().getItemCategory();

                    if (category1 == null && category2 == null) break;
                    if (category1 == null) return -1;
                    if (category2 == null) return 1;

                    compareResult = Ints.compare(category1.getId(), category2.getId());
                    if (compareResult == 0) {
                        // TODO find out item ordering inside a creative tab
                        compareResult = Ints.compare(Item.getId(stack1.getItem()), Item.getId(stack2.getItem()));
                    }
                    break;
                case ITEM_ID:
                    compareResult = Ints.compare(Item.getId(stack1.getItem()), Item.getId(stack2.getItem()));
                    break;
                case ITEM_NAME:
                    compareResult = stack1.getItem().getName(stack1).getString().compareTo(stack2.getItem().getName(stack2).getString());
                    break;
                case DISPLAY_NAME:
                    compareResult = stack1.getHoverName().getString().compareTo(stack2.getHoverName().getString());
                    break;
                default:
                    return 0;
            }

            return compareResult != 0 ? compareResult : QuarkSortingHandler.FALLBACK_COMPARATOR.compare(stack1, stack2);
        }
    }

    public static class InventoryMapping
    {
        int begin = Integer.MAX_VALUE;
        int end = 0;
        final Container inv;
        final Container proxy;
        final AbstractContainerMenu container;
        final Class<? extends Slot> slotType;
        boolean markForRemoval;
        boolean markAsHeterogeneous;

        InventoryMapping(Container inv, AbstractContainerMenu container, Container proxy, Class<? extends Slot> slotType)
        {
            this.inv = inv;
            this.container = container;
            this.proxy = proxy;
            this.slotType = slotType;
        }
        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this).add("i", inv).add("c", container).add("b",begin).add("e",end).toString();
        }

        void addSlot(final Slot sl) {
            if (this.slotType != sl.getClass() && !(this.inv instanceof Inventory) && !(this.inv instanceof FurnaceBlockEntity) && !(this.inv instanceof BrewingStandBlockEntity)) {
                this.markForRemoval = true;
            }
            if (this.slotType != sl.getClass())
            {
                this.markAsHeterogeneous = true;
            }
            this.begin = Math.min(sl.index, this.begin);
            this.end = Math.max(sl.index, this.end);

        }
    }
}
