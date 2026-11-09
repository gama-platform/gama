# GAMA Headless

## Getting started

You can start GAMA in headless mode using the custom script file. 

Several options are available, you can run the help to command to have a better view of what is available as follow : 

```bash
.\gama-headless.bat -help
```

### GUI experiment

You can run a GUI experimenta as follow : 

```bash
.\gama-headless.bat [options] [xmlInputFile] [outputDirectory]
```

You can try for example this sample file : 

```bash 
.\gama-headless.bat ./samples/roadTraffic.xml ./output-folder
```

### Batch experiment

You can also run a batch experimenta as follow : 

```bash
.\gama-headless.bat [options] -batch [experimentName] [modelFile.gaml]
```

You can try for example this sample file : 

```bash 
.\gama-headless.bat -batch Optimization ./samples/predatorPrey/predatorPrey.gaml
```

### Playable models (.gamx) and project archives (.gamp)

Generate an archive from a project directory, or from a model file inside a project.
The format follows the extension of the output file:

```bash
.\gama-headless.bat -package ./MyProject ./MyProject.gamx -default-experiment main
.\gama-headless.bat -package ./MyProject ./MyProject.gamp -include-plugins
```

`-plugin [file.jar]` (repeatable) installs a plugin for the command only, and
declares or embeds it in the archive. Other options: `-experiment`, `-title`,
`-description`, `-image`, `-id`, `-version`, `-project`, `-show-home` and
`-allow-missing-data` for `.gamx`; `-resolve-links` and `-include-plugins` for `.gamp`.

Check an archive (layout, manifest, plugins, compatibility with this GAMA, models):

```bash
.\gama-headless.bat -validate-package ./MyProject.gamx
```

Run one of its experiments, either until its simulation reaches cycle N (`-until-cycle N`)
or, without it, until its `until:` condition is true (or forever: stop it with Ctrl+C):

```bash
.\gama-headless.bat -run-package ./MyProject.gamx -experiment main -until-cycle 100 -output ./results
.\gama-headless.bat -run-package ./MyProject.gamp -model MyProject/models/main.gaml -experiment main
```

The plugins embedded in an archive are installed for the run only, never in GAMA.
`-output` keeps the extracted archive (including the files saved by the model, under
`package/workspace/`) and the simulation outputs; without it they are deleted at the end.
The commands exit with 0 on success, 1 if the archive is invalid or the run fails, and
2 if the command line is invalid.
