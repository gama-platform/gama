/*
 * Copyright 2021 Albert Tregnaghi
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
package gama.ui.viewers.json.eclipse.document;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;

public class FallbackJsonPartitioner implements IDocumentPartitioner {

    private static final String[] LEGAL_CONTENTTYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE };
    private ITypedRegion[] PARTITITIONING = new ITypedRegion[] {};
    private IDocument document;
    private ITypedRegion documentAllRegion;

    @Override
    public void connect(IDocument document) {
        this.document = document;
        updateRegion();
    }

    private boolean updateRegion() {
        if (document == null) {
            this.documentAllRegion = null;
            return false;
        }
        if (documentAllRegion == null) {
            calculateNewAllRegion();
            return true;
        }
        if (document.getLength() == documentAllRegion.getLength()) {
            return false;
        }
        calculateNewAllRegion();
        return true;
    }

    private void calculateNewAllRegion() {
        this.documentAllRegion = new TypedRegion(0, document.getLength(), IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Override
    public void disconnect() {
        this.document = null;
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {

    }

    @Override
    public boolean documentChanged(DocumentEvent event) {
        return updateRegion();
    }

    @Override
    public String[] getLegalContentTypes() {
        return LEGAL_CONTENTTYPES;
    }

    @Override
    public String getContentType(int offset) {
        return IDocument.DEFAULT_CONTENT_TYPE;
    }

    @Override
    public ITypedRegion[] computePartitioning(int offset, int length) {
        return PARTITITIONING;
    }

    @Override
    public ITypedRegion getPartition(int offset) {
        return documentAllRegion;
    }

}
