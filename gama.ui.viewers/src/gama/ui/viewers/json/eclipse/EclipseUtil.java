/*
 * Copyright 2020 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
package gama.ui.viewers.json.eclipse;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

public class EclipseUtil {

    public static ImageDescriptor createImageDescriptor(String path, String pluginId) {
        if (path == null) {
            /* fall back if path null , so avoid NPE in eclipse framework */
            return ImageDescriptor.getMissingImageDescriptor();
        }
        if (pluginId == null) {
            /* fall back if pluginId null , so avoid NPE in eclipse framework */
            return ImageDescriptor.getMissingImageDescriptor();
        }
        Bundle bundle = Platform.getBundle(pluginId);
        if (bundle == null) {
            /*
             * fall back if bundle not available, so avoid NPE in eclipse framework
             */
            return ImageDescriptor.getMissingImageDescriptor();
        }
        URL url = FileLocator.find(bundle, new Path(path), null);

        ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
        return imageDesc;
    }

    public static IEditorPart getActiveEditor() {
        IWorkbenchPage page = getActivePage();
        IEditorPart activeEditor = page.getActiveEditor();
        return activeEditor;
    }

    /**
     * Returns active page or <code>null</code>
     * 
     * @return active page or <code>null</code>
     */
    public static IWorkbenchPage getActivePage() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return null;
        }
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        return window.getActivePage();
    }

    /**
     * Returns active workbench shell - or <code>null</code>
     * 
     * @return active workbench shell - or <code>null</code>
     */
    public static Shell getActiveWorkbenchShell() {
        IWorkbench workbench = getWorkbench();
        if (workbench == null) {
            return null;
        }
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        Shell shell = window.getShell();
        return shell;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        IWorkbench workbench = getWorkbench();
        if (workbench == null) {
            return null;
        }
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

        if (workbenchWindow != null) {
            return workbenchWindow;
        }
        /* fall back - try to execute in UI */
        WorkbenchWindowRunnable wwr = new WorkbenchWindowRunnable();
        getSafeDisplay().syncExec(wwr);
        return wwr.workbenchWindowFromUI;
    }

    public static IProject[] getAllProjects() {
        IProject[] projects = getWorkspace().getRoot().getProjects();
        return projects;
    }

    /**
     * Get image by path from image registry. If not already registered a new image
     * will be created and registered. If not createable a fallback image is used
     * instead
     * 
     * @param path
     * @param pluginId - plugin id to identify which plugin image should be loaded
     * @return image
     */
    public static Image getImage(String path, String pluginId) {
        ImageRegistry imageRegistry = getImageRegistry();
        if (imageRegistry == null) {
            return null;
        }
        Image image = imageRegistry.get(path);
        if (image == null) {
            ImageDescriptor imageDesc = createImageDescriptor(path, pluginId);
            image = imageDesc.createImage();
            if (image == null) {
                image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            }
            imageRegistry.put(path, image);
        }
        return image;
    }

    public static Display getSafeDisplay() {
        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public static void safeAsyncExec(Runnable runnable) {
        getSafeDisplay().asyncExec(runnable);
    }

    public static void throwCoreException(String message) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, JsonEditorActivator.PLUGIN_ID, message));

    }

    public static void throwCoreException(String message, Exception e) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, JsonEditorActivator.PLUGIN_ID, message, e));

    }

    public static String resolveMessageIfNotSet(String message, Throwable cause) {
        if (message == null) {
            if (cause == null) {
                message = "Unknown";
            } else {
                message = cause.getMessage();
            }
        }
        return message;
    }

    private static ImageRegistry getImageRegistry() {
        JsonEditorActivator mainActivator = JsonEditorActivator.getDefault();
        if (mainActivator == null) {
            return null;
        }
        return mainActivator.getImageRegistry();
    }

    /**
     * Returns workbench or <code>null</code>
     * 
     * @return workbench or <code>null</code>
     */
    private static IWorkbench getWorkbench() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return null;
        }
        IWorkbench workbench = PlatformUI.getWorkbench();
        return workbench;
    }

    private static class WorkbenchWindowRunnable implements Runnable {
        IWorkbenchWindow workbenchWindowFromUI;

        @Override
        public void run() {
            IWorkbench workbench = getWorkbench();
            if (workbench == null) {
                return;
            }
            workbenchWindowFromUI = workbench.getActiveWorkbenchWindow();
        }

    }

    public static void logError(String error, Throwable t) {
        getLog().log(new Status(IStatus.ERROR, JsonEditorActivator.PLUGIN_ID, error, t));
    }

    private static ILog getLog() {
        ILog log = JsonEditorActivator.getDefault().getLog();
        return log;
    }

}
