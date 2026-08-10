package gama.export;

import java.nio.file.Path;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gama.export.ExportActivator;

public class ExportHelper 
{
    public ExportHelper() {
        
    }

    private static final String embeddedWorkspaceName = "Embedded_Workspace";

    public static Path resolveEmbeddedPath(Path path)
    {
        if (!path.isAbsolute())
            path = Path.of(ExportActivator.appRootPathStr,path.toString());

        return path;
    }

    public static String resolveEmbeddedPath(String pathStr)
    {
        Path path = Path.of(pathStr);

        if (!path.isAbsolute())
            path = Path.of(ExportActivator.appRootPathStr,pathStr);

        return path.toString();
    }

    public static String resolveEmbeddedWorkspacePath(String pathStr)
    {
        return Path.of(ExportActivator.appRootPathStr,embeddedWorkspaceName,pathStr).toString();
    }

    public static String getEmbeddedWorkspaceName() {
        return embeddedWorkspaceName;
    }

    private static Path resolveVariables(String expression, Path linkPath, Path projectDir, IPathVariableManager pathVarManager) {
        IPath path = new org.eclipse.core.runtime.Path(expression);
        IPath resolved = null;
        
        if(expression.contains("PARENT_LOC"))
            resolved = handleParentLoc(expression,projectDir.resolve(linkPath));
        else
            resolved = pathVarManager.resolvePath(path);
        
        if (resolved != null)
            path = resolved;
        
        return Path.of(URLDecoder.decode(path.toOSString(),StandardCharsets.UTF_8));
    }

    private static IPath handleParentLoc(String expression, Path linkFullPath) {
        try {
            int start = expression.indexOf("PARENT-") + 7;
            int end = expression.indexOf("-PARENT_LOC");
            int parentIndex = Integer.parseInt(expression.substring(start, end));

            Path parent = linkFullPath.getParent();

            for (int i=0;i<parentIndex;i++)
            {
                parent = parent.getParent();
            }

            String resolvedPathStr = expression.replaceAll("\\$?\\{?PARENT-\\d+-PARENT_LOC\\}?",parent.toString());
            resolvedPathStr = resolvedPathStr.replaceAll("PARENT_LOC",parent.toString());

            return new org.eclipse.core.runtime.Path(resolvedPathStr);

        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String,Path> resolveLinks(Path projectFilePath,IProject targetProject) {
        try {
            Map<String,Path> locations = new HashMap<String,Path>();
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(projectFilePath.toFile());
            doc.getDocumentElement().normalize();

            Path projectDir = projectFilePath.getParent();
            IPathVariableManager pathVarManager = targetProject.getPathVariableManager();
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
                    Path resolvedPath = resolveVariables(rawLocation, Path.of(name), projectDir, pathVarManager);

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