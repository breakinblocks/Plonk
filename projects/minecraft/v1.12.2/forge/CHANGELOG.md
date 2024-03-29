# Changelog

## [5.0.5] - 2021-05-05

- Add config for unplaceable items
- Add safer tile entity getting for non-essential methods

## [5.0.4] - 2021-01-01

- Add command to dump item stack render type data
- Adjust render type heuristic: Signs, beds and shields now use item rendering
- Fix tile rotation not being read

## [5.0.3] - 2020-11-03

- Fix no last selection crash
- Add config for max stack size of each slot
- Fix lighting issues

## [5.0.2] - 2020-10-29

- Attempt to fix render crash when block does not have facing state

## [5.0.1] - 2020-10-21

- Fix black screen when camera is inside block

## [5.0.0] - 2020-10-19

- Cannot be washed away by fluids anymore
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
