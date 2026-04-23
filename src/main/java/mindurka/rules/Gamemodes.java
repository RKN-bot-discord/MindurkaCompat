package mindurka.rules;

public class Gamemodes {
    private Gamemodes() {}

    public static final Gamemode attack = new BlankGamemode("attack") {{ vanillaGamemode = true; }};
    public static final Gamemode forts = new Forts();
    public static final Gamemode hexed = new BlankGamemode("hexed-pvp") {{ visible = false; }};
    public static final Gamemode hub = new Hub();
    public static final Gamemode pvp = new BlankGamemode("pvp") {{ vanillaGamemode = true; }};
    public static final Gamemode spvp = new BlankGamemode("sandbox-pvp") {{ vanillaGamemode = true; }};
    public static final Gamemode survival = new BlankGamemode("survival") {{ vanillaGamemode = true; }};
}
