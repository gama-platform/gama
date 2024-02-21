/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package gama.dependencies.kabeja.parser.objects;

import gama.dependencies.kabeja.dxf.DXFConstants;
import gama.dependencies.kabeja.dxf.objects.DXFImageDefObject;
import gama.dependencies.kabeja.dxf.objects.DXFObject;
import gama.dependencies.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFImageDefHandler extends AbstractDXFObjectHandler {
    public final static int GROUPCODE_FILENAME = 1;
    protected DXFImageDefObject imageDef;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.objects.DXFObjectHandler#getObjectType()
     */
    public String getObjectType() {
        return DXFConstants.OBJECT_TYPE_IMAGEDEF;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.objects.DXFObjectHandler#startObject()
     */
    public void startObject() {
        imageDef = new DXFImageDefObject();
        imageDef.setDXFDocument(this.doc);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.objects.DXFObjectHandler#endObject()
     */
    public void endObject() {
        
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.objects.DXFObjectHandler#getDXFObject()
     */
    public DXFObject getDXFObject() {
        
        return imageDef;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.objects.DXFObjectHandler#parseGroup(int,
     *      de.miethxml.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_FILENAME:
            imageDef.setFilename(value.getValue());

            break;

        default:
            super.parseCommonGroupCode(groupCode, value, imageDef);

            break;
        }
    }
}
