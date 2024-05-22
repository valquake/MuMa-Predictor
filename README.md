# MuMa-Predictor
MuMa Predictor is a tool which computes predictors for reaction systems by using a methodology based on over-approximation.
# Reaction systems used for Benchmarks and Simulation 
Currently, we have used two sets of reaction system as reference:
1. **dummy.txt** - It is a dummy reaction system containing 20 instances and 33 different reactions. We used this to highlight the advantages obtained in the verification phase over the performance of a standard reaction system.
2. **mammalian.txt** - It is a reaction system based on a well-known model which examines a succession of molecular events that lead to the reproduction of a cell’s genome and its division through Cell Mitosis. Originally, it was a boolean network, which we converted into a reaction system using a Python-coded tool, in order to study attractors identified by MuMa. It counts 23 different reactions, which governs the activity of 10 instances.
# How to install
1. Download this repository and open it with your favorite java editor.
2. The libraries in the project are ready to use, but require an updated version of JDK to work properly. Set jdk to the latest version in your Java editor(I'll use **Eclipse Editor** as an example):
  - The guide described below is summarized in this video: https://www.youtube.com/watch?v=E9AqjOPM950
  - from here: https://www.oracle.com/it/java/technologies/downloads/ download the latest version of jdk,
  - Right-click on the project and go to properties. <br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme00.png" width="400" height="400"><br/>
  - On the left panel go to Java build path, click on JRE System Library and go to edit:<br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme01.png" width="600" height="400"><br/>
  - Click on "Installed JRes..." on the left.<br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme02.png" width="400" height="400"><br/>
  - Click on Add. <br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme03.png" width="400" height="400"><br/>
  - Select Standard VM and click to Next <br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme04.png" width="400" height="400"><br/>
  - Click on directory.  <br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme07.png" width="400" height="400"><br/>
  - Now go to the folder of the jdk version you downloaded, click on "select folder" and next click on "Finish", for example I am selecting version 22 in the image below. <br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme06.png" width="500" height="400"><br/>
  - Check the version of jdk you just added and click on "Apply and close". <br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme09.png" width="500" height="400"><br/>
  - Select from the drop-down menu the version of jdk you just added and click on "Finish". <br/>
<img src="https://github.com/valquake/MuMa-Predictor/blob/main/images/Readme08.png" width="500" height="400"><br/>
# How to use
For a quick guide on how to use the tool, see the appendix of the thesis.pdf file.



