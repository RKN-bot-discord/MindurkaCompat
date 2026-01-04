package mindurka;

import arc.struct.IntMap;
import arc.util.Nullable;
import mindustry.game.Rules;
import mindustry.game.Team;

public class FortsTeamRules {
    public static FortsTeamRules Default = new FortsTeamRules(Team.derelict);

    public final Team team;

    private FortsTeamRules(Team team) {
        this.team = team;
    }

    private static IntMap<FortsTeamRules> _teamRulesMap = new IntMap<>();
    public static FortsTeamRules of(Team team) {
        return _teamRulesMap.get(team.id, () -> new FortsTeamRules(team));
    }

    public static FortsTeamRules loadOrDefault(@Nullable Team team, Rules rules, MRules customRules) {
        if (team == null) return Default;
        if (!rules.tags.containsKey("mdrk.forts.team." + team.id + ".platform")) return Default;

        FortsTeamRules teamRules = new FortsTeamRules(team);
        teamRules.load(rules);
        return teamRules;
    }

    /**
     * Default core, from bottom to top, then from left to right.
     */
    public int defaultCore = 0;

    /**
     * Platform area.
     * <p>
     * TODO: Not use this for freeform plots.
     */
    public String platformAreaString = "1,0,0,0,";
    /**
     * Whether a player can play as this team.
     */
    public boolean playable = true;

    public void load(Rules rules) {
        if (this == Default)
            throw new RuntimeException("Attempting to load() a default team rules instance");

        String prefix = "mdrk.forts.team." + team.id;
        // TODO: Separate some rules from Forts.
        String rootPrefix = "mdrk.team." + team.id;

        defaultCore = rules.tags.getInt(prefix + ".defaultCoreIdx", Default.defaultCore);
        platformAreaString = rules.tags.get(prefix + ".platform", Default.platformAreaString);
        playable = !rules.tags.containsKey(rootPrefix + ".playable") || rules.tags.getBool(rootPrefix + ".playable");
    }

    public void save(Rules rules) {
        if (this == Default)
            throw new RuntimeException("Attempting to save() a default team rules instance");

        remove(rules);

        String prefix = "mdrk.forts.team." + team.id;
        String rootPrefix = "mdrk.team." + team.id;

        if (defaultCore != 0) rules.tags.put(prefix + ".defaultCoreIdx", Integer.toString(defaultCore));
        rules.tags.put(prefix + ".platform", platformAreaString);
        rules.tags.put(rootPrefix + ".playable", Boolean.toString(playable));
    }

    public void remove(Rules rules) {
        if (this == Default)
            throw new RuntimeException("Attempting to remove() a default team rules instance");

        String prefix = "mdrk.forts.team." + team.id;
        String rootPrefix = "mdrk.team." + team.id;

        rules.tags.remove(prefix + ".defaultCoreIdx");
        rules.tags.remove(prefix + ".platform");
        rules.tags.remove(rootPrefix + ".playable");
    }
}
