# OOSEM Assistant

## Description

Modeling assistant to support the application of the recursive decomposition pattern of Object-Oriented Systems Engineering Method (OOSEM) in the SysML v2 language.

## Install instructions

1. Install the [SysML v2 Pilot Implementation](https://github.com/Systems-Modeling/SysML-v2-Pilot-Implementation/tree/master) according to the 'Manual installation' chapter of the README

2. Clone this repository
   
3. Import the 'hu.bme.mit.sysml.oosem' project into the Eclipse workspace where the SysML v2 Pilot Implementation is set up

4. Rebuild the workspace

5. Start the runtime Eclipse, with the SysML v2 Pilot Implementation

6. Import the 'oosem' project into the runtime workspace

7. (Optional) Import the 'quadcopter-oosem' and 'validation-oosem' projects for examples
   * 'quadcopter-oosem' contains a larger example with several files and subsystems
   * 'validation-oosem' contains an example for the validation rules

8. Rebuild the runtime workspace

## How to use

1. OOSEM Assistant can be accessed via the Eclipse menubar Window>Show view>Other in the pop-up window, choose Other>OOSEM Assistant, and click Open

2. At the plug-ins panel, select the model you want to visualize, then click Load

3. The dashboard shows the OOSEM blocks and their structures, organized according to the methodologies hierarchy

4. Colorful markings represent OOSEM block types
   * Purple circle (🟣): Specification block
   * Green square (🟩): Design block
   * Blue diamond (🔷): Integration block

5. Errors and warnings are also represented by markers (❌/⚠️)
   * Hover your mouse above the labels with errors or warnings to get detailed information

6. Right-click on labels to open the context menu
   * Open in editor: opens the block definitions placed in the model
   * Generate design/integration block: Opens a wizard to generate block frames for the next step

7. The tool automatically builds the model after block generation and refreshes the dashboard after builds

## Known issues

1. The colors of the markers do not render on Windows due to limitations between Eclipse and Windows. (The tool can still be used with monocrome markers.)

## Copyright

OOSEM Assistant

Copyright (c) 2026 [OOSEM Assistant Authors](#contributors)
  
This program is free software: you can redistribute it and/or modify
it under the terms of the Eclipse Public License as published by
 the Eclipse Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
Eclipse Public License for more details.

You should have received a copy of the Eclipse Public License
along with this program.  If not, see <https://www.eclipse.org/legal/epl-2.0/>.


## Contributors

 * Ádám Fiák
 * Vince Molnár
