"""Generate the PWA icons (needs Pillow: pip install pillow). Only run to regenerate icons."""

import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GREEN = (5, 150, 105, 255)  # emerald-600
WHITE = (255, 255, 255, 255)
SIZE = 1024  # draw large, then downscale for smooth edges


def dumbbell(draw, scale):
    """White dumbbell centred on the canvas; `scale` shrinks it for maskable safe zone."""
    c = SIZE / 2

    def box(x0, y0, x1, y1, r):
        draw.rounded_rectangle(
            [c + x0 * scale, c + y0 * scale, c + x1 * scale, c + y1 * scale],
            radius=r * scale, fill=WHITE,
        )

    box(-250, -40, 250, 40, 20)      # bar
    box(-330, -170, -250, 170, 30)   # inner plates
    box(250, -170, 330, 170, 30)
    box(-410, -110, -330, 110, 30)   # outer plates
    box(330, -110, 410, 110, 30)


def make(rounded, scale):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    if rounded:
        d.rounded_rectangle([0, 0, SIZE - 1, SIZE - 1], radius=SIZE // 5, fill=GREEN)
    else:
        d.rectangle([0, 0, SIZE, SIZE], fill=GREEN)
    dumbbell(d, scale)
    return img


def save(img, rel, size):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.resize((size, size), Image.LANCZOS).save(path)
    print("wrote", rel)


any_icon = make(rounded=True, scale=1.0)
full_bleed = make(rounded=False, scale=0.75)  # keeps artwork inside the maskable safe zone
save(any_icon, "public/icons/icon-192.png", 192)
save(any_icon, "public/icons/icon-512.png", 512)
save(full_bleed, "public/icons/maskable-512.png", 512)
save(any_icon, "app/icon.png", 512)             # browser tab icon
save(full_bleed, "app/apple-icon.png", 180)     # iOS home screen icon
