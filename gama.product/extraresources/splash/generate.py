#!/usr/bin/env python3
"""Regenerate the Swiss balanced splashes: python3 -m pip install cairosvg Pillow; python3 generate.py.

Run on macOS with Gotham Black, Avenir Next and Helvetica Neue installed (not bundled).
Blends the pale Swiss artwork and original multiply-blended inks equally on cream paper, with seeded grain and no grid.
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
from PIL import Image, ImageChops


ROOT = Path(__file__).resolve().parent
REPO = ROOT.parents[2]
SVG = "http://www.w3.org/2000/svg"
ET.register_namespace("", SVG)

artwork = ET.parse(ROOT / "artwork.svg").getroot()
artwork.attrib.update(x="390", y="62.5", width="450", height="435")
palette = {
    "#3570a0": "#28516a", "#3770a1": "#28516a", "#d3673a": "#c75d36",
    "#efb54f": "#d7ab53", "#f8b332": "#e6b23d", "#a14423": "#9a432e",
}
for element in artwork.iter():
    for attribute in ("fill", "stroke"):
        color = element.get(attribute)
        if color in palette:
            element.set(attribute, palette[color])

splash = ET.Element(f"{{{SVG}}}svg", {
    "width": "900", "height": "560", "viewBox": "0 0 900 560",
})
paper = ET.SubElement(splash, f"{{{SVG}}}rect", {
    "width": "900", "height": "560", "fill": "#f8f6f0",
})


def text(value, y, size, color="#39423d", family="Helvetica Neue", weight="normal"):
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

text("GAMA", 110, 72, "#242823", "Gotham Black", "normal")
version = text(None, 170, version_size, "#28516a", "Avenir Next", "normal")
ET.SubElement(version, f"{{{SVG}}}tspan").text = "2026-"
ET.SubElement(version, f"{{{SVG}}}tspan", {"fill": "#9a432e"}).text = "11"
text("https://gama-platform.org", 488, 18)
text("\u00a9 UMMISCO & Partners 2007-2026", 516, 16)

source = ET.tostring(splash)
original_source = ET.tostring(splash)


def render(document, scale):
    png = cairosvg.svg2png(bytestring=document, output_width=900 * scale, output_height=560 * scale)
    with Image.open(BytesIO(png)) as image:
        return image.convert("RGBA")


# Preserve each arc's transparency and its separate outline-only path.
layers = []
original_layers = []
for group in artwork.findall(".//{*}g[@id='Canevas_1_Calque_3']/{*}g"):
    layer_artwork = deepcopy(artwork)
    container = layer_artwork.find(".//{*}g[@id='Canevas_1_Calque_3']")
    for child in list(container):
        container.remove(child)
    container.append(deepcopy(group))
    layer = ET.Element(f"{{{SVG}}}svg", {"width": "900", "height": "560", "viewBox": "0 0 900 560"})
    layer.append(layer_artwork)
    original_layers.append(ET.tostring(layer))
    for element in layer_artwork.iter(f"{{{SVG}}}path"):
        element.set("fill-opacity", str(float(element.get("fill-opacity", "1")) * 0.38))
        element.set("stroke", "#979b90")
        element.set("stroke-width", "0.5")
        element.set("stroke-opacity", "0.45")
    layers.append(ET.tostring(layer))


def grain(image):
    image = image.convert("RGB")
    noise = random.Random(202611)
    image.putdata([
        tuple(max(0, min(255, round(channel + noise.gauss(0, 0.8)))) for channel in pixel)
        for pixel in image.getdata()
    ])
    return image


for scale, dpi, destination in (
    (1, 72, REPO / "gama.ui.application/splash.png"),
    (2, 144, ROOT / "splash-macos.png"),
):
    image = render(source, scale)
    for layer in layers:
        image = Image.alpha_composite(image, render(layer, scale))
    original = render(original_source, scale).convert("RGB")
    for layer in original_layers:
        foreground = render(layer, scale)
        multiplied = ImageChops.multiply(original, foreground.convert("RGB"))
        original = Image.composite(multiplied, original, foreground.getchannel("A"))
    image = Image.blend(grain(image), grain(original), 0.5)
    image.save(destination, dpi=(dpi, dpi))
    print(destination)
