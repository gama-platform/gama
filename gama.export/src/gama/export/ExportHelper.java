package gama.export;

import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import gama.workspace.manager.WorkspacePaths;

public class ExportHelper 
{
    public ExportHelper() {
        
    }

    private static final String embeddedWorkspaceName = "Embedded_Workspace";

    public static Path toEmbeddedWorkspaceRelativePath(String pathStr) {
        return WorkspacePaths.installationDirectory().resolve(embeddedWorkspaceName)
            .relativize(Path.of(pathStr));
    }

    public static Path toAbsoluteFromAppRootPath(Path path)
    {
        if (!path.isAbsolute())
            path = WorkspacePaths.applicationDirectory().resolve(path);

        return path;
    }

    public static String toAbsoluteFromAppRootPath(String pathStr)
    {
        Path path = Path.of(pathStr);

        if (!path.isAbsolute())
            path = WorkspacePaths.applicationDirectory().resolve(path);

        return path.toString();
    }

    public static Path toAbsoluteFromEclipsePath(Path path)
    {
        return WorkspacePaths.resolveAgainstInstallation(path);
    }

    public static String toAbsoluteFromEclipsePath(String pathStr)
    {
        return WorkspacePaths.resolveAgainstInstallation(pathStr);
    }


    public static String toAbsoluteFromEmbeddedWorkspacePath(String pathStr)
    {
        return WorkspacePaths.installationDirectory().resolve(embeddedWorkspaceName).resolve(pathStr).toString();
    }

    public static String getEmbeddedWorkspaceName() {
        return embeddedWorkspaceName;
    }

    public static Map<String,Path> resolveLinks(Path projectFilePath,IProject targetProject) {
        try {
            Map<String,Path> locations = new HashMap<String,Path>();
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(projectFilePath.toFile());
            doc.getDocumentElement().normalize();

            NodeList linkList = doc.getElementsByTagName("link");

            for (int i = 0; i < linkList.getLength(); i++) {
                Element link = (Element) linkList.item(i);
                String name = link.getElementsByTagName("name").item(0).getTextContent();
                
                // Location can be raw text or a URI
                NodeList locElem = link.getElementsByTagName("location");
                NodeList uriElem = link.getElementsByTagName("locationURI");
                
                String rawLocation = null;
                if (locElem.getLength() > 0) {
                    rawLocation = locElem.item(0).getTextContent();
                } else if (uriElem.getLength() > 0) {
                    rawLocation = uriElem.item(0).getTextContent();
                }

                if (rawLocation != null) {
                    IResource resource = targetProject.findMember(name);
                    if (resource == null || resource.getLocation() == null) {
                        throw new IllegalStateException("Cannot resolve linked resource: " + name);
                    }
                    Path resolvedPath = resource.getLocation().toFile().toPath();

                    System.out.println("Link Name: " + name + " -> Resolved Path: " + resolvedPath);
                    
                    locations.put(name,resolvedPath);
                }
            }
            return locations;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}