package gama.export;

import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import gama.export.dependency.BundleDependencyAnalyzer;
import gama.export.GamaZipBuilder;

public class Main {
    
    public static void main(String argv[])
    {
        // System.out.println(System.getProperty("java.home"));
        Set<String> neededModules = Set.of(
            "gama.extension.bdi",
            "gama.extension.database",
            "gama.extension.fipa",
            "gama.extension.pedestrian",
            "gama.extension.physics",
            "gama.extension.traffic",
            "gama.library",
            "gama.ui.display.opengl",
            "gama.ui.display.opengl4",
            "gama.export"
        );

        // GamaZipBuilder ziper = new GamaZipBuilder(neededModules);
        GamaZipBuilder ziper = new GamaZipBuilder(Collections.<String>emptySet());
        try { 
            ziper.zip("/home/cytech/gamatest.zip");
        } catch (Exception e) {
            System.err.println("IOException raised while cloning GAMA :\n" + e);
        }


    }
}