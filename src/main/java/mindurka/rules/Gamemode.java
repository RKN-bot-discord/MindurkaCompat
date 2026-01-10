package mindurka.rules;

import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import mindurka.ui.RulesWrite;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.maps.Map;

public abstract class Gamemode {
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract class Impl {
        protected final MRules customRules;
        protected final Rules rules;
        protected final Map map;

        public final Gamemode factory() { return Gamemode.this; }

        /**
         * A unique name for this gamemode.
         * <p>
         * This is an internal name, for localized name use {@link arc.Core#bundle}.
         * <p>
         * Internally this calls {@link Gamemode#name}
         */
        public final String name() { return factory().name(); }

        /**
         * Set default rules.
         * <p>
         * This function <i>should</i> be called after setting this gamemode, but
         * it's not absolutely necessary. Generally, this being called <b>must</b>
         * be the default behavior, <i>maybe</i> with an optional opt-out.
         */
        public final void setRules() {
            // Reset potentially modified rules to default.

            for (Team team : Team.all) {
                Rules.TeamRule t = rules.teams.get(team);
                t.cheat = false;
            }
            rules.modeName = "";
            rules.onlyDepositCore = false;
            rules.coreIncinerates = false;
            rules.attackMode = false;
            rules.possessionAllowed = true;
            rules.enemyCoreBuildRadius = 30f;
            rules.schematicsAllowed = true;
            rules.loadout.clear();
            rules.bannedBlocks.clear();
            rules.hideBannedBlocks = false;
            rules.infiniteResources = false;

            // Content patches apparently must be set separately.

            _setRules();
        }

        /**
         * Remove this gamemode's tags.
         * <p>
         * Should be called before swapping this instance with another.
         */
        abstract void remove();
        /**
         * Obtain the default content patch.
         * <p>
         * Should be called before swapping this instance with another.
         */
        public @Nullable String builtInContentPatch() { return null; }
        /**
         * Set default custom rules for this gamemode.
         * <p>
         * This function is never called directly, instead so via {@link Impl#setRules}.
         */
        protected abstract void _setRules();
        /**
         * Add gamemode rules to a settings dialog.
         */
        public void writeGamemodeRules(RulesWrite write) {}
    }

     public static Gamemode UNKNOWN = new Gamemode() {
         @Override
         public String name() {
             return "unknown";
         }

         @Override
         Impl create(MRules customRules, Rules rules, Map map) {
             return new Impl(customRules, rules, map) {
                 @Override void remove() {}
                 @Override
                 protected void _setRules() {}
             };
         }
     };

     /**
      * A unique name for this gamemode.
      * <p>
      * This is an internal name, for localized name use {@link arc.Core#bundle}.
      */
     public abstract String name();
     /**
      * Create an instance of this gamemode.
      * <p>
      * This method is used by {@link MRules} and is not intended to be called
      * directly.
      */
     abstract Impl create(MRules customRules, Rules rules, Map map);

     private static final OrderedMap<String, Gamemode> factories = new OrderedMap<>();

     public static void addGamemode(Gamemode factory) {
         factories.put(factory.name(), factory);
     }

     private static boolean initialized = false;
     public static void init() {
         if (initialized) throw new IllegalStateException("Already initialized");
         initialized = true;

         addGamemode(Gamemodes.forts);
     }
     public static @Nullable Gamemode forName(String name) {
         return factories.get(name);
     }

     public static Seq<String> keys() {
         return factories.orderedKeys();
     }
}

// @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
// public abstract class Gamemode {
//     public abstract static class Factory {
//         public abstract String name();
//
//         abstract Gamemode create(MRules customRules, Rules rules, Map map);
//     }
//
//     public static Factory UNKNOWN = new Factory() {
//         @Override
//         public String name() {
//             return "unknown";
//         }
//
//         @Override
//         Gamemode create(MRules customRules, Rules rules, Map map) {
//             return new Gamemode(customRules, rules, map) {
//                 @Override void remove() {}
//                 @Override
//                 protected void _setRules() {}
//             };
//         }
//     };
//
//     protected final MRules customRules;
//     protected final Rules rules;
//     protected final Map map;
//
//     abstract void remove();
//
//     public final void setRules() {
//         // Reset potentially modified rules to default.
//
//         for (Team team : Team.all) {
//             Rules.TeamRule t = rules.teams.get(team);
//             t.cheat = false;
//         }
//         rules.modeName = "";
//         rules.onlyDepositCore = false;
//         rules.coreIncinerates = false;
//         rules.attackMode = false;
//         rules.possessionAllowed = true;
//         rules.enemyCoreBuildRadius = 30f;
//         rules.schematicsAllowed = true;
//         rules.loadout.clear();
//         rules.bannedBlocks.clear();
//         rules.hideBannedBlocks = false;
//         rules.infiniteResources = false;
//
//         // Content patches apparently must be set separately.
//
//         _setRules();
//     }
//     protected abstract void _setRules();
//
//     public @Nullable String builtInContentPatch() { return null; }
//
//     private static final OrderedMap<String, Factory> factories = new OrderedMap<>();
//
//     public static void addGamemode(Factory factory) {
//         factories.put(factory.name(), factory);
//     }
//
//     private static boolean initialized = false;
//     public static void init() {
//         if (initialized) throw new IllegalStateException("Already initialized");
//         initialized = true;
//
//         addGamemode(new Forts.Factory());
//     }
//     public static @Nullable Factory forName(String name) {
//         return factories.get(name);
//     }
//
//     public static Seq<String> keys() {
//         return factories.orderedKeys();
//     }
// }
