/*
 * Copyright 2005 Simon Mieth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package gama.dependencies.kabeja.parser.entities;

import gama.dependencies.kabeja.dxf.DXFEllipse;
import gama.dependencies.kabeja.dxf.DXFEntity;
import gama.dependencies.kabeja.parser.DXFValue;

/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 *
 *
 */
public class DXFEllipseHandler extends AbstractEntityHandler {
	public final static String ENTITY_NAME = "ELLIPSE";
	public final static int RATIO = 40;
	public final static int START_PARAMETER = 41;
	public final static int END_PARAMTER = 42;
	public static final int COUNTERCLOCKWISE = 73;
	private DXFEllipse ellipse;

	@Override
	public void endDXFEntity() {}

	@Override
	public DXFEntity getDXFEntity() { return ellipse; }

	@Override
	public String getDXFEntityName() { return ENTITY_NAME; }

	@Override
	public boolean isFollowSequence() { return false; }

	@Override
	public void parseGroup(int groupCode, DXFValue value) {
		switch (groupCode) {
			case GROUPCODE_START_X:
				ellipse.getCenterPoint().setX(value.getDoubleValue());
				break;
			case GROUPCODE_START_Y:
				ellipse.getCenterPoint().setY(value.getDoubleValue());
				break;
			case GROUPCODE_START_Z:
				ellipse.getCenterPoint().setZ(value.getDoubleValue());
				break;
			case END_X:
				ellipse.getMajorAxisDirection().setX(value.getDoubleValue());
				break;
			case END_Y:
				ellipse.getMajorAxisDirection().setY(value.getDoubleValue());
				break;
			case END_Z:
				ellipse.getMajorAxisDirection().setZ(value.getDoubleValue());
				break;
			case RATIO:
				ellipse.setRatio(value.getDoubleValue());
				break;
			case START_PARAMETER:
				ellipse.setStartParameter(value.getDoubleValue());
				break;
			case END_PARAMTER:
				ellipse.setEndParameter(value.getDoubleValue());
				break;
			case COUNTERCLOCKWISE:
				ellipse.setCounterClockwise(value.getBooleanValue());
				break;
			default:
				super.parseCommonProperty(groupCode, value, ellipse);
				break;
		}
	}

	@Override
	public void startDXFEntity() {
		ellipse = new DXFEllipse();
		ellipse.setDXFDocument(doc);
	}
}
