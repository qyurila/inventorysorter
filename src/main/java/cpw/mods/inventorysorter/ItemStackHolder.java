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

import net.minecraft.world.item.ItemStack;

/**
 * Created by cpw on 08/01/16.
 */
public class ItemStackHolder
{
    public final ItemStack itemStack;

    ItemStackHolder(ItemStack stack)
    {
        this.itemStack = stack;
    }

    @Override
    public int hashCode()
    {
        return itemStack.getItem().hashCode() * 31 + (itemStack.hasTag() ? itemStack.getTag().hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ItemStackHolder)) return false;
        ItemStackHolder holder = (ItemStackHolder)obj;
        return itemStack.getItem() == holder.itemStack.getItem() && ItemStack.isSameItemSameTags(itemStack, holder.itemStack);
    }
}
