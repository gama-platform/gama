package gama.ui.devtools.dependencies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.io.FileReader;
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


public class DependencyGraphBuilder {

    private static String servicePathStr = Path.of("META-INF/services").toString();

    /**
     * Location of the native java dependency analyzer executable
     */
    private static String jdepsLocation = Path.of(System.getProperty("java.home"),"bin","jdeps").toString();

    /**
     * Map linking
     */
    private Map<String,Path> librariesNameToPath = null;

    /**
     * libraries
     */
    private Set<Path> libraries = null;

    /**
     * (DirectedPseudograph	directed self-loops multiple-edges)
     * X -> Y means X imports Y
     */
    private Graph<String,DefaultEdge> dependenciesGraph = null;

    /**
     * (DirectedPseudograph	directed self-loops multiple-edges)
     * X -> Y means X is imported by Y
     */
    private EdgeReversedGraph<String,DefaultEdge> importsGraph = null;

     /**
     * (DirectedPseudograph	directed self-loops multiple-edges)
     * X -> Y means X is consuming  a service provided by Y
     * 
     * successors of X are its providers
     */   
    private Graph<String,DefaultEdge> serviceConsumingGraph = null;

     /**
     * (DirectedPseudograph	directed self-loops multiple-edges)
     * X -> Y means X is providing a service consumed by Y
     * 
     * successors of X are its consumers
     */   
    private Graph<String,DefaultEdge> serviceProvidingGraph = null;

    /**
     * Set of all the known services
     */
    private Set<String> services;

    /**
     * map linking each service to the set of provider libraries
     */
    private Map<String,Set<String>> serviceToLibraryMap;

    /**
     * map linking each library to the set of services it provides
     */
    private Map<String,Set<String>> libraryToServicesMap;

    public DependencyGraphBuilder() {
        libraries = new HashSet<Path>();
        services = new HashSet<String>();
        serviceToLibraryMap = new HashMap<String,Set<String>>();
        libraryToServicesMap = new HashMap<String,Set<String>>();
    }

    /**
     * Generate the dependency Graph
     * (DirectedPseudograph	directed self-loops multiple-edges)
     * 
     * @param verbose
     *              if true, prints the dependencies relationships in the standard output;
     * 
     * @return boolean success value of the operation
     */
    public void generate(boolean verbose) {

        dependenciesGraph = new DirectedPseudograph<>(DefaultEdge.class);

        List<String> arguments = libraries.stream()
            .map(p -> p.toString())
            .collect(Collectors.toCollection(LinkedList::new));

        librariesNameToPath.keySet().forEach(filename -> dependenciesGraph.addVertex(filename));

        arguments.addFirst("--recursive");
        arguments.addFirst("base");
        arguments.addFirst("--multi-release");
        arguments.addFirst(jdepsLocation);

        ProcessBuilder pb = new ProcessBuilder(arguments.toArray(new String[0]));

        try {

            Process process = pb.start();

            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.charAt(0) == ' ')
                        continue;
                    
                    Path dependencyPath = Path.of(line.replaceAll(".*->\s*",""));
                    
                    if (! Files.exists(dependencyPath))
                        continue;

                    String importer = line.replaceAll("\s*->.*","");

                    dependenciesGraph.addEdge(importer,dependencyPath.getFileName().toString());

                    if(verbose)
                        System.out.println(line);
                }
            }

            importsGraph = new EdgeReversedGraph<String,DefaultEdge>(dependenciesGraph);

            process.waitFor();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to execute jdeps on " + libraries.toString(), e);
        }
    }

    /**
     * Generate the dependency Graph
     * (DirectedPseudograph	directed self-loops multiple-edges)
     */
    public void generate() {
        generate(false);
    }

    /**
     * Resolve the libraries to be inside a directory.
     * The resolved libraries are added to the 
     * already found libraries.
     * 
     * @param path
     *           Path of the target directory
     * 
     * @return boolean success value of the operation
     */
    public boolean resolveLibrariesInDirectory(Path path)
    {
        path = path.toAbsolutePath();

        if(! Files.isDirectory(path)) {
            System.err.println("error : " + path + " is not a directory");
            return false;
        }

        try (Stream<Path> stream = Files.walk(path)) {

            final Set<Path> foundLibraries =  stream
                    .filter(p -> p.toString().endsWith(".jar")
                                || Files.exists(p.resolve("META-INF/MANIFEST.MF")))
                    .collect(Collectors.toSet());
    
            if(librariesNameToPath == null)
                librariesNameToPath = new HashMap<String,Path>();

            this.libraries.addAll(foundLibraries);
            processLibrariesNames();

            return true;

        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        
    }

    /**
     * Resolve the libraries to be inside a directory.
     * The resolved libraries are added to the 
     * already found libraries.
     * 
     * @param pathStr
     *              String representation of the
     *              path of the target directory
     *              (Will be casted to Path)
     * 
     * @return boolean success value of the operation
     */
    public boolean resolveLibrariesInDirectory(String pathStr) {
        try {
            return resolveLibrariesInDirectory(Path.of(pathStr));
        } catch (InvalidPathException e) 
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Manually set the target libraries to be analyzed
     * 
     * @param libraries
     *                Set<Path> of libraries
     */
    public void setLibraries(Set<Path> libraries) {
        this.libraries = new HashSet<Path>(libraries);
        processLibrariesNames();
    }

    /**
     * Manually add a library to be analyzed
     * 
     * @param library
     *                Path of the library to add
     * 
     * @return true if library was not already present else false
     */
    public boolean addLibrary(Path library)
    {
        return libraries.add(library);
    }

    /**
     * Manually add a libraries to be analyzed
     * 
     * @param libraries
     *                Set<Path> of libraries to add
     * 
     * @return true if library was not already present else false
     */
    public boolean addLibraries(Set<Path> libraries)
    {
        return libraries.addAll(libraries);
    }

    /**
     * Associate every library's name to its Path
    */    
    public void processLibrariesNames(){
        libraries.forEach(p -> librariesNameToPath.put(p.getFileName().toString(),p));
    }

    /**
     * Export the graph in DOT format
     * 
     * @param outputPath
     *                 the path of the desired output file
     * 
     * @return success boolean value
    */    
    public boolean exportGraph(Path outputPath)
    {
        try {
            DOTExporter<String, DefaultEdge> exporter =
                new DOTExporter<>();

            exporter.setVertexAttributeProvider((v) -> {
                Map<String, Attribute> map = new LinkedHashMap<>();
                map.put("label", DefaultAttribute.createAttribute(v.toString()));
                return map;
            });
            
            Writer writer = new StringWriter();
            exporter.exportGraph(this.getDependenciesGraph(), writer);
            Files.writeString(outputPath,writer.toString());

            return true;

        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Export the graph in DOT format
     * 
     * @param outputPathStr
     *                    the String representing the path 
     *                    of the desired output file
     * 
     * @return success boolean value
    */  
    public boolean exportGraph(String outputPathStr) {
        try {
            return exportGraph(Path.of(outputPathStr));
        } catch (InvalidPathException e) 
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the dependency graph
     * Generate it if was not generated yet
     * 
     * @return the Dependency Graph
    */
    public Graph<String,DefaultEdge> getDependenciesGraph() {
        if (dependenciesGraph == null)
            generate();

        return dependenciesGraph;
    }

    /**
     * Get the imports graph
     * Generate it if was not generated yet
     * 
     * @return the Imports Graph
    */
    public Graph<String,DefaultEdge> getImportsGraph() {
        if (importsGraph == null)
            generate();

        return importsGraph;
    }

    /**
     * Get the Set of libraries
     * @return Set<Path>
    */
    public Set<Path> getLibraries() {
        return libraries;
    }

    /**
     * Get the Path of a library from its name
     * @return Path if library is found else null
    */
    public Path getLibraryPath(String name) {
        return librariesNameToPath.getOrDefault(name,null);
    }

    /**
     * get the set of libraries importing the target library (recursively)
     * @param name
     *           name of the target library
     * 
     * @return the list of libraries (names) importing the input library
    */
    public Set<String> getImporters(String name) {
        return getSuccessorsRecursive(importsGraph,name);
    }

    /**
     * get the set of libraries imported by the target library (recursively)
     * @param name
     *           name of the target library
     * 
     * @return the list of libraries (names) imported by the input library
    */
    public Set<String> getDependencies(String name) {
        return getSuccessorsRecursive(dependenciesGraph,name);
    }

    /**
     * return the list of all successors of a vertex of the target graph
     * recuresively (including successors of direct successors... etc)
     * 
     * @param graph
     *            the target graph
     * @param name
     *           the target vertex
     * 
     * @return the list of libraries (names) importing the input library
    */
    private static Set<String> getSuccessorsRecursive(Graph<String,DefaultEdge> graph, String name)
    {
        Set<String> successors = new HashSet<>();
        
        BreadthFirstIterator<String, DefaultEdge> iterator = new BreadthFirstIterator<>(graph, name);
        
        if (iterator.hasNext()) {
            iterator.next(); 
        }
        
        while (iterator.hasNext()) {
            successors.add(iterator.next());
        }
        
        return successors;        
    }


    public Set<String> getDirectDependencies(String library)
    {
        if (! getDependenciesGraph().vertexSet().contains(library))
            return null;

        return new HashSet<String>(Graphs.successorListOf(getDependenciesGraph(),library));
    }

    public Set<String> getDirectImporters(String library)
    {
        if (! getImportsGraph().vertexSet().contains(library))
            return null;

        return new HashSet<String>(Graphs.successorListOf(getImportsGraph(),library));
    }

    /**
     * Load a graph from a dot format graph file
     * @param dotFilePathStr
     *                     Swtring representing the path to the .dot graph
     * 
     * @return boolean success value
     */
    public boolean fromDot(String dotFilePathStr)
    {
        dependenciesGraph = new DirectedPseudograph<String,DefaultEdge>(DefaultEdge.class);

        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();
        
        Map<String, String> labelMap = new HashMap<>(); 

        importer.addVertexAttributeConsumer((pair, attribute) -> {
            String vertex = pair.getFirst();
            String attributeName = pair.getSecond();
            String attributeValue = attribute.getValue();

            // Track the specific human-readable 'label' if it exists
            if ("label".equals(attributeName)) {
                labelMap.put(vertex, attributeValue);
            }
        });

        importer.setVertexFactory(label -> label);

        try {
            // retrieving the labels (init the labelMap)
            FileReader reader = new FileReader(dotFilePathStr);
            importer.importGraph(new DirectedPseudograph<String,DefaultEdge>(DefaultEdge.class), reader);
            reader.close();

            // actually reading the graph
            reader = new FileReader(dotFilePathStr);
            importer.setVertexFactory(label -> labelMap.getOrDefault(label,"no label found"));
            importer.importGraph(dependenciesGraph, reader);
            reader.close();

            importsGraph = new EdgeReversedGraph<String,DefaultEdge>(dependenciesGraph);

            return true;

        } catch (IOException | ImportException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load a graph from a dot format graph file
     * @param dotFilePath
     *                     Path to the .dot graph
     * 
     * @return boolean success value
     */
    public boolean fromDot(Path dotFilePath)
    {
        return fromDot(dotFilePath.toString());
    }


    /**
     * build a service to provider libraries map,
     * and library to provided services map, by listing
     * the services declared in each library's 
     * 'META-INF/services' directory
     */
    public void listServices()
    {
        for(Path library : libraries)
        {
            final String filename = library.getFileName().toString();
            Set<String> thisLibraryServices;

            if(Files.isDirectory(library))
                continue;
            
            // retrive services declared in META-INF/services/*
            try (ZipFile zipFile = new ZipFile(library.toString())) {

                thisLibraryServices = zipFile.stream()
                    .map(ZipEntry::getName)
                    .filter(entry -> entry.startsWith(servicePathStr) && ! entry.endsWith("/"))
                    .map(entry -> entry.replaceAll(".*/","").replaceAll("\\.","/")) // a service is a/b/c/ClassName (relevent for bytecode research)
                    .collect(Collectors.toSet());

                // binding the current library as a provider of each found services
                thisLibraryServices.forEach(service -> {
                    serviceToLibraryMap.computeIfAbsent(service, 
                        key -> new HashSet<String>()
                    ).add(filename);

                    libraryToServicesMap.computeIfAbsent(filename, 
                        key -> new HashSet<String>()
                    ).add(service);
                });

                services.addAll(thisLibraryServices);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compute the consume/provide of services relations 
     * among known libraries and services.
     * 
     * A service declared in META-INF/services will
     * follow the exemple pattern: a.b.c.className
     * 
     * A service is considered potentially consumed by
     * a library if one of its .class files contains
     * the hard coded string a/b/c/className
     * 
     */
    public void checkServicesDependenciesRelations()
    {
        serviceConsumingGraph = new DirectedPseudograph<>(DefaultEdge.class); 
        serviceProvidingGraph = new DirectedPseudograph<>(DefaultEdge.class); 

        librariesNameToPath.keySet().forEach(filename -> {
            serviceConsumingGraph.addVertex(filename);
            serviceProvidingGraph.addVertex(filename);
        });

        for (Path library : libraries)
        {
            if(Files.isDirectory(library))
                continue;

            String filename = library.getFileName().toString();

            try {
                URI zipUri = URI.create("jar:" + library.toUri().toString());
                Map<String, String> env = new HashMap<>();

                ZipFile zipFile = new ZipFile(library.toString());

                // listing all class files entries
                Set<String> entries = zipFile.stream().map(ZipEntry::getName)
                    .filter(entry -> entry.endsWith(".class"))
                    .collect(Collectors.toSet());

                zipFile.close();

                FileSystem zipfs = FileSystems.newFileSystem(zipUri, env);

                for(String entry: entries)
                {
                    Path entryPath = zipfs.getPath(entry);
                    byte[] content = Files.readAllBytes(entryPath);

                    for (String service : services)
                    {
                        if (Bytes.indexOf(content,service.getBytes(StandardCharsets.UTF_8)) != -1)
                        {
                            serviceToLibraryMap.get(service).forEach(provider -> {
                                serviceConsumingGraph.addEdge(filename,provider);
                                serviceProvidingGraph.addEdge(provider,filename);
                            });
                        }
                    }
                }

                zipfs.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieve the set of all providers
     * of services the target library potentially
     * consumes (recursively)
     * 
     * @param library
     *              name of the target library 
     * 
     * @return Set<String> of providers of library
     */
    public Set<String> getLibraryProviders(String library)
    {
        if (serviceConsumingGraph == null) {
            listServices();
            checkServicesDependenciesRelations();
        }

        return getSuccessorsRecursive(serviceConsumingGraph,library);
    }

    /**
     * Retrieve the set of potential consumers
     * of services declared in the 'META-INF/services'
     * of the target library (recursive)
     * 
     * @param library
     *              name of the target library 
     * 
     * @return Set<String> of direct consumers of library
     */
    public Set<String> getLibraryConsumers(String library)
    {
        if (serviceProvidingGraph == null) {
            listServices();
            checkServicesDependenciesRelations();
        }

        return getSuccessorsRecursive(serviceProvidingGraph,library);
    }

    /**
     * Retrieve the set of direct providers
     * of services the target library potentially
     * consumes
     * 
     * @param library
     *              name of the target library 
     * 
     * @return Set<String> of direct providers of library
     */
    public Set<String> getLibraryDirectProviders(String library)
    {
        if (serviceConsumingGraph == null) {
            listServices();
            checkServicesDependenciesRelations();
        }

        return new HashSet<String>(Graphs.successorListOf(serviceConsumingGraph,library));
    }

    /**
     * Retrieve the set of potential direct consumers
     * of services declared in the 'META-INF/services'
     * of the target library
     * 
     * @param library
     *              name of the target library 
     * 
     * @return Set<String> of direct consumers of library
     */
    public Set<String> getLibraryDirectConsumers(String library)
    {
        if (serviceProvidingGraph == null) {
            listServices();
            checkServicesDependenciesRelations();
        }
        
        return new HashSet<String>(Graphs.successorListOf(serviceProvidingGraph,library));
    }


    /**
     * Retrieve the set of services declared 
     * in the 'META-INF/services'of the target 
     * library
     * 
     * @param library
     *              name of the target library 
     * 
     * @return Set<String> of services of the target library
     */
    public Set<String> getLibraryServices(String library)
    {
        return new HashSet<String>(libraryToServicesMap.getOrDefault(library,Collections.emptySet()));
    }
}