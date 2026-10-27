package gama.export;

import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import gama.export.ExportActivator;

public class ExportHelper 
{
    public ExportHelper() {
        
    }

    private static final String embeddedWorkspaceName = "Embedded_Workspace";

    public static Path toEmbeddedWorkspaceRelativePath(String pathStr) {
        return Path.of(ExportActivator.eclipsePathStr,embeddedWorkspaceName)
            .relativize(Path.of(pathStr));
    }

    public static Path toAbsoluteFromAppRootPath(Path path)
    {
        if (!path.isAbsolute())
            path = Path.of(ExportActivator.appRootPathStr,path.toString());

        return path;
    }

    public static String toAbsoluteFromAppRootPath(String pathStr)
    {
        Path path = Path.of(pathStr);

        if (!path.isAbsolute())
            path = Path.of(ExportActivator.appRootPathStr,pathStr);

        return path.toString();
    }

    public static Path toAbsoluteFromEclipsePath(Path path)
    {
        if (!path.isAbsolute())
            path = Path.of(ExportActivator.eclipsePathStr,path.toString());

        return path;
    }

    public static String toAbsoluteFromEclipsePath(String pathStr)
    {
        Path path = Path.of(pathStr);

        if (!path.isAbsolute())
            path = Path.of(ExportActivator.eclipsePathStr,pathStr);

        return path.toString();
    }


    public static String toAbsoluteFromEmbeddedWorkspacePath(String pathStr)
    {
        return Path.of(ExportActivator.eclipsePathStr,embeddedWorkspaceName,pathStr).toString();
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

	public static IProject getProjectFromEvent(final ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
		IProject project = null;
        
        if (selection != null && selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();
            
            if (firstElement != null) {
                
                if (firstElement instanceof IProject) {
                    project = (IProject) firstElement;
                } 
                else if (firstElement instanceof IAdaptable) {
                    IResource resource = ((IAdaptable) firstElement).getAdapter(IResource.class);
                    if (resource != null) {
                        project = resource.getProject();
                    }
                }
            }
        }
		return project;
	} 

}