package obscurum.environment.foreground;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;
import obscurum.display.Display;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Tile;

@Getter
public class ForegroundTile extends Tile {
    protected int currentHealth;
    protected int baseHealth;
    protected int maxHealth;
    protected int armour;
    @Setter protected boolean invulnerable;
    @Setter protected boolean opaque;

    public ForegroundTile(String name, char glyph, Color foregroundColour, int baseHealth, int armour, boolean invulnerable, boolean opaque) {
        super(name, glyph, foregroundColour);

        if (baseHealth < 1) {
            throw new IllegalArgumentException(String.format("Base health %d must be at least 1.", baseHealth));
        }

        this.currentHealth = baseHealth;
        this.baseHealth = baseHealth;
        this.maxHealth = baseHealth;
        setArmour(armour);
        this.invulnerable = invulnerable;
        this.opaque = opaque;
    }

    public static ForegroundTile createStone() {
        return new ForegroundTile("Stone", (char) 177, AsciiPanel.brightBlack, 1, 0, true, true);
    }

    public static ForegroundTile createWall() {
        return new ForegroundTile("Wall", (char)177, AsciiPanel.yellow, 1, 0, true, true);
    }

    public static ForegroundTile createEmptyTile() {
        return new ForegroundTile("Empty Tile", Display.SPACE, Display.BLACK, 1, 0, true, false);
    }

    public void setCurrentHealth(int currentHealth) {
        if (currentHealth < 0) {
            throw new IllegalArgumentException(String.format("Health %d must be non-negative.", currentHealth));
        }
        this.currentHealth = currentHealth;
    }

    public void setArmour(int armour) {
        if (armour < 0) {
            throw new IllegalArgumentException(String.format("Armour %d must be non-negative.", armour));
        }
        this.armour = armour;
    }

    public void dealDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException(String.format("The amount of damage dealt (%d) must be non-negative.", damage));
        }

        if (!invulnerable) {
            currentHealth = Math.max(0, currentHealth - damage);
        }
    }
}
