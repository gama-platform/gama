package gama.export;

import java.nio.file.Path;
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
}