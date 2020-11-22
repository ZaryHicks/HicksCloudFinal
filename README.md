# HicksCloudFinal

Unfortunately, I was unable to get the Docker to GCP Communication aspect of the project completed in time.
However, both the Docker UI Application and the Hadoop GCP Algorithms are fully completed.

This document will show how to set up and run both sides of this application (although separate) on a new machine.
Further down, you will see three different videos. The first will be a code walkthrough of both the UI application and
the Hadoop Application. The second and third will be demos of both applications running in their complete (but separate) states.

*** Steps to Get this Running

First we will discuss what is needed to setup the Docker Application, and then we will discuss the Hadoop Application.

Since I am working on Windows, this tutorial will be for how to run both applications on Windows.
Note: Because I was unable to connect the two applications, there are no Authentication variables required.

To begin, clone this repository to your desired location on your local machine.

** Docker UI Application

Assumptions:

You have Docker installed

You have Xming installed (for the UI)

You are on Windows


At this point you should launch Docker Desktop / the Linux environment that Docker will run in.

With the repo cloned to your computer, navigate via PowerShell to HicksCloudFinal/DockerApp

Inside this folder you should see finalui.java and the Dockerfile for this project. Finalui.java contains the code for the
UI application that we will run.

On PowerShell, type the following Docker command to build the Image:

	docker build -t hicksfinal .

This command tells Docker to build the image from this Dockerfile, and name it "hicksfinal". Do not forget the "." at the end.
Note: you may receive some warnings when it compiles, but this is alright. We are using an older version of Java for this image.

Now we must set the Display variable in order to have Xming display our UI for us.

1. On PowerShell, type "ipconfig /all" to bring up your network information
2. Scroll down until you find the IPv4 Address for your machine, remember this address
3. On PowerShell, set the DISPLAY environment variable with the following command:

	set-variable -name DISPLAY -value IP_ADDRESS:0.0

Note: You should replace IP_ADDRESS with the address that you found in step 2. Be sure to include the :0.0

5. Launch Xming however you please. You should see it in the bottom right expanded toolbar.

With Xming launched, and DISPLAY set to your Ip Address, we are ready to run our UI.

On PowerShell, type the following command to run the Docker container that we built that contains this application:

	docker run -it -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix hicksfinal

If you followed the steps above correctly, the UI application should launch within Xming.

** GCP MapReduce Hadoop Application

Assumptions:

You have a GCP DataProc Cluster created and running

All provided files for this project are pre-uploaded to the cluster (we will discuss where shortly)

IF you want to compile the Jar from the source, you must have Eclipse installed

The Hadoop Jars used to build this application are:
	hadoop-mapreduce-client-core-2.6.0-cdh5.15.1.jar, 
	hadoop-common-2.6.0-cdh5.15.1.jar, 
	commons-logging-1.2.jar, 
	commons-cli-1.2.jar

To see the application files for this part of the project, cd to HicksCloudFinal/GCPApp

Inside this folder you should see HicksFinal.jar and HicksFinal.java. This java file contains the source code  for the application.
The JAR file is a compiled jar of this source code that include the necessary Hadoop Libraries. I will quickly explain how to build this
application for yourself in Eclipse.

In Eclipse, make a new project
In Project Properties -> Java Compiler, ensure "Use Compliance from execution environment JavaSE-1.8" is checked
In Project Properties -> Java Build Path, select Libraries. Click "Add External Jars". Select the 4 jars mentioned above. Hit apply.
Add HicksFinal.java into the project under src.
Right click on the project, click Export. Select "Runnable Jar File". Ensure that the launch configuration is set for the correct project.
	Note: if you do not see this new project under configuration, run the project first. It will crash but this is alright. 
Once the you have the configuration set, click Finish to compile the Jar.

This compiled Jar should be identical to the one provided - feel free to compile your own or use the provided Jar.

I will now discuss the formatting of HDFS that this application expects - namely, where to put the input files provided for this project

On GCP, navigate to your Hadoop Cluster. Click on the SSH button next to the master node to launch an SSH session into the cluster via your browser.

Everything pertaining to this application will be put in /user/hdfs/.

HicksFinal.jar / input files folder / output folders


Create a directory called "files" in /user/hdfs:

	hadoop fs -mkdir /user/hdfs/files

Within files, create 3 folders, one for each tar of input files provided

	hadoop fs -mkdir /user/hdfs/files/Hugo
	hadoop fs -mkdir /user/hdfs/files/Tolstoy
	hadoop fs -mkdir /user/hdfs/files/Shakespeare

Note that these folder names are capitalized.
Inside these folders, we will put the files from their respective tars.

Note: Hugo and Tolstoy will contain 2 files each. Shakespeare will contain another set of directories reflective of how it is laid out in the tar files.

These internal folders are: comedies, histories, poetry, tragedies. (The README and gloassary are excluded)
They should be lowercase as shown here and as provided in the tar. Inside each of these folders will be the corresponsing works

Here is the file structure for the input files (in files) as expected by the application:

/user/hdfs/

	/files/

		/Hugo/

			works

		/Tolstoy/

			works

		/Shakespeare/

			/comedies/

				works

			/histories/

				works

			/poetry/

				works

			/tragedies/

				works


With the input files correctly placed in hdfs, we can now run the application
Click the Gear in the top right, select "Upload File" and upload HicksFinal.jar

Follow these commands to make the Jar executable, and place it into hdfs:

	chmod +x HicksFinal.jar
	hadoop fs -put HicksFinal.jar /user/hdfs/
	
HicksFinal.jar will be placed in /user/hdfs/ for the sake of having all of the files in the same general location.

I will now explain how to run the 3 different operations for this application. The general format for running a job with this application is as follows:

	hadoop jar HicksFinal.jar HicksFinal -type [value]

-type has 3 options:

	-i Construct Inverted Indices

	-t Term Search

	-n Top-N


Value corresponds to the type of operation we are performing:
	
For -i, provide any combination of H T or S as a single command. This defines which of the 3 tar files we want to include in our input for the Inverted Indices.
For example: -i HS constructs Inverted Indices for the files from Hugo and Shakespeare, where just T will construct them only for Tolstoy.
Provide HTS in order to construct the indices for all provided input files
	
For -t provide any single string term/word that you want to search the Inverted Indices for
	
For -n provide the N value that you want to perform a Top-N word count search for from the Indices.

Since the -t and -n options RELY on the Inverted Indices being constructed, they will not work unless -i has been run once

Each operation creates an output folder for its results:

-i creates /user/hdfs/invertedind, which contains the Reducer outputs of the Inverted Indices

-t creates /user/hdfs/term/TERM. The subfolder /term will create a subfolder for the each TERM searched for, and contain the results there

-n creates /user/hdfs/topN/N. The subfolder /topN will create a subfolder for each N value that we call Top-N for, and contain the results there.

	
In order to to see the results from these operations, we will use the cat command to print all of the output files for a given job:

	hadoop fs -cat /user/hdfs/invertedind/*
	hadoop fs -cat /user/hdfs/term/TERM/*
	hadoop fs -cat /user/hdfs/topN/N/*

I will now provide a textual example for this.

Having put HicksFinal.jar into hdfs and set up the input files as specified we now run the following:

	hadoop jar HicksFinal.jar HicksFinal -i HTS
	
This will construct Inverted Indices for all 3 tar inputs files and output them to /user/hdfs/invertedind/. Calling:

	hadoop fs -cat /user/hdfs/invertedind/*
	
Will print out the Inverted Indices that we constructed.

Now we can search for a term. Enter the following:

	hadoop jar HicksFinal.jar HicksFinal -t king
	
This will search the Inverted Indices for the term "king" and output all postings for this term to a single file.
This file will be located at /user/hdfs/term/king/ and can printed with:

	hadoop fs -cat /user/hdfs/term/king/*

We can see the compiled and sorted (by docId) postings for this term from all of the files used to construct the Index

Now we can search for Top-N wordcount from these files. Enter the following:

	hadoop jar HicksFinal.jar HicksFinal -n 10

This will search the Inverted Indices and compile the top 10 most used words from all files used to construct the Index
This file will be located at /user/hdfs/topN/10/ and can be printed with:
	
	hadoop fs -cat /user/hdfs/topN/10/*

This will print out the results of our top 10 search.

This concludes the tutorial on how to run both of the applications created for this project. Unfortunately, they cannot
communicate and thus are run separate at this time.

*** Video Demonstrations

This video is a walkthrough of the code for both sides of this project:

https://pitt-my.sharepoint.com/:v:/g/personal/zsh5_pitt_edu/EXILh5ZEsfZOrmSx7frjaoQBEnvNCR934bK2Ofb13mbgMw?e=MhMcbU

NOTE: Issues with the screen recording software prevented the UI application from being shown while I talk about it. This does not take away from the code walkthrough. To see the UI application function, watch the UI application demo video below.

This video is a Demonstration of the UI Application:

https://pitt-my.sharepoint.com/:v:/g/personal/zsh5_pitt_edu/EYT2itDdsnZGh3Mp3GQ1_OIBFWUxBejuZISOzIf9rLlWuA?e=h4y9UR

This video is a Demonstration of the GCP Hadoop Application:

https://pitt-my.sharepoint.com/:v:/g/personal/zsh5_pitt_edu/EYqV_SGNYXxBvc0Y9USsXXIBfBnGoijqnpLZQ_b0Hpo6Fw?e=JWFTmn
