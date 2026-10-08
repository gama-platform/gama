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
package gama.ui.viewers.json.eclipse.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class FallbackOutlineContentProvider implements ITreeContentProvider {

    private static final Object[] NO_CHILDREN = new Object[] {};
    private static final Object[] ROOT_ELEMENTS = new Object[] { "No content available" };

    @Override
    public Object[] getElements(Object inputElement) {
        return ROOT_ELEMENTS;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return NO_CHILDREN;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return false;
    }

}
