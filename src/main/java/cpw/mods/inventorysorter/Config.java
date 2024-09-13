package cpw.mods.inventorysorter;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static class ServerConfig {
        static final ServerConfig CONFIG;
        static final ForgeConfigSpec SPEC;

        static {
            final Pair<ServerConfig, ForgeConfigSpec> conf = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
            CONFIG = conf.getLeft();
            SPEC = conf.getRight();
        }

        final ForgeConfigSpec.ConfigValue<List<? extends String>> containerBlacklist;
        final ForgeConfigSpec.ConfigValue<List<? extends String>> slotBlacklist;

        private ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Inventory sorter blacklists");
            builder.push("blacklists");
            containerBlacklist = builder
                    .comment("Container blacklist")
                    .translation("inventorysorter.config.containerblacklist")
                    .defineList("containerBlacklist", ArrayList::new, t -> true);
            slotBlacklist = builder
                    .comment("Slot type blacklist")
                    .translation("inventorysorter.config.slotblacklist")
                    .defineList("slotBlacklist", new ArrayList<>(), t -> true);
            builder.pop();
        }
    }
    public static class ClientConfig {
        public enum SortOrder
        {
            QUARK,
            CREATIVE,
            ITEM_ID,
            ITEM_NAME,
            DISPLAY_NAME,
        }

        static final ClientConfig CONFIG;
        static final ForgeConfigSpec SPEC;

        static {
            final Pair<ClientConfig, ForgeConfigSpec> conf = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
            CONFIG = conf.getLeft();
            SPEC = conf.getRight();
        }

        final ForgeConfigSpec.EnumValue<SortOrder> sortOrder;
        final ForgeConfigSpec.BooleanValue sortByCountFirst;

        private ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Inventory sorter");
            builder.push("sortingRules");
            sortOrder = builder
                    .comment("Sort order")
                    .translation("inventorysorter.config.sortorder")
                    .defineEnum("sortOrder", SortOrder.QUARK);
            sortByCountFirst = builder
                    .comment("Sort by count first")
                    .translation("inventorysorter.config.sortbycountfirst")
                    .define("sortByCountFirst", false);
            builder.pop();
        }
    }
}
