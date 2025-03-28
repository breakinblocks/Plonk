# Changelog

## [10.0.6] - 2025-03-25

- Ported to Minecraft 1.21.1 for NeoForge.

## [10.0.5] - 2024-02-10

- Ported to Minecraft 1.19.4 for Forge.
- Ported to Minecraft 1.20.1 for Forge.

## [10.0.4] - 2023-04-08

- Fix crash from using bad block-state.

## [10.0.3] - 2022-08-07

- Ported to Minecraft 1.19.2 for Forge.

## [10.0.2] - 2022-03-13

- Ported to Minecraft 1.18.2 for Forge.

## [10.0.1] - 2022-01-18

- Fixed placed items not saving properly.
- Fixed placed items causing chunks to not load on the clientside.

## [10.0.0] - 2022-01-18

- Ported to Minecraft 1.18.1 for Forge.

## [9.0.8] - 2021-05-05

- Add config for unplaceable items
- Add tag 'plonk:unplaceable' for unplaceable items
- Add safer tile entity getting for non-essential methods

## [9.0.7] - 2021-01-19

- Update to Minecraft 1.16.5 (supports 1.16.x)
- Fix #9: Dupe bug when block place is canceled (see MinecraftForge#7609)

## [9.0.6] - 2021-01-04

- Fix #8: Crash when Enderman ray-traces block

## [9.0.5] - 2021-01-02

- Fix removing last item setting to air instead of using fluid state

## [9.0.4] - 2021-01-01

- Add command to dump item stack render type data
- Adjust render type heuristic: Signs, beds and shields now use item rendering
- Fix tile rotation not being read
- Adjust rendering to no longer change lighting state

## [9.0.3] - 2020-11-22

- Updated to 1.16.4

## [9.0.2] - 2020-11-03

- Fix no last selection crash
- Add config for max stack size of each slot

## [9.0.1] - 2020-10-29

- Use passed in combinedLightIn for rendering
- Attempt to fix render crash when block does not have facing state
- Pretend to support 1.16.X

## [9.0.0] - 2020-10-19

- Updated to 1.16.3
- Cannot be washed away by fluids anymore
- Can be waterlogged
- Fix null selection box crash
- Added rotation to tile: Sneak + Place Key (empty hand) to rotate tile (4 increments)
- Added rotation to each item: Sneak + Right Click (empty hand) to rotate item (16 increments)

## [1.0.0] - 2020-01-08

- Updated to 1.12.2
- Placed items 'block' can be instantly broken

## [0.0.3] - 2019-12-07

- Modularized bounding boxes (should have no visible change)
- Made item insertion and inventory updating server-side only

## [0.0.2] - 2019-10-06

- Initial release of Plonk for Minecraft 1.7.10
- Place up to 4 items down
- Can be placed on 6 sides of a block (but up and down always face north)
- Can be washed away by water
- Items can be extracted but not inserted into the placed items
