/**
* Name: Color Brewer
* Author: Arnaud Grignard, Patrick Taillandier
* Description: Catalogue of every ColorBrewer palette exposed by brewer_palettes, plus the vintage
*   Swiss, Warm Amber and Mineral palettes. Each labelled row shows the native list of colours.
*   A second experiment assigns a selected number of colours to grid agents.
* Tags: color, colorbrewer, palette, visualization, display, cartography, brewer_colors, vintage_colors
*/
model ColorBrewer

global {
    int nb_classes <- 6 min: 1 max: 15;
    list<string> palettes <- brewer_palettes(nb_classes);
    list<string> brewer_names <- brewer_palettes(1) sort_by each;
    list<string> vintage_names <- ["Swiss", "Warm Amber", "Mineral"];
    list<string> qualitative_names <- ["Accents", "Paired", "Set1", "Set2", "Set3", "Dark2", "Pastel1", "Pastel2"];
    list<string> diverging_names <- ["PRGn", "PuOr", "RdGy", "Spectral", "RdYlGn", "RdBu", "RdYlBu", "PiYG", "BrBG"];
    list<string> catalogue_names <- brewer_names + vintage_names;
    list<string> catalogue_kinds <- (brewer_names collect (qualitative_names contains each ? "Qualitative"
        : (diverging_names contains each ? "Diverging" : "Sequential"))) + (vintage_names collect "Vintage");
    list<list<rgb>> catalogue_colors <- (brewer_names collect brewer_colors(each))
        + (vintage_names collect vintage_colors(each));

    init {
        loop i from: 0 to: length(palettes) - 1 {
            list<rgb> colors <- brewer_colors(palettes[i], nb_classes);
            ask cell where (each.grid_y = i) {
                palette_name <- palettes[i];
                color <- colors[grid_x];
            }
        }
    }
}

grid cell width: nb_classes height: max([1, length(palettes)]) {
    string palette_name;
}

experiment BrewerPalette type: gui {
    text "Purpose: browse every available ColorBrewer palette and the newer Swiss, Warm Amber and Mineral vintage inks. Each row names its palette and shows its complete native colour list; counts can differ. The catalogue is populated from brewer_palettes(1), so future ColorBrewer additions appear automatically." category: "What this demonstrates";
    text "Sequential palettes encode ordered values; diverging palettes emphasize departures from a midpoint; qualitative palettes distinguish categories without ordering. Pastel1, the former third example row, is qualitative. Vintage inks are also categorical, not sequential data scales." category: "What this demonstrates";
    text "Try: resize the display; no simulation steps are needed. Columns adapt to the viewport. Borders make very pale colours visible against white. The final three rows are labelled Vintage and use vintage_colors(name), not brewer_colors(name). Use the second experiment to assign colour classes to agents." category: "What this demonstrates";

    output {
        display "All palettes" type: java2D background: #white {
            graphics "Catalogue" coordinates: #screen {
                int columns <- min([3, max([1, int(#display_width / 360)])]);
                int rows <- int(ceil(length(catalogue_names) / float(columns)));
                float column_width <- (#display_width - 32) / columns;
                float row_height <- max([12.0, min([38.0, (#display_height - 48) / rows])]);
                draw "Available palettes: names, colour counts and native swatches"
                    at: {16, 20} anchor: #left_center color: #black font: font("Helvetica", 14, #bold);
                loop i from: 0 to: length(catalogue_names) - 1 {
                    int column <- int(i / rows);
                    int row <- i mod rows;
                    float x <- 16 + column * column_width;
                    float y <- 40 + row * row_height;
                    list<rgb> colors <- catalogue_colors[i];
                    draw (catalogue_names[i] + " (" + length(colors) + ") - " + catalogue_kinds[i])
                        at: {x, y} anchor: #left_center color: #black
                        font: font("Helvetica", max([8, int(row_height * 0.32)]));
                    float swatch_width <- (column_width - 16) / length(colors);
                    loop j from: 0 to: length(colors) - 1 {
                        // Intrinsic geometry lengths are pixels, not fractional at:/size: components.
                        draw rectangle(swatch_width, row_height * 0.42)
                            at: {x + (j + 0.5) * swatch_width, y + row_height * 0.4}
                            color: colors[j] border: #gray;
                    }
                }
            }
        }
    }
}

experiment BrewerColoredAgent type: gui {
    parameter "Number of data classes" var: nb_classes category: "Brewer";
    text "Purpose: assign palette colours to grid agents. Each row is a ColorBrewer palette and each column is one class.<br/>Try: choose the number of classes before initialization, then initialize; no simulation steps are needed.<br/>Expected: only palettes with at least that many native colours are included. Increasing the count can exclude qualitative palettes; it does not interpolate missing colours. Vintage inks are shown in the catalogue experiment." category: "What this demonstrates";
    output {
        display "Palette classes" type: java2D {
            grid cell border: #black;
            graphics "Names" coordinates: #world {
                loop i from: 0 to: length(palettes) - 1 {
                    draw palettes[i] at: cell[0, i].location color: #black anchor: #left_center;
                }
            }
        }
    }
}
