# Official repository of the GAMA modeling platform
[![Continuous project validation](https://github.com/gama-platform/new.gama/actions/workflows/trigger-compilation.yaml/badge.svg)](https://github.com/gama-platform/gama/actions/workflows/trigger-compilation.yaml)
[![Language](https://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)
[![GitHub issues](https://img.shields.io/github/issues/gama-platform/gama.svg)](https://github.com/gama-platform/gama/issues)
[![Github Releases](https://img.shields.io/github/release/gama-platform/gama.svg)](https://github.com/gama-platform/gama/releases)
[![Documentation](https://img.shields.io/badge/documentation-web-brightgreen.svg)](https://gama-platform.github.io)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=gama-platform_new.gama&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=gama-platform_new.gama)
[![CodeScene general](https://codescene.io/images/analyzed-by-codescene-badge.svg)](https://codescene.io/projects/51964)
[![CodeScene Average Code Health](https://codescene.io/projects/51964/status-badges/average-code-health)](https://codescene.io/projects/51964)


This is the official repository of the GAMA platform, an open-source modeling and simulation environment for creating spatially explicit agent-based simulations. 

**See the changelog on the [website](https://gama-platform.org/wiki/next/Changelog) for a detailed list of changes since version 1.9.0**  

## Installing GAMA

### Install a release
There's a detailed tutorial that covers all cases in the [documentation](https://gama-platform.org/wiki/Installation), but for most user you can just go straight to the [**releases**](https://github.com/gama-platform/gama/releases) section of this repository and download the version that corresponds to your needs, we recommend you take a version bundled with a JDK.

### Run it from the source code

To run it from the source code you can either use `maven` to build the program yourself, or run it from `eclipse` in case you want to inspect the code and/or modify it. In both cases you will first need to **clone this repository** and to get the [Temurin distribution of JDK21](https://adoptium.net).

#### Using eclipse

There is a more detailled explanation on the [website](https://gama-platform.org/wiki/InstallingGitVersion) about how to do so, here we will only give a fast and easy approach.

The _highly recommended configuration_ for working on this branch is [Eclipse for Java and DSL 2025-03](https://www.eclipse.org/downloads/packages/release/2025-03/r/eclipse-ide-java-and-dsl-developers), using a different version may expose you to some bugs.

Once you have imported the code base into your workspace, simply open the `gama.product` project and open either `gama.headless.product` or `gama.product` depending if you want to run gama with or without GUI.
In the `Overview` pane of the file, click on `Synchronize` and then `Launch an Eclipse application` and GAMA should start.

#### Using maven

First install [maven](https://maven.apache.org/) on your computer, then open a terminal at the root of project and follow those steps:
1. go to the project `gama.annotations`
2. run the command:
```bash
mvn clean install
```
3. go to the project `gama.processor`
4. run the same mvn command
5. go to the project `gama.parent`
6. run the same mvn command one last time

The produced release should be stored in the project `gama.product` under the `target` folder.

## Reporting problems, bugs and issues

If you spot a bug or want to suggest an improvement to GAMA, please do so by posting an issue here: [https://github.com/gama-platform/gama/issues](https://github.com/gama-platform/gama/issues). 

If you noticed something wrong/not up-to-date or a lack of information on something on the website and the documentation you could create an issue on the website's dedicated repository here: [https://github.com/gama-platform/gama-platform.github.io/issues](https://github.com/gama-platform/gama-platform.github.io/issues)

For general questions about modelling/development on GAMA you can either ask them in the [discussions](https://github.com/gama-platform/gama/discussions) of this repository or on the [mailing list](https://groups.google.com/g/gama-platform)

## Contributing

There are a few tutorials explaining how to create new functionalities in gama that you can find in the [developing extensions](https://gama-platform.org/wiki/DevelopingExtensions) section of the website, and one general explanation of the architecture of the software and important types used in the section [Introduction to GAMA Java API](https://gama-platform.org/wiki/Introduction-To-Gama-Java-API).

To contribute to the code you just have to submit a pull-request to this repository, if you encounter any problem to do so, feel free to ask about it either on the [github discussions](https://github.com/gama-platform/gama/discussions) or the [mailing list](https://groups.google.com/g/gama-platform).

 ## Acknowledgments

 ![YourKit logo](https://www.yourkit.com/images/yk_logo.svg) GAMA is actively supported by YourKit LLC, with its full-featured <a href="https://www.yourkit.com/java/profiler/index.jsp">Java Profiler</a>.


 
 

  
  
  
