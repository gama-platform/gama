/*
 * Copyright 2016 Albert Tregnaghi
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * Unpersisted marker helper is a helper object for markers. "Unpersisted",
 * because the created markers are recognized and added to a list. So they can
 * all be removed by the helper at once (this is normally not possible). But the
 * handling inside the list does need the markers to be NOT persisted!!!
 * 
 * @author albert
 *
 */

public class UnpersistedMarkerHelper extends AbstractMarkerHelper {

    private List<IMarker> markerRegistry;

    public UnpersistedMarkerHelper(String markerType) {
        this.markerType = markerType;
        markerRegistry = new ArrayList<>();
    }

    @Override
    protected void handleMarkerAdded(IMarker marker) {
        if (marker == null) {
            return;
        }
        markerRegistry.add(marker);
    }

    /**
     * Removes all created error markers
     * 
     * @throws CoreException
     */
    public void removeAllRegisteredMarkers() throws CoreException {
        List<IMarker> workingCopy = new ArrayList<>(markerRegistry);
        for (IMarker marker : workingCopy) {
            String type = null;
            boolean markerExists = marker.exists();

            if (markerExists) {
                try {
                    type = marker.getType();
                } catch (CoreException e) {
                    markerExists = false;
                }

                if (IMarker.TASK.equals(type)) {
                    /* tasks are not deleted */
                    continue;
                }
            }
            markerRegistry.remove(marker);
            if (!markerExists) {
                /*
                 * means marker.getType() failed, because marker does not exist any more. This
                 * can happen when a marker is removed manually on ui.
                 */
                continue;
            }
            marker.delete();

        }
    }

    public boolean hasRegisteredMarkers() {
        return !markerRegistry.isEmpty();
    }

}