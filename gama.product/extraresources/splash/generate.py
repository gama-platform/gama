#!/usr/bin/env python3
"""Regenerate the blueprint splashes: python3 -m pip install cairosvg Pillow; python3 generate.py.

Run on macOS with Gotham Black, Avenir Next and Helvetica Neue installed (not bundled).
Uses artwork.svg, 40% colored fills, pale outlines, a 20px grid and seeded paper grain.
Text uses a 60px left margin, matching the artwork's right margin and StartupSplash.
Outputs 900x560 at 72 DPI and 1800x1120 at 144 DPI (900x560 logical points on macOS).
No renderer is needed at build or startup. Update the version and copyright below for a release.
"""

from copy import deepcopy
from io import BytesIO
from pathlib import Path
import random
import xml.etree.ElementTree as ET

import cairocffi as cairo
import cairosvg
from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parent
REPO = ROOT.parents[2]
SVG = "http://www.w3.org/2000/svg"
ET.register_namespace("", SVG)

artwork = ET.parse(ROOT / "artwork.svg").getroot()
artwork.attrib.update(x="390", y="62.5", width="450", height="435")
palette = {
    "#3570a0": "#428ac0", "#3770a1": "#428ac0", "#d3673a": "#dc7849",
    "#efb54f": "#e6b958", "#f8b332": "#f0b63e", "#a14423": "#bf623e",
}
for element in artwork.iter():
    for attribute in ("fill", "stroke"):
        color = element.get(attribute)
        if color in palette:
            element.set(attribute, palette[color])

splash = ET.Element(f"{{{SVG}}}svg", {
    "width": "900", "height": "560", "viewBox": "0 0 900 560",
})
ET.SubElement(splash, f"{{{SVG}}}rect", {
    "width": "900", "height": "560", "fill": "#183c55",
})


def text(value, y, size, color="#ccdbd6", family="Helvetica Neue", weight="normal"):
    element = ET.SubElement(splash, f"{{{SVG}}}text", {
        "x": "60", "y": str(y), "font-family": family,
        "font-size": str(size), "fill": color, "font-weight": weight,
    })
    element.text = value
    return element


metrics = cairo.Context(cairo.ImageSurface(cairo.FORMAT_ARGB32, 1, 1))
metrics.select_font_face("Gotham Black", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
metrics.set_font_size(72)
title_width = metrics.text_extents("GAMA")[2]
metrics.select_font_face("Avenir Next", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
version_size = 72 * title_width / metrics.text_extents("2026-11")[2]

text("GAMA", 110, 72, "#f0e9d8", "Gotham Black", "normal")
version = text(None, 170, version_size, "#428ac0", "Avenir Next", "normal")
ET.SubElement(version, f"{{{SVG}}}tspan").text = "2026-"
ET.SubElement(version, f"{{{SVG}}}tspan", {"fill": "#bf623e"}).text = "11"
text("https://gama-platform.org", 488, 18)
text("(c) UMMISCO & Partners 2007-2026", 516, 16)

source = ET.tostring(splash)


def render(document, scale):
    png = cairosvg.svg2png(bytestring=document, output_width=900 * scale, output_height=560 * scale)
    with Image.open(BytesIO(png)) as image:
        return image.convert("RGBA")


# Preserve each arc's transparency and its separate outline-only path.
layers = []
for group in artwork.findall(".//{*}g[@id='Canevas_1_Calque_3']/{*}g"):
    layer_artwork = deepcopy(artwork)
    container = layer_artwork.find(".//{*}g[@id='Canevas_1_Calque_3']")
    for child in list(container):
        container.remove(child)
    container.append(deepcopy(group))
    layer = ET.Element(f"{{{SVG}}}svg", {"width": "900", "height": "560", "viewBox": "0 0 900 560"})
    layer.append(layer_artwork)
    for element in layer_artwork.iter(f"{{{SVG}}}path"):
        element.set("fill-opacity", str(float(element.get("fill-opacity", "1")) * 0.4))
        element.set("stroke", "#d4dfd8")
        element.set("stroke-width", "0.8")
    layers.append(ET.tostring(layer))

for scale, dpi, destination in (
    (1, 72, REPO / "gama.ui.application/splash.png"),
    (2, 144, ROOT / "splash-macos.png"),
):
    image = render(source, scale)
    for layer in layers:
        image = Image.alpha_composite(image, render(layer, scale))
    grid = Image.new("RGBA", image.size)
    draw = ImageDraw.Draw(grid)
    for x in range(0, image.width, 20 * scale):
        draw.line((x, 0, x, image.height), fill=(195, 220, 220, 14), width=scale)
    for y in range(0, image.height, 20 * scale):
        draw.line((0, y, image.width, y), fill=(195, 220, 220, 14), width=scale)
    image = Image.alpha_composite(image, grid).convert("RGB")
    noise = random.Random(202611)
    image.putdata([
        tuple(max(0, min(255, round(channel + noise.gauss(0, 0.8)))) for channel in pixel)
        for pixel in image.getdata()
    ])
    image.save(destination, dpi=(dpi, dpi))
    print(destination)
