New Models Backport (NMB)
Created by ProstoMaslo
Original idea by Bay4lly (https://github.com/Bay4lly/1.21.11-Java-Item-Model-Backport) Fabric 1.20.1

# Overview
New Models Backport is a utility library mod for Minecraft 1.21.1 (NeoForge). It allows map makers and resource pack creators to use modern JSON models (1.21.11+ format) in older versions like 1.21.1. It ports the handling of the new JSON formatting so it can be natively read, loaded, and rendered.

Features
- Modern Model Formatting: Load block and item model JSONs formatted for the newest Minecraft versions.
- Arbitrary Rotation Angles: Full support for arbitrary rotation angles generated via Blockbench (removes the 22.5 degree limits).
- Fullbright Support: Support for the "ignore_light": true property in elements. Sets them to render at maximum brightness, completely ignoring world lighting and shadows.
- Optimized Rendering: All 3D math and trigonometry are handled strictly during the Baking stage. Zero FPS impact during gameplay.

For Developers
This mod is a library dependency. Add it to your project to easily load custom models exported from the newest Blockbench versions. The mod automatically handles custom JSON serialization, model baking, arbitrary rotation, and ignore_light properties.

Requirements
- Minecraft: 1.21.1
- Mod Loader: NeoForge (>=21.1.200)

License
MIT
