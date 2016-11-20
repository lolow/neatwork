# NEATWORK 3.5

NeatWork is a free program specifically fashioned for the design of
entirely gravity-driven water distribution networks for rural areas.

This program is developed by the NGO APLV (Agua Para La Vida) and
Logilab (University of Geneva). Mosek Solver is graciously made
available for the optimization part of the software.

## LIABILITY WAIVER

Under no circumstances, including, but not limited to, negligence, 
shall APLV, Logilab and Mosek be liable for any loss of profits; any 
incidental, special, exemplary, or consequential damages that results
from the access or use of, or the inability to access or use, the
materials at the internet site.


## INSTALLATION INSTRUCTIONS

Unzip the zip file in a new directory and that's all !


## Table of contents

1. Files Requirements
2. Java Requirements 
3. Running the program 
4. Edit with a spreadsheet

### Files Requirements

neatwork.jar : NeatWork engine.

neatwork.bat : Launcher

*.dll	: Solver libraries
lib/*.dll      : Solver libraries

### Java Requirements

NeatWorks needs the Java Virtual Machine 1.5 or above.
This restriction may disappear in the future.
The most recent Sun JVM can be found at
http://java.sun.com/getjava/download.html


### Running the program

First : Try to double-click on the JAR file.

If it doesn't work : Use the laucher file neatwork.bat. This script
opens a DOS console and runs the command: java -jar NeatWork.jar


### Edit network data with a spreadsheet

In case the user wishes to introduce a new large network, we recommend  
to prepare the data on a Excel spreadsheet. Then the transfer of data from
the spreadsheet to NeatWork can be performed by simple cut and paste.
The required format of the spreadsheet can be obtained by exporting
the data of the provided sample network. 

3.27
- Fix 2 errors concerning user-defined roughness coefficients. The roughness coefficient was measured in millimiters, but not converted in meters in the calculations. The other error occured in reading a parameter value in a table.
3.26
- No more need for a source at height 0
- Fixed bug on pipe constraints in Design
3.25
- internalization (french, spanish)
3.24
- fixes minor bugs
- New user guide
3.23
- Corrections dans l'interface
3.22
- Changer Select en Browse... (DONE)
- Enlever le tooltip dans simulation (DONE)
- Type d'orifice commercial par d�faut.Garder en m�moire les param�tres de New Simulation (DONE)
