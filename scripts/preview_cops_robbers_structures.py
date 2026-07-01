#!/usr/bin/env python3
from __future__ import annotations

from dataclasses import dataclass, field
from html import escape
from pathlib import Path


BlockMap = dict[tuple[int, int, int], str]


COLORS = {
    "air": ("#ffffff", " "),
    "smooth_stone": ("#a6a6a6", "S"),
    "quartz": ("#ece8df", "Q"),
    "smooth_quartz": ("#f7f5ef", "R"),
    "light_gray_concrete": ("#9a9a9a", "P"),
    "yellow_concrete": ("#ffd84d", "Y"),
    "iron_bars": ("#5f6670", "B"),
    "glass": ("#aee8ff", "G"),
    "iron_door": ("#cad0d8", "D"),
    "iron_block": ("#dfe4e9", "I"),
    "button": ("#606060", "*"),
    "stone_pressure_plate": ("#b8b8b8", "^"),
    "oak_pressure_plate": ("#b88a50", "^"),
    "glowstone": ("#ffd36a", "L"),
    "sea_lantern": ("#d8fff8", "F"),
    "lantern": ("#ffb84d", "l"),
    "blue_concrete": ("#2d55d8", "b"),
    "white_concrete": ("#f6f6f6", "w"),
    "red_concrete": ("#c63737", "r"),
    "oak_sign": ("#b98247", "="),
    "oak_planks": ("#b88a50", "O"),
    "white_wool": ("#f0f0e8", "W"),
    "oak_log": ("#83562c", "T"),
    "oak_door": ("#a66b32", "d"),
    "glass_pane": ("#c9f3ff", "g"),
    "oak_slab": ("#c79a60", "_"),
    "red_wool": ("#b33a3a", "m"),
    "red_carpet": ("#c83f3f", "c"),
    "chest": ("#b66d28", "C"),
    "gold_block": ("#f8c93c", "$"),
    "mossy_cobblestone": ("#6f7f68", "M"),
}

PASSABLE = {"air", "button", "oak_sign", "stone_pressure_plate", "oak_pressure_plate", "red_carpet"}
DOORS = {"iron_door", "oak_door"}
GLASS = {"glass", "glass_pane"}
TEXTURE_NOTES = {
    "quartz": "station wall texture, add trim and decals",
    "smooth_quartz": "flat roof cap, good candidate for parapet detail",
    "light_gray_concrete": "driveway and parking pad, later add painted lines",
    "yellow_concrete": "drop-off marker, later add hazard stripes",
    "iron_bars": "jail bars, preview should show cell sealing",
    "iron_block": "solid jail door frame that visually meets bars",
    "glass": "large jail/back wall window",
    "glass_pane": "bank window panes",
    "oak_planks": "bank floor and burnable wood features",
    "oak_log": "bank trim, later use beams/frames",
    "red_concrete": "fire station exterior wall",
    "white_wool": "burnable bank wall and office partitions",
    "glowstone": "interior lights",
    "sea_lantern": "fluorescent strip lighting for public-service buildings",
    "lantern": "warm old-school lighting for banks and hideouts",
}
TEXTURED_BLOCKS = {
    "smooth_stone",
    "quartz",
    "smooth_quartz",
    "light_gray_concrete",
    "yellow_concrete",
    "iron_bars",
    "glass",
    "glowstone",
    "oak_planks",
    "white_wool",
    "oak_log",
    "oak_door",
    "glass_pane",
    "red_wool",
    "chest",
    "gold_block",
    "mossy_cobblestone",
    "iron_block",
}


@dataclass
class Structure:
    name: str
    blocks: BlockMap = field(default_factory=dict)

    def set(self, x: int, y: int, z: int, block: str) -> None:
        if block == "air":
            self.blocks.pop((x, y, z), None)
        else:
            self.blocks[(x, y, z)] = block

    def fill(self, min_x: int, min_y: int, min_z: int, max_x: int, max_y: int, max_z: int, block: str) -> None:
        for x in range(min_x, max_x + 1):
            for y in range(min_y, max_y + 1):
                for z in range(min_z, max_z + 1):
                    self.set(x, y, z, block)

    def door(self, x: int, y: int, z: int, block: str) -> None:
        self.set(x, y, z, block)
        self.set(x, y + 1, z, block)


def police_station() -> Structure:
    s = Structure("Police station")
    s.fill(-8, 0, -4, 8, 0, 13, "smooth_stone")
    s.fill(-8, 1, 13, 8, 4, 13, "quartz")
    s.fill(-8, 1, -4, -8, 4, 13, "quartz")
    s.fill(8, 1, -4, 8, 4, 13, "quartz")
    s.fill(-8, 4, -4, 8, 4, 13, "smooth_quartz")
    s.fill(-8, 1, -4, -3, 3, -4, "quartz")
    s.fill(3, 1, -4, 8, 3, -4, "quartz")
    s.fill(-2, 3, -4, 2, 3, -4, "quartz")
    s.fill(-7, 2, -4, -5, 2, -4, "glass_pane")
    s.fill(5, 2, -4, 7, 2, -4, "glass_pane")
    s.fill(-2, 1, -4, -1, 2, -4, "glass")
    s.fill(1, 1, -4, 2, 2, -4, "glass")
    s.fill(8, 2, 0, 8, 2, 3, "glass_pane")
    s.fill(-7, 1, -3, 7, 3, 12, "air")
    s.set(0, 0, -5, "smooth_stone")
    s.door(0, 1, -4, "iron_door")
    s.set(0, 1, -5, "stone_pressure_plate")
    s.set(0, 1, -3, "stone_pressure_plate")
    s.set(0, 4, -5, "oak_sign")
    s.fill(3, 1, -3, 7, 1, 12, "light_gray_concrete")
    s.fill(3, 1, -4, 7, 3, -4, "quartz")
    s.fill(3, 2, -4, 4, 2, -4, "glass")
    s.fill(6, 2, -4, 7, 2, -4, "glass")
    s.set(5, 0, -5, "smooth_stone")
    s.door(5, 1, -4, "iron_door")
    s.set(5, 1, -5, "stone_pressure_plate")
    s.set(5, 1, -3, "stone_pressure_plate")
    s.fill(-4, -1, 14, 4, -1, 20, "light_gray_concrete")
    s.fill(-1, -1, 15, 1, -1, 19, "yellow_concrete")
    s.fill(-7, 1, 3, -2, 3, 10, "air")
    s.fill(-7, 1, 2, -2, 3, 2, "iron_bars")
    s.fill(-7, 1, 11, -2, 3, 11, "iron_bars")
    s.fill(-7, 1, 3, -7, 3, 10, "iron_bars")
    s.fill(-2, 1, 3, -2, 3, 10, "iron_bars")
    s.fill(-2, 1, 5, -2, 2, 7, "iron_block")
    s.door(-2, 1, 6, "iron_door")
    s.set(-1, 1, 6, "button")
    s.set(-3, 1, 6, "button")
    s.fill(-6, 4, -1, -3, 4, -1, "sea_lantern")
    s.fill(-6, 4, 7, -3, 4, 7, "sea_lantern")
    s.fill(2, 4, 1, 6, 4, 1, "sea_lantern")
    s.fill(2, 4, 7, 6, 4, 7, "sea_lantern")
    s.fill(0, 1, 13, 0, 2, 13, "air")
    s.set(0, 0, 14, "smooth_stone")
    s.door(0, 1, 13, "iron_door")
    s.set(0, 1, 14, "stone_pressure_plate")
    s.set(0, 1, 12, "stone_pressure_plate")
    s.set(0, 3, 14, "oak_sign")
    s.set(-1, 5, -4, "blue_concrete")
    s.set(0, 5, -4, "white_concrete")
    s.set(1, 5, -4, "red_concrete")
    return s


def bank() -> Structure:
    s = Structure("Bank")
    s.fill(-7, 0, -4, 7, 0, 11, "oak_planks")
    s.fill(-7, 1, 11, 7, 4, 11, "white_wool")
    s.fill(-7, 1, -4, -7, 4, 11, "white_wool")
    s.fill(7, 1, -4, 7, 4, 11, "white_wool")
    s.fill(-7, 4, -4, 7, 4, 11, "oak_planks")
    s.fill(-7, 1, -4, -2, 3, -4, "oak_log")
    s.fill(2, 1, -4, 7, 3, -4, "oak_log")
    s.fill(-1, 3, -4, 1, 3, -4, "oak_log")
    s.fill(-6, 1, -3, 6, 3, 10, "air")
    s.fill(-1, 1, -4, -1, 2, -4, "glass")
    s.fill(1, 1, -4, 1, 2, -4, "glass")
    s.set(0, 0, -5, "oak_planks")
    s.door(0, 1, -4, "oak_door")
    s.set(0, 1, -5, "oak_pressure_plate")
    s.fill(-5, 2, -4, -3, 2, -4, "glass_pane")
    s.fill(3, 2, -4, 5, 2, -4, "glass_pane")
    s.fill(-7, 2, 0, -7, 2, 3, "glass_pane")
    s.fill(7, 2, 0, 7, 2, 3, "glass_pane")
    s.fill(-5, 1, 1, 5, 1, 1, "oak_planks")
    s.fill(-5, 2, 1, 5, 2, 1, "oak_slab")
    s.fill(-4, 1, -2, 4, 1, -2, "red_carpet")
    s.fill(-5, 1, 5, 5, 3, 9, "iron_bars")
    s.fill(-4, 1, 6, 4, 2, 8, "air")
    s.fill(-3, 1, 8, 3, 1, 8, "chest")
    s.fill(0, 1, 5, 1, 2, 5, "air")
    s.fill(-7, 1, -3, 7, 1, -3, "oak_log")
    s.set(0, 1, -3, "oak_pressure_plate")
    s.set(-1, 5, -4, "gold_block")
    s.set(0, 5, -4, "white_wool")
    s.set(1, 5, -4, "gold_block")
    s.set(0, 4, -5, "oak_sign")
    s.set(-4, 3, 0, "lantern")
    s.set(4, 3, 0, "lantern")
    s.set(0, 3, 7, "lantern")
    return s


def hideout() -> Structure:
    s = Structure("Robber hideout")
    s.fill(-5, 1, -4, 5, 1, 4, "mossy_cobblestone")
    s.fill(-5, 2, -4, 5, 3, -4, "oak_planks")
    s.fill(-5, 2, 4, 5, 3, 4, "oak_planks")
    s.fill(-5, 2, -3, -5, 3, 4, "mossy_cobblestone")
    s.fill(5, 2, -4, 5, 3, 4, "oak_planks")
    s.fill(-4, 4, -3, 4, 4, 3, "oak_slab")
    s.fill(-4, 2, -3, 4, 3, 3, "air")
    s.set(0, 1, 5, "oak_planks")
    s.door(0, 2, 4, "oak_door")
    s.set(0, 2, 5, "oak_pressure_plate")
    s.set(0, 2, 3, "oak_pressure_plate")
    s.fill(-3, 3, -4, -2, 3, -4, "glass_pane")
    s.fill(2, 3, -4, 3, 3, -4, "glass_pane")
    s.fill(5, 3, -1, 5, 3, 1, "glass_pane")
    s.set(0, 4, 5, "oak_sign")
    s.set(0, 2, 0, "chest")
    s.set(-3, 3, -2, "lantern")
    s.set(3, 3, 2, "lantern")
    return s


def fire_station() -> Structure:
    s = Structure("Fire station")
    s.fill(-8, 1, -5, 8, 1, 10, "smooth_stone")
    s.fill(-8, 2, -5, 8, 4, -5, "red_concrete")
    s.fill(-8, 2, 10, 8, 4, 10, "red_concrete")
    s.fill(-8, 2, -5, -8, 4, 10, "red_concrete")
    s.fill(8, 2, -5, 8, 4, 10, "red_concrete")
    s.fill(-8, 5, -5, 8, 5, 10, "smooth_quartz")
    s.fill(-7, 2, -4, 7, 4, 9, "air")
    s.fill(-4, 2, -5, 4, 4, -5, "glass_pane")
    s.fill(-1, 2, -5, 1, 4, -5, "white_concrete")
    s.set(0, 1, -6, "smooth_stone")
    s.door(0, 2, -5, "iron_door")
    s.set(0, 2, -6, "stone_pressure_plate")
    s.set(0, 2, -4, "stone_pressure_plate")
    s.fill(-7, 3, -5, -5, 3, -5, "glass_pane")
    s.fill(5, 3, -5, 7, 3, -5, "glass_pane")
    s.fill(-8, 3, 0, -8, 3, 3, "glass_pane")
    s.fill(8, 3, 0, 8, 3, 3, "glass_pane")
    s.fill(-7, 2, 3, -5, 4, 8, "white_wool")
    s.fill(-5, 4, 0, -1, 4, 0, "sea_lantern")
    s.fill(1, 4, 0, 5, 4, 0, "sea_lantern")
    s.fill(-5, 4, 6, -1, 4, 6, "sea_lantern")
    s.fill(1, 4, 6, 5, 4, 6, "sea_lantern")
    s.set(0, 6, -6, "oak_sign")
    s.set(0, 6, -5, "white_concrete")
    s.set(-1, 6, -5, "red_concrete")
    s.set(1, 6, -5, "red_concrete")
    return s


def bounds(structures: list[Structure]) -> tuple[int, int, int, int, int, int]:
    coords = [coord for structure in structures for coord in structure.blocks]
    return (
        min(x for x, _, _ in coords),
        max(x for x, _, _ in coords),
        min(y for _, y, _ in coords),
        max(y for _, y, _ in coords),
        min(z for _, _, z in coords),
        max(z for _, _, z in coords),
    )


def hex_to_rgb(color: str) -> tuple[int, int, int]:
    color = color.lstrip("#")
    return int(color[0:2], 16), int(color[2:4], 16), int(color[4:6], 16)


def shade(color: str, factor: float) -> str:
    red, green, blue = hex_to_rgb(color)
    return f"#{int(red * factor):02x}{int(green * factor):02x}{int(blue * factor):02x}"


def block_color(block: str) -> str:
    return COLORS.get(block, ("#dddddd", "?"))[0]


def is_passable(structure: Structure, x: int, y: int, z: int) -> bool:
    feet = structure.blocks.get((x, y, z), "air")
    head = structure.blocks.get((x, y + 1, z), "air")
    return feet in PASSABLE and head in PASSABLE


def has_floor(structure: Structure, x: int, y: int, z: int) -> bool:
    return structure.blocks.get((x, y - 1, z), "air") not in PASSABLE


def render_walk_layer(structure: Structure, y: int) -> str:
    min_x, max_x, _, _, min_z, max_z = bounds([structure])
    rows = []
    for z in range(min_z, max_z + 1):
        cells = []
        for x in range(min_x, max_x + 1):
            block = structure.blocks.get((x, y, z), "air")
            if block in DOORS:
                label, css = "D", "door"
            elif is_passable(structure, x, y, z) and has_floor(structure, x, y, z):
                label, css = ".", "walk"
            elif is_passable(structure, x, y, z):
                label, css = "~", "void"
            else:
                label, css = "#", "blocked"
            title = escape(f"x={x}, y={y}, z={z}: {block}")
            cells.append(f'<td class="{css}" title="{title}">{label}</td>')
        rows.append(f"<tr>{''.join(cells)}</tr>")
    return f'<table class="grid walk-grid"><tbody>{"".join(rows)}</tbody></table>'


def render_isometric(structure: Structure) -> str:
    tile_w = 24
    tile_h = 12
    block_h = 18
    raw_points = []
    for x, y, z in structure.blocks:
        sx = (x - z) * tile_w / 2
        sy = (x + z) * tile_h / 2 - y * block_h
        raw_points.extend([(sx - tile_w / 2, sy), (sx + tile_w / 2, sy + tile_h), (sx, sy + tile_h + block_h)])
    min_x = min(point[0] for point in raw_points)
    max_x = max(point[0] for point in raw_points)
    min_y = min(point[1] for point in raw_points)
    max_y = max(point[1] for point in raw_points)
    pad = 28
    width = int(max_x - min_x + pad * 2)
    height = int(max_y - min_y + pad * 2)

    def project(x: int, y: int, z: int) -> tuple[float, float]:
        return (x - z) * tile_w / 2 - min_x + pad, (x + z) * tile_h / 2 - y * block_h - min_y + pad

    polygons = [svg_texture_defs()]
    for x, y, z in sorted(structure.blocks, key=lambda coord: (coord[0] + coord[2], coord[1], coord[2], coord[0])):
        block = structure.blocks[(x, y, z)]
        base = block_color(block)
        opacity = "0.58" if block in GLASS else "1"
        sx, sy = project(x, y, z)
        top = [(sx, sy), (sx + tile_w / 2, sy + tile_h / 2), (sx, sy + tile_h), (sx - tile_w / 2, sy + tile_h / 2)]
        left = [(sx - tile_w / 2, sy + tile_h / 2), (sx, sy + tile_h), (sx, sy + tile_h + block_h), (sx - tile_w / 2, sy + tile_h / 2 + block_h)]
        right = [(sx + tile_w / 2, sy + tile_h / 2), (sx, sy + tile_h), (sx, sy + tile_h + block_h), (sx + tile_w / 2, sy + tile_h / 2 + block_h)]
        title = escape(f"{block} at x={x}, y={y}, z={z}")
        for face_name, face, color in (("left", left, shade(base, 0.76)), ("right", right, shade(base, 0.88)), ("top", top, base)):
            fill = texture_fill(block, color, face_name)
            points = " ".join(f"{px:.1f},{py:.1f}" for px, py in face)
            polygons.append(f'<polygon points="{points}" fill="{fill}" fill-opacity="{opacity}" stroke="#30363d" stroke-width="0.45"><title>{title}</title></polygon>')
        if block == "iron_bars":
            for offset in (-4, 0, 4):
                polygons.append(f'<line x1="{sx + offset:.1f}" y1="{sy + tile_h:.1f}" x2="{sx + offset:.1f}" y2="{sy + tile_h + block_h:.1f}" stroke="#1f2328" stroke-width="1.2"/>')
        if block in DOORS:
            polygons.append(f'<circle cx="{sx + 4:.1f}" cy="{sy + tile_h + 8:.1f}" r="1.5" fill="#202124"/>')
    return f'<svg class="iso" viewBox="0 0 {width} {height}" role="img" aria-label="{escape(structure.name)} isometric preview">{"".join(polygons)}</svg>'


def texture_fill(block: str, color: str, face_name: str) -> str:
    if face_name == "top" and block in TEXTURED_BLOCKS:
        return f"url(#tex-{block.replace('_', '-')})"
    return color


def svg_texture_defs() -> str:
    def pattern(block: str, body: str) -> str:
        return f'<pattern id="tex-{block.replace("_", "-")}" patternUnits="userSpaceOnUse" width="16" height="16">{body}</pattern>'

    bodies = []
    bodies.append(pattern("smooth_stone", '<rect width="16" height="16" fill="#a6a6a6"/><path d="M0 6H16M5 0V6M11 6V16" stroke="#8b8b8b" stroke-width="1"/>'))
    bodies.append(pattern("quartz", '<rect width="16" height="16" fill="#ece8df"/><path d="M2 3H14M4 10H16" stroke="#d8d0c4" stroke-width="1"/><path d="M7 0V16" stroke="#f7f5ef" stroke-width="1"/>'))
    bodies.append(pattern("smooth_quartz", '<rect width="16" height="16" fill="#f7f5ef"/><path d="M0 8H16M8 0V16" stroke="#e4ded2" stroke-width="1"/>'))
    bodies.append(pattern("light_gray_concrete", '<rect width="16" height="16" fill="#9a9a9a"/><circle cx="4" cy="5" r="1" fill="#858585"/><circle cx="11" cy="9" r="1" fill="#b3b3b3"/><circle cx="7" cy="13" r="1" fill="#777"/>'))
    bodies.append(pattern("yellow_concrete", '<rect width="16" height="16" fill="#ffd84d"/><path d="M-4 16L16 -4M2 20L20 2" stroke="#1f2328" stroke-width="2" opacity=".35"/>'))
    bodies.append(pattern("iron_bars", '<rect width="16" height="16" fill="#8a929d"/><path d="M3 0V16M8 0V16M13 0V16" stroke="#25292e" stroke-width="2"/>'))
    bodies.append(pattern("glass", '<rect width="16" height="16" fill="#aee8ff" opacity=".55"/><path d="M2 14L14 2M9 16L16 9" stroke="#ffffff" stroke-width="1.5" opacity=".85"/>'))
    bodies.append(pattern("glass_pane", '<rect width="16" height="16" fill="#c9f3ff" opacity=".55"/><path d="M4 0V16M12 0V16M0 8H16" stroke="#ffffff" stroke-width="1" opacity=".75"/>'))
    bodies.append(pattern("glowstone", '<rect width="16" height="16" fill="#ffd36a"/><path d="M0 8H16M8 0V16" stroke="#fff1a8" stroke-width="2"/><circle cx="4" cy="4" r="2" fill="#fff3b0"/><circle cx="12" cy="12" r="2" fill="#f4a93a"/>'))
    bodies.append(pattern("oak_planks", '<rect width="16" height="16" fill="#b88a50"/><path d="M0 4H16M0 10H16M5 0V4M11 4V10M7 10V16" stroke="#805628" stroke-width="1"/><path d="M3 2C6 4 8 1 12 3" stroke="#d0a76d" stroke-width="1" fill="none"/>'))
    bodies.append(pattern("oak_log", '<rect width="16" height="16" fill="#83562c"/><path d="M1 4C5 1 9 7 15 3M1 11C5 8 10 14 15 10" stroke="#c08a50" stroke-width="1.5" fill="none"/>'))
    bodies.append(pattern("oak_door", '<rect width="16" height="16" fill="#a66b32"/><rect x="3" y="2" width="10" height="12" fill="none" stroke="#5c3517" stroke-width="1.5"/><circle cx="12" cy="8" r="1.5" fill="#222"/>'))
    bodies.append(pattern("white_wool", '<rect width="16" height="16" fill="#f0f0e8"/><path d="M0 4H16M0 12H16M4 0V16M12 0V16" stroke="#deded4" stroke-width="1"/>'))
    bodies.append(pattern("red_wool", '<rect width="16" height="16" fill="#b33a3a"/><path d="M0 4H16M0 12H16M4 0V16M12 0V16" stroke="#922d2d" stroke-width="1"/>'))
    bodies.append(pattern("chest", '<rect width="16" height="16" fill="#b66d28"/><rect x="2" y="3" width="12" height="10" fill="none" stroke="#6b3d18" stroke-width="1.5"/><rect x="6" y="6" width="4" height="4" fill="#f0c34e"/>'))
    bodies.append(pattern("gold_block", '<rect width="16" height="16" fill="#f8c93c"/><path d="M0 5H16M5 0V16M11 5V16" stroke="#d79b1f" stroke-width="1"/><path d="M2 2H8" stroke="#fff5a8" stroke-width="2"/>'))
    return f"<defs>{''.join(bodies)}</defs>"


def structure_report(structure: Structure) -> str:
    total = len(structure.blocks)
    counts: dict[str, int] = {}
    for block in structure.blocks.values():
        counts[block] = counts.get(block, 0) + 1
    door_count = sum(count for block, count in counts.items() if block in DOORS) // 2
    glass_count = sum(count for block, count in counts.items() if block in GLASS)
    light_count = counts.get("glowstone", 0) + counts.get("sea_lantern", 0) + counts.get("lantern", 0)
    sign_count = counts.get("oak_sign", 0)
    bar_count = counts.get("iron_bars", 0)
    min_x, max_x, min_y, max_y, min_z, max_z = bounds([structure])
    walk_y1 = sum(1 for x in range(min_x, max_x + 1) for z in range(min_z, max_z + 1) if is_passable(structure, x, 1, z) and has_floor(structure, x, 1, z))
    walk_y2 = sum(1 for x in range(min_x, max_x + 1) for z in range(min_z, max_z + 1) if is_passable(structure, x, 2, z) and has_floor(structure, x, 2, z))
    details = [
        ("blocks", total),
        ("doors", door_count),
        ("glass/window blocks", glass_count),
        ("jail-bar blocks", bar_count),
        ("lights", light_count),
        ("signs", sign_count),
        ("open exterior gaps", exterior_gap_count(structure)),
        ("walkable y=1", walk_y1),
        ("walkable y=2", walk_y2),
    ]
    return "".join(f"<li><b>{escape(name)}:</b> {value}</li>" for name, value in details)


def exterior_gap_count(structure: Structure) -> int:
    min_x, max_x, min_y, max_y, min_z, max_z = structural_bounds(structure)
    count = 0
    for y in range(min_y + 1, max_y):
        for x in range(min_x, max_x + 1):
            for z in (min_z, max_z):
                if structure.blocks.get((x, y, z), "air") == "air":
                    count += 1
        for z in range(min_z + 1, max_z):
            for x in (min_x, max_x):
                if structure.blocks.get((x, y, z), "air") == "air":
                    count += 1
    return count


def structural_bounds(structure: Structure) -> tuple[int, int, int, int, int, int]:
    ignored = {"oak_sign", "button", "stone_pressure_plate", "oak_pressure_plate"}
    coords = []
    for coord, block in structure.blocks.items():
        x, y, z = coord
        if y <= 0 or block in ignored:
            continue
        above = structure.blocks.get((x, y + 1, z), "air")
        if y == 1 and (above == "air" or above in ignored):
            continue
        coords.append(coord)
    return (
        min(x for x, _, _ in coords),
        max(x for x, _, _ in coords),
        min(y for _, y, _ in coords),
        max(y for _, y, _ in coords),
        min(z for _, _, z in coords),
        max(z for _, _, z in coords),
    )


def render_layer(structure: Structure, y: int) -> str:
    min_x, max_x, _, _, min_z, max_z = bounds([structure])
    rows = []
    for z in range(min_z, max_z + 1):
        cells = []
        for x in range(min_x, max_x + 1):
            block = structure.blocks.get((x, y, z), "air")
            color, symbol = COLORS.get(block, ("#ddd", "?"))
            title = escape(f"x={x}, y={y}, z={z}: {block}")
            cells.append(f'<td title="{title}" style="background:{color}">{escape(symbol)}</td>')
        rows.append(f"<tr>{''.join(cells)}</tr>")
    return f'<table class="grid"><tbody>{"".join(rows)}</tbody></table>'


def render_html(structures: list[Structure]) -> str:
    legend = "".join(
        f'<span><b style="background:{color}">{escape(symbol)}</b>{escape(name)}</span>'
        for name, (color, symbol) in sorted(COLORS.items())
        if name != "air"
    )
    texture_rows = "".join(
        f"<tr><td>{escape(block)}</td><td>{escape(note)}</td></tr>"
        for block, note in sorted(TEXTURE_NOTES.items())
    )
    sections = []
    for structure in structures:
        _, _, min_y, max_y, _, _ = bounds([structure])
        layers = []
        for y in range(max_y, min_y - 1, -1):
            layers.append(f"<section><h3>Y={y}</h3>{render_layer(structure, y)}</section>")
        sections.append(
            f"""<article>
<h2>{escape(structure.name)}</h2>
<p>Front is the top row. Hover cells or blocks for x/y/z/block. Walk maps mark open two-block headroom over a floor.</p>
<div class="summary"><ul>{structure_report(structure)}</ul></div>
<div class="iso-wrap">{render_isometric(structure)}</div>
<details open><summary>Walkability overlays</summary>
<section><h3>Feet Y=1</h3>{render_walk_layer(structure, 1)}</section>
<section><h3>Feet Y=2</h3>{render_walk_layer(structure, 2)}</section>
</details>
<details><summary>Block layers</summary>{"".join(layers)}</details>
</article>"""
        )
    return f"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Cops and Robbers Structure Preview</title>
<style>
body {{ font-family: system-ui, sans-serif; margin: 24px; background: #eef1f4; color: #1f2428; }}
article {{ margin: 0 0 36px; padding: 18px; background: white; border: 1px solid #d8dee4; border-radius: 8px; box-shadow: 0 10px 30px rgba(31, 35, 40, 0.08); }}
h1, h2, h3, p {{ margin: 0 0 12px; }}
section {{ display: inline-block; vertical-align: top; margin: 0 18px 18px 0; }}
.grid {{ border-collapse: collapse; table-layout: fixed; box-shadow: 0 0 0 1px #8c959f; }}
.grid td {{ width: 20px; height: 20px; border: 1px solid #d0d7de; text-align: center; font: 11px ui-monospace, SFMono-Regular, Menlo, monospace; }}
.walk-grid td.walk {{ background: #b7f5c4; }}
.walk-grid td.void {{ background: #f5e9b7; }}
.walk-grid td.blocked {{ background: #d0d7de; }}
.walk-grid td.door {{ background: #f7c267; }}
.legend {{ display: flex; flex-wrap: wrap; gap: 8px 14px; margin: 14px 0 24px; }}
.legend span {{ white-space: nowrap; font-size: 13px; }}
.legend b {{ display: inline-block; width: 18px; height: 18px; margin-right: 5px; border: 1px solid #8c959f; text-align: center; line-height: 18px; }}
.summary ul {{ display: flex; flex-wrap: wrap; gap: 8px 18px; padding: 0; margin: 0 0 14px; list-style: none; font-size: 13px; }}
.iso-wrap {{ overflow: auto; padding: 12px; margin: 10px 0 18px; background: #f6f8fa; border: 1px solid #d8dee4; border-radius: 6px; }}
.iso {{ min-width: 720px; max-width: 100%; height: auto; display: block; }}
details {{ margin-top: 14px; }}
summary {{ cursor: pointer; font-weight: 700; margin-bottom: 12px; }}
.texture-plan {{ margin: 0 0 28px; padding: 16px; background: #fff; border: 1px solid #d8dee4; border-radius: 8px; }}
.texture-plan table {{ border-collapse: collapse; width: 100%; font-size: 13px; }}
.texture-plan td {{ border-top: 1px solid #d8dee4; padding: 7px 8px; }}
.entity-grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: 14px; margin: 0 0 28px; }}
.entity-card {{ background: white; border: 1px solid #d8dee4; border-radius: 8px; padding: 14px; }}
.entity-card svg {{ width: 100%; height: 160px; display: block; background: #f6f8fa; border: 1px solid #d8dee4; border-radius: 6px; }}
.entity-card h3 {{ margin-top: 10px; }}
.entity-card p {{ font-size: 13px; color: #57606a; }}
</style>
</head>
<body>
<h1>Cops and Robbers Structure Preview</h1>
<p>Use this before live testing to catch offset doors, unsealed cells, missing windows, cramped navigation paths, and raised paths.</p>
<div class="legend">{legend}</div>
<div class="texture-plan">
<h2>Texture Roadmap</h2>
<p>The current preview uses palette blocks. These notes identify where a later texture pass should add detail without changing gameplay collision.</p>
<table><tbody>{texture_rows}</tbody></table>
</div>
{render_entity_gallery()}
{''.join(sections)}
</body>
</html>
"""


def render_entity_gallery() -> str:
    cards = [
        ("Police Cruiser", vehicle_svg("#f5f7fa", "#1f6feb", False), "Red/blue lightbar, push bar, mirrors, side badge, open cabin feel."),
        ("Fire Truck", vehicle_svg("#c7332f", "#ffd84d", True), "Larger frame with ladder and roof water cannon, separate silhouette from cruiser."),
        ("Bank Robber", npc_svg("#1f2328", "#f6f7f8", "#c9a27a", "#f8c93c"), "Masked robber with face, striped shirt, and visible stolen gold."),
        ("Cop", npc_svg("#1f5fbf", "#ffffff", "#c9a27a", None, badge=True), "Blue uniform, front badge placement, readable face."),
        ("Firefighter", npc_svg("#c7332f", "#ffd84d", "#c9a27a", None, helmet=True), "Red turnout gear, yellow helmet, readable face."),
        ("Teller", npc_svg("#7a4fb3", "#f4d35e", "#c9a27a", None), "Friendly bank teller colors, readable face behind counters."),
    ]
    body = "".join(
        f'<div class="entity-card">{svg}<h3>{escape(name)}</h3><p>{escape(note)}</p></div>'
        for name, svg, note in cards
    )
    return f'<section class="entity-grid">{body}</section>'


def vehicle_svg(body: str, accent: str, fire_truck: bool) -> str:
    ladder = ""
    cannon = ""
    push_bar = '<rect x="31" y="95" width="8" height="38" rx="2" fill="#30363d"/>' if not fire_truck else ""
    if fire_truck:
        ladder = '<rect x="74" y="28" width="92" height="9" rx="2" fill="#d8dee4"/><path d="M82 28V37M100 28V37M118 28V37M136 28V37M154 28V37" stroke="#6e7781" stroke-width="2"/>'
        cannon = '<rect x="48" y="48" width="54" height="10" rx="5" fill="#8c959f"/><circle cx="48" cy="53" r="7" fill="#6e7781"/>'
    return f"""<svg viewBox="0 0 220 160" role="img">
<rect x="42" y="72" width="132" height="44" rx="5" fill="{body}" stroke="#24292f" stroke-width="3"/>
<path d="M66 72L96 42H145L166 72Z" fill="{body}" stroke="#24292f" stroke-width="3"/>
<path d="M92 48H138L153 70H78Z" fill="#9be7ff" opacity=".72" stroke="#24292f" stroke-width="2"/>
<rect x="82" y="82" width="34" height="22" fill="{accent}" opacity=".9"/>
<rect x="122" y="82" width="34" height="22" fill="#ffffff" opacity=".92"/>
<rect x="93" y="35" width="18" height="8" fill="#cf222e"/><rect x="111" y="35" width="18" height="8" fill="#0969da"/>
{ladder}{cannon}{push_bar}
<circle cx="70" cy="119" r="18" fill="#24292f"/><circle cx="70" cy="119" r="8" fill="#8c959f"/>
<circle cx="150" cy="119" r="18" fill="#24292f"/><circle cx="150" cy="119" r="8" fill="#8c959f"/>
</svg>"""


def npc_svg(primary: str, accent: str, skin: str, held: str | None, badge: bool = False, helmet: bool = False) -> str:
    held_svg = f'<rect x="136" y="92" width="28" height="13" rx="2" fill="{held}" stroke="#8a6a18" stroke-width="2"/>' if held else ""
    badge_svg = '<polygon points="105,85 112,90 109,100 101,100 98,90" fill="#f8d847" stroke="#6e5700" stroke-width="1.5"/>' if badge else ""
    helmet_svg = '<path d="M71 42Q110 22 149 42V56H71Z" fill="#ffd84d" stroke="#24292f" stroke-width="3"/>' if helmet else ""
    return f"""<svg viewBox="0 0 220 160" role="img">
<rect x="82" y="64" width="56" height="58" rx="8" fill="{primary}" stroke="#24292f" stroke-width="3"/>
<rect x="66" y="70" width="18" height="54" rx="8" fill="{primary}" stroke="#24292f" stroke-width="3"/>
<rect x="136" y="70" width="18" height="54" rx="8" fill="{primary}" stroke="#24292f" stroke-width="3"/>
<rect x="88" y="120" width="18" height="34" rx="6" fill="#2f363d"/><rect x="114" y="120" width="18" height="34" rx="6" fill="#2f363d"/>
<rect x="78" y="38" width="64" height="44" rx="13" fill="{skin}" stroke="#24292f" stroke-width="3"/>
{helmet_svg}
<rect x="82" y="42" width="56" height="14" fill="{primary}" opacity=".95"/>
<circle cx="96" cy="63" r="4" fill="#1f2328"/><circle cx="124" cy="63" r="4" fill="#1f2328"/>
<path d="M99 74Q110 80 121 74" stroke="#6b3f26" stroke-width="3" fill="none"/>
<path d="M84 88H136" stroke="{accent}" stroke-width="6"/><path d="M84 106H136" stroke="{accent}" stroke-width="6"/>
{badge_svg}{held_svg}
</svg>"""


def main() -> None:
    output = Path("build/structure-preview/cops-and-robbers-structures.html")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(render_html([police_station(), bank(), fire_station(), hideout()]), encoding="utf-8")
    print(output)


if __name__ == "__main__":
    main()
