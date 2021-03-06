package obscurum.items;

import java.awt.Color;
import java.lang.Math;
import java.util.ArrayList;
import obscurum.display.Display;
import obscurum.display.DisplayCharacter;
import obscurum.display.DisplayColour;
import obscurum.display.DisplayTile;
import obscurum.screens.InventoryScreen;

/**
 * This models a depiction generator, which contains method to generate several
 * types of depictions for various items or item components.
 * @author Alex Ghita
 */
public class DepictionGenerator {
    public static final int MAX_WIDTH = InventoryScreen.DISPLAY_WIDTH - 2;
    public static final int MAX_HEIGHT = InventoryScreen.DISPLAY_HEIGHT - 2;
    public static final Color[] COLOURS = {Display.RED, Display.GREEN,
            Display.YELLOW, Display.BLUE, Display.MAGENTA, Display.CYAN,
            Display.WHITE, Display.BRIGHT_BLACK, Display.BRIGHT_RED,
            Display.BRIGHT_GREEN, Display.BRIGHT_YELLOW, Display.BRIGHT_BLUE,
            Display.BRIGHT_MAGENTA, Display.BRIGHT_CYAN, Display.BRIGHT_WHITE};
    private Equipment item;
    private final DisplayCharacter[] hiltSideGlyphs = {DisplayCharacter.of('('), DisplayCharacter.of(')'), DisplayCharacter.of('|'), DisplayCharacter.of('\\'), DisplayCharacter.of('/'), DisplayCharacter.of('['), DisplayCharacter.of(']')};
    private final DisplayCharacter[] hiltBaseGlyphs = {DisplayCharacter.of('-'), DisplayCharacter.of('_'), DisplayCharacter.of('='), DisplayCharacter.of('~')};
    private final DisplayCharacter[] hiltFillGlyphs = {DisplayCharacter.of('.'), DisplayCharacter.of('#'), DisplayCharacter.of(',')};
    private final DisplayCharacter[] bladeBaseGlyphs = {DisplayCharacter.of('='), DisplayCharacter.of('-'), DisplayCharacter.of('~')};
    private final DisplayCharacter[] bladeFillGlyphs = {DisplayCharacter.of('.'), DisplayCharacter.of('#'), DisplayCharacter.of(','), DisplayCharacter.of('|')};
    private final boolean[][] hiltSideGlyphConstraints = {
            {true, true, false, false, false, false, false},
            {true, true, false, false, false, false, false},
            {false, false, true, false, false, false, false},
            {false, false, false, true, true, false, false},
            {false, false, false, true, true, false, false},
            {false, false, false, false, false, true, true},
            {false, false, false, false, false, true, true}
    };

    public DepictionGenerator(Equipment item) {
        this.item = item;
    }

    /**
     * Draws a rectangle of the given dimensions, using the given glyphs for each
     * side, and filling the rectangle with the given fill glyph.
     * @param height
     * @param width
     * @param leftGlyph
     * @param rightGlyph
     * @param topGlyph
     * @param bottomGlyph
     * @param fillGlyph
     * @param cornerType 0 = random corners, 1 = vertical glyphs used as corners,
     *                   2 = horizontal glyphs used as corners
     * @return
     */
    private DisplayTile[][] drawRectangle(int height, int width,
                                          DisplayTile leftGlyph, DisplayTile rightGlyph, DisplayTile topGlyph,
                                          DisplayTile bottomGlyph, DisplayTile fillGlyph, int cornerType) {
        DisplayTile[][] rectangle = new DisplayTile[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                DisplayTile verticalGlyph = j == 0 ? leftGlyph : j == width - 1 ?
                        rightGlyph : fillGlyph;

                if (i == 0 || i == height - 1) {
                    DisplayTile horizontalGlyph = i == 0 ? bottomGlyph : topGlyph;
                    double pickChance = cornerType == 0 ? Math.random() : 2 - cornerType;

                    // Pick a corner depending on the coordinates and the corner type.
                    rectangle[i][j] = j != 0 && j != width - 1 || pickChance < 0.5 ?
                            horizontalGlyph : verticalGlyph;
                } else {
                    rectangle[i][j] = verticalGlyph;
                }
            }
        }

        return rectangle;
    }

    /**
     * Vertically concatenates the two given depiction. The first depiction will
     * be on top, and the narrower depiction will be centred horizontally with
     * the other one.
     * @param topDepiction
     * @param bottomDepiction
     * @return
     */
    private DisplayTile[][] concatenateDepictions(DisplayTile[][] topDepiction,
                                                  DisplayTile[][] bottomDepiction) {
        int resultHeight = topDepiction.length + bottomDepiction.length;
        int resultWidth = Math.max(topDepiction[0].length,
                bottomDepiction[0].length);
        DisplayTile[][] result = new DisplayTile[resultHeight][resultWidth];

        // Get the start and end width for each depiction, so they are centred.
        int topStartWidth = (resultWidth - topDepiction[0].length) / 2;
        int topEndWidth = topStartWidth + topDepiction[0].length;
        int bottomStartWidth = (resultWidth - bottomDepiction[0].length) / 2;
        int bottomEndWidth = bottomStartWidth + bottomDepiction[0].length;
        for (int i = 0; i < resultHeight; i++) {
            // Get the start and end widths for the current line.
            int startWidth = i < topDepiction.length ?
                    topStartWidth : bottomStartWidth;
            int endWidth = i < topDepiction.length ? topEndWidth : bottomEndWidth;
            for (int j = startWidth; j < endWidth; j++) {
                result[i][j] = i < topDepiction.length ?
                        topDepiction[i][j - startWidth] :
                        bottomDepiction[i - topDepiction.length][j - startWidth];
            }
            // Fill the remaining tiles with empty spaces.
            for (int j = 0; j < startWidth; j++) {
                result[i][j] = new DisplayTile(DisplayCharacter.of(' '));
            }
            for (int j = endWidth; j < resultWidth; j++) {
                result[i][j] = new DisplayTile(DisplayCharacter.of(' '));
            }
        }

        return result;
    }

    /**
     * Returns a hilt with or without a crossguard, made up of the specified
     * tiles, and with the specified dimensions. If the crossguard dimensions are
     * both 0, it is assumed there is no crossguard.
     * @param hiltHeight
     * @param hiltWidth
     * @param crossguardHeight
     * @param crossguardWidth
     * @param baseColour colour used for filling
     * @param accentColour colour used for the outline
     * @param leftGlyph
     * @param rightGlyph
     * @param topGlyph
     * @param bottomGlyph
     * @param fillGlyph
     * @param cornerType
     * @return
     */
    private DisplayTile[][] generateHilt(int hiltHeight, int hiltWidth,
                                         int crossguardHeight, int crossguardWidth, Color baseColour,
                                         Color accentColour, DisplayTile leftGlyph, DisplayTile rightGlyph,
                                         DisplayTile topGlyph, DisplayTile bottomGlyph, DisplayTile fillGlyph,
                                         int cornerType) {
        // Check for illegal arguments.
        if (hiltHeight <= 0) {
            throw new IllegalArgumentException("Hilt height " + hiltHeight +
                    " must be positive.");
        }
        if (hiltWidth <= 0) {
            throw new IllegalArgumentException("Hilt width " + hiltWidth +
                    " must be positive.");
        }
        if (crossguardHeight < 0 ||
                crossguardHeight == 0 && crossguardWidth != 0) {
            throw new IllegalArgumentException("Crossguard height " +
                    crossguardHeight + " must be positive.");
        }
        if (crossguardWidth < 0 ||
                crossguardWidth == 0 && crossguardHeight != 0) {
            throw new IllegalArgumentException("Crossguard width " +
                    crossguardWidth + " must be positive.");
        }
        if (baseColour == null) {
            throw new IllegalArgumentException("Base colour cannot be null.");
        }
        if (accentColour == null) {
            throw new IllegalArgumentException("Accent colour cannot be null.");
        }
        if (leftGlyph == null) {
            throw new IllegalArgumentException("Left glyph cannot be null.");
        }
        if (rightGlyph == null) {
            throw new IllegalArgumentException("Right glyph cannot be null.");
        }
        if (topGlyph == null) {
            throw new IllegalArgumentException("Top glyph cannot be null.");
        }
        if (bottomGlyph == null) {
            throw new IllegalArgumentException("Bottom glyph cannot be null.");
        }
        if (fillGlyph == null) {
            throw new IllegalArgumentException("Fill glyph cannot be null.");
        }
        if (cornerType < 0 || cornerType > 2) {
            throw new IllegalArgumentException("Corner type " + cornerType +
                    " must be between 0 and 2.");
        }

        // Generate the hilt.
        DisplayTile[][] display = drawRectangle(hiltHeight, hiltWidth, leftGlyph,
                rightGlyph, topGlyph, bottomGlyph, fillGlyph, cornerType);
        // Generate the crossguard and add it to the hilt, if necessary.
        if (crossguardHeight != 0 && crossguardWidth != 0) {
            DisplayTile[][] crossguard = drawRectangle(crossguardHeight,
                    crossguardWidth, leftGlyph, rightGlyph, topGlyph, bottomGlyph,
                    fillGlyph, cornerType);
            display = concatenateDepictions(display, crossguard);
        }

        return display;
    }

    private DisplayTile[][] generateBlade(int bladeHeight, int bladeWidth, DisplayColour accentColour, DisplayTile bottomGlyph,
                                          DisplayTile fillGlyph, int tipType, int cornerType) {
        int tipHeight = tipType > 2 ? 0 : tipType > 0 ? bladeWidth - 1 :
                bladeWidth / 2 + bladeWidth % 2;
        DisplayTile[][] blade;
        DisplayTile[][] tip;

        blade = drawRectangle(bladeHeight, bladeWidth,
                new DisplayTile(DisplayCharacter.of('|'), accentColour), new DisplayTile(DisplayCharacter.of('|'), accentColour),
                bottomGlyph, bottomGlyph, fillGlyph, cornerType);

        if (item.getModifier().getName().equals("Serrated")) {
            int side = Math.random() < 0.5 ? 0 : bladeWidth - 1;
            char sideGlyph = side == 0 ? '<' : '>';
            int start = Math.min(bladeHeight - 1, 2 + (int)(Math.random() * 6));
            int stop = Math.min(bladeHeight - 1, bladeHeight - 2 - (int)(Math.random() * 6));
            for (int i = start; i <= stop; i++) {
                blade[i][side] = new DisplayTile(DisplayCharacter.of(sideGlyph), accentColour);
            }
        }

        if (item.getModifier().getName().equals("Jagged")) {
            for (int i = 0; i < blade.length; i++) {
                if (i < blade.length - 2) {
                    double chance = Math.random();
                    if (chance < 0.2) {
                        blade[i][0] = new DisplayTile(DisplayCharacter.of('\\'),
                                blade[i][0].getForegroundColour());
                        i++;
                        blade[i][0] = new DisplayTile(DisplayCharacter.of('/'),
                                blade[i][0].getForegroundColour());
                    } else if (chance < 0.4) {
                        blade[i][blade[i].length - 1] = new DisplayTile(DisplayCharacter.of('/'),
                                blade[i][blade[i].length - 1].getForegroundColour());
                        i++;
                        blade[i][blade[i].length - 1] = new DisplayTile(DisplayCharacter.of('\\'),
                                blade[i][blade[i].length - 1].getForegroundColour());
                    }
                }
            }
        }

        tip = new DisplayTile[tipHeight][bladeWidth];

        switch (tipType) {
            case 0:
                for (int i = 0; i < bladeWidth / 2; i++) {
                    tip[i][i] = new DisplayTile(DisplayCharacter.of('\\'), accentColour);
                    tip[i][bladeWidth - i - 1] = new DisplayTile(DisplayCharacter.of('/'), accentColour);
                    if (i > 0) {
                        blade[bladeHeight - 1][i] = fillGlyph;
                        blade[bladeHeight - 1][bladeWidth - i - 1] = fillGlyph;
                    }
                }
                for (int i = 0; i < tipHeight; i++) {
                    boolean inBlade = false;
                    for (int j = 0; j < bladeWidth; j++) {
                        if (tip[i][j] != null) {
                            inBlade = !inBlade;
                            continue;
                        }
                        tip[i][j] = inBlade ? fillGlyph : new DisplayTile(DisplayCharacter.of(' '));
                    }
                }
                return concatenateDepictions(blade, tip);
            case 1:
                for (int i = 0; i < tipHeight; i++) {
                    tip[i][bladeWidth - 1] = new DisplayTile(DisplayCharacter.of('|'), accentColour);
                    tip[i][i] = new DisplayTile(DisplayCharacter.of('\\'), accentColour);
                    blade[bladeHeight - 1][i] = i > 0 && i < bladeWidth - 1 ?
                            fillGlyph : new DisplayTile(DisplayCharacter.of('|'), accentColour);
                    for (int j = i + 1; j < bladeWidth - 1; j++) {
                        tip[i][j] = fillGlyph;
                    }
                }
                blade[bladeHeight - 1][bladeWidth - 1] =
                        new DisplayTile(DisplayCharacter.of('|'), accentColour);
                for (int i = 0; i < tipHeight; i++) {
                    for (int j = 0; j < bladeWidth; j++) {
                        if (tip[i][j] == null) {
                            tip[i][j] = new DisplayTile(DisplayCharacter.of(' '));
                        }
                    }
                }
                return concatenateDepictions(blade, tip);
            case 2:
                for (int i = tipHeight - 1; i >= 0; i--) {
                    tip[tipHeight - 1 - i][0] = new DisplayTile(DisplayCharacter.of('|'), accentColour);
                    tip[tipHeight - 1 - i][i + 1] =
                            new DisplayTile(DisplayCharacter.of('/'), accentColour);
                    blade[bladeHeight - 1][i + 1] = fillGlyph;
                    for (int j = 1; j <= i; j++) {
                        tip[tipHeight - 1 - i][j] = fillGlyph;
                    }
                }
                blade[bladeHeight - 1][0] = new DisplayTile(DisplayCharacter.of('|'), accentColour);
                blade[bladeHeight - 1][bladeWidth - 1] =
                        new DisplayTile(DisplayCharacter.of('|'), accentColour);
                for (int i = 0; i < tipHeight; i++) {
                    for (int j = 0; j < bladeWidth; j++) {
                        if (tip[i][j] == null) {
                            tip[i][j] = new DisplayTile(DisplayCharacter.of(' '));
                        }
                    }
                }
                return concatenateDepictions(blade, tip);
            case 3:
                for (int i = 1; i < bladeWidth / 2; i++) {
                    blade[bladeHeight - 1][i] = new DisplayTile(DisplayCharacter.of(' '));
                    blade[bladeHeight - 1][bladeWidth - i - 1] = new DisplayTile(DisplayCharacter.of(' '));
                    blade[bladeHeight - i][i] =
                            new DisplayTile(DisplayCharacter.of('/'), accentColour);
                    blade[bladeHeight - i][bladeWidth - i - 1] =
                            new DisplayTile(DisplayCharacter.of('\\'), accentColour);
                }
                blade[bladeHeight - 1][0] = new DisplayTile(DisplayCharacter.of('|'), accentColour);
                blade[bladeHeight - 1][bladeWidth - 1] =
                        new DisplayTile(DisplayCharacter.of('|'), accentColour);
                if (bladeWidth % 2 == 1) {
                    blade[bladeHeight - bladeWidth / 2][bladeWidth / 2] =
                            new DisplayTile(DisplayCharacter.of('_'), accentColour);
                    blade[bladeHeight - 1][bladeWidth / 2] = new DisplayTile(DisplayCharacter.of(' '));
                }
                return blade;
            default:
                return blade;
        }
    }

    private DisplayTile[][] generateSword() {
        boolean hasCrossguard;
        int hiltHeight = 0;
        int hiltWidth = 0;
        int crossguardHeight = 0;
        int crossguardWidth = 0;
        int bladeWidth;
        int bladeHeight;
        int tipHeight;
        // 0 = middle tip, 1 = left tip, 2 = right tip, 3 = inward tip, 4 = no tip.
        int tipType;
        DisplayTile hiltLeftGlyph = null;
        DisplayTile hiltRightGlyph = null;
        DisplayTile hiltTopGlyph;
        DisplayTile hiltBottomGlyph;
        DisplayTile hiltFillGlyph;
        DisplayTile bladeBottomGlyph;
        DisplayTile bladeFillGlyph;
        Color hiltBaseColour;
        Color hiltAccentColour;
        DisplayColour displayHiltAccentColour;
        Color bladeBaseColour;
        Color bladeAccentColour;
        DisplayColour displayBladeAccentColour;
        int features = item.getItemLevel() / 5;
        boolean[] usedFeatures = new boolean[10];
        int[] featureCosts = {1, 1, 3, 2, 2, 2, 2, 3, 1, 5};
        DisplayTile[][] hilt;
        DisplayTile[][] blade;

        hiltBaseColour = COLOURS[(int)(Math.random() * COLOURS.length)];
        hiltAccentColour = COLOURS[(int)(Math.random() * COLOURS.length)];

        do {
            ArrayList<Integer> unusedFeatures = new ArrayList<Integer>();
            for (int i = 0; i < usedFeatures.length; i++) {
                if (!usedFeatures[i] && featureCosts[i] <= features) {
                    unusedFeatures.add(i);
                }
            }
            if (unusedFeatures.isEmpty()) {
                break;
            }

            int feature = unusedFeatures.get((int)(Math.random() *
                    unusedFeatures.size()));
            usedFeatures[feature] = true;
            features -= featureCosts[feature];
            switch (feature) {
                // Longer hilt.
                case 0:
                    hiltHeight = 4 + (int)(Math.random() * 3);
                    break;
                // Wider hilt.
                case 1:
                    hiltWidth = 3 + (int)(Math.random() * 2);
                    break;
                // Crossguard.
                case 2:
                    hasCrossguard = true;
                    break;
                // Side glyphs.
                case 3:
                    hiltLeftGlyph = new DisplayTile(hiltSideGlyphs[(int)(Math.random() *
                            hiltSideGlyphs.length)], DisplayColour.fromRgb("Colour", hiltAccentColour.getRed(), hiltAccentColour.getGreen(), hiltAccentColour.getBlue()));
                    hiltRightGlyph = getMatchingGlyph(hiltLeftGlyph, hiltSideGlyphs,
                            hiltSideGlyphConstraints);
                    break;
                // Make all hilt corners side glyphs.
                case 4:
                    break;
                // Longer blade.
                case 5:
                    break;
                // Wider blade.
                case 6:
                    break;
                // Pointy tip.
                case 7:
                    break;
                // Make all blade corners side glyphs.
                case 8:
                    break;
                // Add blade accent line.
                case 9:
                    break;
            }
        } while (features > 0);

        if (!usedFeatures[0]) {
            hiltHeight = 3 + (int)(Math.random() * 5);
        }
        if (!usedFeatures[1]) {
            hiltWidth = 2 + (int)(Math.random() * 3);
        }
        if (!usedFeatures[3]) {
            hiltLeftGlyph = new DisplayTile(hiltSideGlyphs[(int)(Math.random() *
                    hiltSideGlyphs.length)], DisplayColour.fromRgb("Colour", hiltAccentColour.getRed(), hiltAccentColour.getGreen(), hiltAccentColour.getBlue()));
            hiltRightGlyph = new DisplayTile(hiltSideGlyphs[(int)(Math.random() *
                    hiltSideGlyphs.length)], DisplayColour.fromRgb("Colour", hiltAccentColour.getRed(), hiltAccentColour.getGreen(), hiltAccentColour.getBlue()));
        }
        hiltTopGlyph = new DisplayTile(hiltBaseGlyphs[(int)(Math.random() *
                hiltBaseGlyphs.length)], DisplayColour.fromRgb("Colour", hiltAccentColour.getRed(), hiltAccentColour.getGreen(), hiltAccentColour.getBlue()));
        hiltBottomGlyph = new DisplayTile(hiltBaseGlyphs[(int)(Math.random() *
                hiltBaseGlyphs.length)], DisplayColour.fromRgb("Colour", hiltAccentColour.getRed(), hiltAccentColour.getGreen(), hiltAccentColour.getBlue()));
        hiltFillGlyph = new DisplayTile(hiltFillGlyphs[(int)(Math.random() *
                hiltFillGlyphs.length)], DisplayColour.fromRgb("Colour", hiltBaseColour.getRed(), hiltBaseColour.getGreen(), hiltBaseColour.getBlue()));

        if (!usedFeatures[2] && Math.random() < 0.5 || usedFeatures[2]) {
            crossguardWidth = hiltWidth + 1 + (int)(Math.random() * hiltWidth * 2);
            crossguardHeight = Math.min(hiltHeight - 1,
                    2 + (int)(Math.random() * 2));
        }

        if (usedFeatures[5]) {
            bladeHeight = Math.min(hiltHeight * (5 + (int)(Math.random() * 2)) +
                            (int)(Math.random() * 7),
                    InventoryScreen.DISPLAY_HEIGHT - hiltHeight - 8);
        } else {
            bladeHeight = Math.min(hiltHeight * (2 + (int)(Math.random() * 2)) +
                            (int)(Math.random() * 7),
                    InventoryScreen.DISPLAY_HEIGHT - hiltHeight - 8);
        }

        if (usedFeatures[6]) {
            bladeWidth = Math.min(4 + (int)(Math.random() * 3), hiltWidth);
        } else {
            bladeWidth = Math.min(3 + (int)(Math.random() * 3), hiltWidth);
        }

        do {
            bladeBaseColour = COLOURS[(int)(Math.random() * COLOURS.length)];
        } while (bladeBaseColour.equals(hiltBaseColour));
        do {
            bladeAccentColour = COLOURS[(int)(Math.random() * COLOURS.length)];
        } while (bladeAccentColour.equals(hiltAccentColour));
        bladeBottomGlyph = new DisplayTile(bladeBaseGlyphs[(int)(Math.random() *
                bladeBaseGlyphs.length)], DisplayColour.fromRgb("Colour", bladeAccentColour.getRed(), bladeAccentColour.getGreen(), bladeAccentColour.getBlue()));
        bladeFillGlyph = new DisplayTile(bladeFillGlyphs[(int)(Math.random() *
                bladeFillGlyphs.length)], DisplayColour.fromRgb("Colour", bladeBaseColour.getRed(), bladeBaseColour.getGreen(), bladeBaseColour.getBlue()));

        ArrayList<Integer> allowedTips = new ArrayList<Integer>();
        for (int i = 0; i < 5; i++) {
            allowedTips.add(i);
        }
        if (bladeWidth % 2 == 1) {
            allowedTips.set(0, -1);
            allowedTips.set(3, -1);
        }
        if (usedFeatures[7] || item.getModifier().getName().equals("Sharp")) {
            allowedTips.set(3, -1);
            allowedTips.set(4, -1);
        }
        boolean removed;
        do {
            removed = false;
            for (int i = 0; i < allowedTips.size(); i++) {
                if (allowedTips.get(i) == -1) {
                    allowedTips.remove(i);
                    removed = true;
                }
            }
        } while (removed);
        tipType = allowedTips.get((int)(Math.random() * allowedTips.size()));

        int hiltCornerType = usedFeatures[4] ? 1 : 0;
        int bladeCornerType = usedFeatures[8] ? 1 : 0;

        hilt = generateHilt(hiltHeight, hiltWidth, crossguardHeight,
                crossguardWidth, hiltBaseColour, hiltAccentColour, hiltLeftGlyph,
                hiltRightGlyph, hiltTopGlyph, hiltBottomGlyph, hiltFillGlyph,
                hiltCornerType);
        blade = generateBlade(bladeHeight, bladeWidth, DisplayColour.fromRgb("Colour", bladeAccentColour.getRed(), bladeAccentColour.getGreen(), bladeAccentColour.getBlue()), bladeBottomGlyph, bladeFillGlyph, tipType,
                bladeCornerType);

        if (item.getModifier().getName().equals("Rusty")) {
            for (int i = 0; i < blade.length; i++) {
                for (int j = 0; j < blade[i].length; j++) {
                    if (Math.random() < 0.3) {
                        blade[i][j] = new DisplayTile(blade[i][j].getDisplayCharacter(),
                                DisplayColour.BROWN);
                    }
                }
            }
        }

        if (usedFeatures[9]) {
            for (int i = 0; i < bladeHeight; i += 2) {
                DisplayColour qualityColour = Display.QUALITY_COLOURS[item.getQuality()];
                blade[i][bladeWidth / 2] = new DisplayTile(DisplayCharacter.ASTERISK, qualityColour);
                if (bladeWidth % 2 == 0) {
                    blade[i][bladeWidth / 2 - 1] = new DisplayTile(DisplayCharacter.ASTERISK, qualityColour);
                }
            }
        }

        return concatenateDepictions(hilt, blade);
    }

    public DisplayTile[][] generateDepiction() {
        if (item.getDepictionType() == Equipment.SWORD) {
            return generateSword();
        }
        DisplayTile[][] placeholder = {
                {new DisplayTile(DisplayCharacter.PLUS, DisplayColour.RED), new DisplayTile(DisplayCharacter.PLUS, DisplayColour.BLUE)},
                {new DisplayTile(DisplayCharacter.of('|'), DisplayColour.RED), new DisplayTile(DisplayCharacter.of('|'), DisplayColour.BLUE)},
                {new DisplayTile(DisplayCharacter.of('-'), DisplayColour.RED),
                        new DisplayTile(DisplayCharacter.of('-'), DisplayColour.BLUE, DisplayColour.YELLOW)}};
        return new DisplayTile[][]{{new DisplayTile(DisplayCharacter.of(' '))}};
    }

    /**
     * Given a constraint graph and a glyph, returns a glyph that is compatible
     * with the given one. A constraint graph dictates what glyphs match with
     * others in some specific context, e.g. the '(' glyph when used to draw the
     * sides of a sword's hilt may only be compatible with ')', i.e. the former
     * is used to draw the hilt's left side, and the latter to draw the glyph's
     * right side.
     * @param glyph
     * @param options all the available glyph options, in the order they appear
     *                in the constraint graph
     * @param constraints the constraint graph; if constraint[x][y] is true, then
     *                    glyphs options[x] and options[y] match
     * @return a matching glyph
     */
    private DisplayTile getMatchingGlyph(DisplayTile glyph, DisplayCharacter[] options,
                                         boolean[][] constraints) {
        int glyphIndex = 0;
        ArrayList<DisplayCharacter> compatibleGlyphs = new ArrayList<>();

        // Find the given glyph's index in the options array.
        for (int i = 0; i < options.length; i++) {
            if (options[i] == glyph.getDisplayCharacter()) {
                glyphIndex = i;
                break;
            }
        }
        // Find all the compatible glyphs for the given glyph.
        for (int i = 0; i < constraints[glyphIndex].length; i++) {
            if (constraints[glyphIndex][i]) {
                compatibleGlyphs.add(options[i]);
            }
        }

        return new DisplayTile(compatibleGlyphs.get(
                (int)(Math.random() * compatibleGlyphs.size())),
                glyph.getForegroundColour());
    }

}
