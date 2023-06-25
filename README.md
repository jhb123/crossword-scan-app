# Crossword Scan
![image](./banner.png)

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![OpenCV](https://img.shields.io/badge/opencv-%23white.svg?style=for-the-badge&logo=opencv&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
--
Crossword Scan is an Android app that brings physical crosswords into the digital realm, enabling on-the-go solving and sharing with your friends. Under the hood, Crossword Scan is written in Kotlin, uses Jetpack Compose and leverages classical image processing algorithms alongside modern machine-learning approaches from Google's ML kit. A REST API was developed for sharing puzzles across devices, and the source code for that can be found [here](https://github.com/jhb123/PuzzleServer).
## Screen Demos
 Scanning grids |  Scanning clues | Interacting with the crossword 
 :--: | :--: | :--: 
|<img src="./readme_resources/grid_scanning.gif" height="400"/>|<img src="./readme_resources/scan_clues.gif" height="400"/> |<img src="https://i.imgur.com/S1sVNMM.gif" height="400" />


Dark mode |  Light mode | Authentication Scren
 :--: | :--: | :--: 
<img src="https://i.imgur.com/8jstZzY.png" height="400"/>|<img src="https://i.imgur.com/EmwWxVg.png" height="400"/>|<img src= "./readme_resources/auth_screen.png" height="400" />

## Image Processing Pipeline

The image processing pipeline used to detect the crossword grid was implemented with OpenCV. Below is an image of how this pipeline works.

![image](./readme_resources/crossword-pipeline.png)

This pipeline was originally developed in Python, and that can project can be found here: https://github.com/jhb123/crosswordScan

