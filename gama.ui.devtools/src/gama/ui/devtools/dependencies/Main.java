package gama.ui.devtools.dependencies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.net.URI;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.*;
import org.jgrapht.traverse.*;
import com.google.common.primitives.Bytes;

import gama.ui.devtools.dependencies.DependencyGraphBuilder;
import gama.ui.devtools.dependencies.GraphToHtml;


public class Main {

    public static void main(String argv[])
    {
        final DependencyGraphBuilder builder = new DependencyGraphBuilder();
        final String librariesPathStr = "..";
        Set<Path> libraries;
        
        System.out.println("Resolving libraries in " + librariesPathStr);
        builder.resolveLibrariesInDirectory(librariesPathStr);

        libraries = builder.getLibraries(); 

        System.out.println("Found " + libraries.size() + " librar" + (libraries.size() == 1 ? "y" : "ies"));
        System.out.println("Listing services");
        builder.listServices();

        System.out.println("Generating the static dependencies graph");
        builder.generate();

        System.out.println("Checking the providing/consuming services relations");
        builder.checkServicesDependenciesRelations();

        System.out.println("Generating HTML graph explorer");
        GraphToHtml.generateHtml(builder,"./graph2.html");

        System.out.println("Exporting the graph in .dot format");
        builder.exportGraph("./graph2.dot");
        System.out.println("done");
    }
}