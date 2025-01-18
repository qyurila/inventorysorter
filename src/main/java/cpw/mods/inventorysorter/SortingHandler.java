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

import com.google.common.collect.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.*;

import java.util.Iterator;
import java.util.function.*;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

/**
 * @author cpw
 */
public enum SortingHandler implements Consumer<ContainerContext>
{
    INSTANCE;
    @Override
    public void accept(ContainerContext context)
    {
        if (context == null) throw new NullPointerException("WHUT");
        // Ignore if we can't find ourselves in the slot set
        if (context.slotMapping == null) return;
        final Multiset<ItemStackHolder> itemcounts = InventoryHandler.INSTANCE.getInventoryContent(context);

        if (!(context.slot.container instanceof CraftingContainer) && !context.slotMapping.markAsHeterogeneous)
        {
            compactInventory(context, itemcounts);
        }
    }

    private void compactInventory(final ContainerContext context, final Multiset<ItemStackHolder> itemcounts)
    {
        final ResourceLocation containerTypeName = lookupContainerTypeName(context.slotMapping.container);

        InventorySorter.INSTANCE.lastContainerType = containerTypeName;
        if (InventorySorter.INSTANCE.containerblacklist.contains(containerTypeName)) {
            InventorySorter.INSTANCE.debugLog("Container {} blacklisted", ()-> new String[] {containerTypeName.toString()});
            return;
        }

        InventorySorter.INSTANCE.debugLog("Container \"{}\" being sorted", ()->new String[] {containerTypeName.toString()});
        final Iterator<Multiset.Entry<ItemStackHolder>> itemsIterator;
        try
        {
            if (Config.ClientConfig.CONFIG.sortByCountFirst.get()) {
                itemsIterator = Multisets.copyHighestCountFirst(itemcounts).entrySet().iterator();
            } else {
                itemsIterator = itemcounts.entrySet().iterator();
            }
        }
        catch (Exception e)
        {
            InventorySorter.LOGGER.warn("Weird, the sorting didn't quite work!", e);
            return;
        }
        int slotLow = context.slotMapping.begin;
        int slotHigh = context.slotMapping.end + 1;

        Multiset.Entry<ItemStackHolder> stackHolder = itemsIterator.hasNext() ? itemsIterator.next() : null;
        int itemCount = stackHolder != null ? stackHolder.getCount() : 0;

        ItemStack[] slotBuffer = new ItemStack[slotHigh];
        for (int i = slotLow; i < slotHigh; i++)
        {
            slotBuffer[i] = ItemStack.EMPTY;

            final Slot slot = context.player.containerMenu.getSlot(i);
            if (!slot.mayPickup(context.player) && slot.hasItem()) {
                InventorySorter.LOGGER.log(Level.DEBUG, "Slot {} of container {} disallows canTakeStack", () -> slot.index, () -> containerTypeName);
                slotBuffer[i] = slot.getItem();
                continue;
            }

            ItemStack target = ItemStack.EMPTY;
            if (itemCount > 0 && stackHolder != null)
            {
                target = stackHolder.getElement().itemStack.copy();
                target.setCount(Math.min(itemCount, slot.getMaxStackSize(target)));
            }

            if (!target.isEmpty()) {
                if (!slot.mayPlace(target)) { // The item isn't valid for this slot
                    final ItemStack trg = target;
                    InventorySorter.LOGGER.log(Level.DEBUG, "Item {} is not valid in slot {} of container {}", () -> trg, () -> slot.index, () -> containerTypeName);
                    continue;
                }

                slotBuffer[i] = target;

                itemCount -= target.getCount();
                if (itemCount == 0) {
                    stackHolder = itemsIterator.hasNext() ? itemsIterator.next() : null;
                    itemCount = stackHolder != null ? stackHolder.getCount() : 0;
                }
            }
        }
        if (stackHolder != null)
        {
            InventorySorter.LOGGER.log(Level.INFO, "Some items were about to be deleted, sorting canceled");
            return;
        }
        for (int i = slotLow; i < slotHigh; i++)
        {
            Slot slot = context.player.containerMenu.getSlot(i);
            slot.set(slotBuffer[i]);
        }
    }

    private static final ResourceLocation DUMMY_PLAYER_CONTAINER = new ResourceLocation("inventorysorter:dummyplayercontainer");

    private ResourceLocation lookupContainerTypeName(AbstractContainerMenu container) {
        return container instanceof InventoryMenu ? DUMMY_PLAYER_CONTAINER : ForgeRegistries.MENU_TYPES.getKey(container.getType());
    }
}
