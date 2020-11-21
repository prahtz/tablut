# Tablut
An IA for the Tablut Challenge 2020

## Description
This player has been designed to play against another player communicating through a [server](https://github.com/AGalassi/TablutCompetition).

## Install on Ubuntu/Debian
From console, run these commands to install JDK 8:
```
sudo apt update
sudo apt install openjdk-8-jdk -y
```

## Run player
The following instructions will explain how to run the player.
* open a terminal and go to `scripts` folder
* run `./tablut <player> <timeout> <address>`

## Build jar
If you want to create an executable .jar file you must:
* open a terminal and go to `scripts` folder
* run `./buildjar`
You can find the generated file in the root folder

## Optimization using Genetic Algorithm
If you want to optimize the hyperparameters used for the evaluations of good moves of this player you must:
* open a terminal and go to `scripts` folder
* run `./genetic`

The results and all the populations will be written in `out/populations.txt`.

## Notes
* If you want to export the jar file you MUST also copy the `lib` folder and put it in the same directory of the executable.
* In some cases, in order to execute the scripts, you must first change the permission of the `scripts` folder, for example using `chmod u+x *`