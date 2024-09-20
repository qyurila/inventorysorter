# Inventory Sorter (Configurable)

A Minecraft mod for simple inventory sorting, now in configurable order.
Forked from [Inventory Sorter by cpw](https://github.com/cpw/inventorysorter).

The default sort order is ported from **[Quark](https://quarkmod.net/)'s Inventory Sorting** feature.

Some bugfixes and improvements were also added. More information below.

Supports Forge 1.18.2, 1.19.2 and 1.20.1 for now.

## Cautions

This fork is meant to be a **drop-in replacement** for the original mod.
**Delete** the original mod first to prevent accidental conflicts!

Also, I don't test this mod on multiplayer servers so there might be some issues that don't present in the original.
Please report any issues if you find any.


## Links

- [GitHub](https://github.com/qyurila/inventorysorter)
- [Modrinth](https://modrinth.com/project/inventory-sorter-configurable)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/inventory-sorter-configurable)


## Features

- **Inventory Sorting**, with the middle click or custom hotkey
- ~~Scroll wheel~~ (removed, sorry)


### Changes and Bug Fixes

- The 1.18.2 version now works with the mods that have increased slot stack size
  - notably **Sophisticated Backpacks/Storages**
  - 1.19.2 and 1.20.1 versions already worked with it
- Made a failsafe to **prevent items from being deleted** in some cases
  - notably on Curios slots
- Some redundant features/codes that don't work properly or cause issues in some cases were removed
  - notably Scroll Wheel

> TODO: Make PRs for the original repo, after enough testing


### Configure the Sort Order

You can finally customize the sort order, while maintaining the compatibility of the original Inventory Sorter!

The default sort order is (mostly) **the same as [Quark](https://quarkmod.net/)'s Inventory Sorting** feature - because I love Quark.
The code is directly ported and adopted from the [original code](https://github.com/VazkiiMods/Quark/blob/master/src/main/java/org/violetmoon/quark/base/handler/SortingHandler.java).

The current available options for the sort order are:

- Quark
  - Identical to the original, except that torch comes before foods: Check the differences on GitHub Repo.
- Creative Tab
  - Could be not 100% accurate
- Raw Item ID (Registered Order)
  - Same as the default order in JEI etc. (on ~1.19.2)
- Item (Original) Name
- Item Display Name

You can also set it to order items **by count first**, just like the original mod,
and then by one of the above options (when the count is the same).

Just choose an option in the config file. No relaunch required.


## Details

- The core functionality is not changed from the original, which means:

> The mod requires installation on both client and server to function.
> It makes no attempt to sort in the client, rather delegating all work to the server.
> This means it is very reliable and very fast. It should work with all containers (and maybe too many),
> and tries to respect canExtract/isItemValid (it will avoid slots that are marked that way).


## Thanks to

- [cpw](https://github.com/cpw), the author of the original [Inventory Sorter](https://github.com/cpw/inventorysorter)
- [Vazkii](https://vazkii.net/), the author of [Quark](https://quarkmod.net/)


## License

This project is licensed under GPL v3, except for the code in [`QuarkSortingHandler.java`](https://github.com/qyurila/inventorysorter/blob/1.18.2/src/main/java/cpw/mods/inventorysorter/QuarkSortingHandler.java),
which is licensed under the CC BY-NC-SA 3.0.
Please refer to [LICENSE.md](https://github.com/qyurila/inventorysorter/blob/1.18.2/LICENSE.md) for more details.
