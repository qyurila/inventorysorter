package cpw.mods.inventorysorter;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static class Server
    {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> containerBlacklist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> slotBlacklist;

        private Server(ForgeConfigSpec.Builder builder) {
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

    public static class Client {
        public enum SortOrder
        {
            RAW_ID,
            CREATIVE,
            QUARK
        }

        public final ForgeConfigSpec.EnumValue<SortOrder> sortOrder;
        public final ForgeConfigSpec.BooleanValue sortByCountFirst;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Inventory sorter");
            builder.push("sortingRules");
            sortOrder = builder
                  .comment("Sort order")
                  .translation("inventorysorter.config.sortorder")
                  .defineEnum("sortOrder", SortOrder.RAW_ID);
            sortByCountFirst = builder
                  .comment("Sort by count first")
                  .translation("inventorysorter.config.sortbycountfirst")
                  .define("sortByCountFirst", false);
        }
    }

    static final Server SERVER;
    static final ForgeConfigSpec SERVER_SPEC;

    static final Client CLIENT;
    static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverSpecPair.getLeft();
        SERVER_SPEC = serverSpecPair.getRight();

        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }
}
