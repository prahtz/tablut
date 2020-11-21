# Tablut
An IA for the Tablut Challenge 2020

## Description
This player has been designed to play against another player communicating through a [server](https://github.com/AGalassi/TablutCompetition).

## Install on Ubuntu/Debian
From console, run these commands to install JDK 8:

'''
sudo apt update
sudo apt install openjdk-8-jdk -y
'''

## Run player
The following instructions will explain how to run the player.
* open a terminal and go to 'scripts' folder
* run './tablut <player> <timeout> <address>'

## Build jar
If you want to create an executable .jar file you must:
* open a terminal and go to 'scripts' folder
* run './buildjar'
You can find the generated file in the 'jar' folder

## Optimization using Genetic Algorithm
If you want to optimize the hyperparameters used for the evaluations of good moves of this player you must:
* open a terminal and go to 'scripts' folder
* run './genetic'
The results and all the populations will be written in 'out/populations.txt'.
