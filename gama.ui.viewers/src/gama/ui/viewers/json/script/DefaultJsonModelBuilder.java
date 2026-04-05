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
package gama.ui.viewers.json.script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;

import gama.ui.viewers.json.outline.Item;
import gama.ui.viewers.json.outline.ItemType;
import gama.ui.viewers.json.outline.ItemVariant;
import gama.ui.viewers.json.outline.RootItem;

public class DefaultJsonModelBuilder implements JsonModelBuilder {

    private JsonFactory configuredJSONFactory = new JsonFactory();
    private JsonFactory ignoreFailuresJSONFactory = new JsonFactory();
    
    public static final DefaultJsonModelBuilder INSTANCE = new DefaultJsonModelBuilder();

    private DefaultJsonModelBuilder() {
        /* configure ignore failures factory */
        for (JsonReadFeature readFeature : JsonReadFeature.values()) {
            if (readFeature.name().toUpperCase().startsWith("ALLOW")) {
                ignoreFailuresJSONFactory.configure(readFeature.mappedFeature(), true);
            }
        }
    }

    private class JSONContext {
        JsonModel model;
        JsonParser parser;
        String currentName;
        Item currentParent;
        int tresholdGroupArrays;
        public RootItem rootItem;
    }

    public JsonModel build(String json, int tresholdGroupArrays, boolean ignoreFailures) {
        JsonModel model = new JsonModel();
        JSONContext context = new JSONContext();
        context.model = model;
        context.tresholdGroupArrays = tresholdGroupArrays;
        try {
            JsonFactory factory = null;

            if (ignoreFailures) {
                factory = ignoreFailuresJSONFactory;
            } else {
                factory = configuredJSONFactory;
            }
            context.parser = factory.createParser(json);

            while (!context.parser.isClosed()) {
                JsonToken token = context.parser.nextToken();
                if (token == null) {
                    break;
                }
                switch (token) {
                case END_ARRAY:
                    String originParentName = provideArrayNameWithIndexSizeOfCurrentParent(context);
                    provideNamesIndexedForChildrenOfCurrentParent(originParentName, context);
                    groupChildrenOfCurrentParent(originParentName, context);

                    context.currentParent = context.currentParent.getParent();

                    break;
                case END_OBJECT:
                    context.currentParent = context.currentParent.getParent();
                    break;
                case FIELD_NAME:
                    context.currentName = context.parser.getCurrentName();
                    break;
                case NOT_AVAILABLE:
                    break;
                case START_ARRAY:
                    Item arrayItem = addJsonNodeToParent(ItemVariant.ARRAY, context, token);
                    context.currentParent = arrayItem;
                    break;

                case START_OBJECT:
                    Item objectItem = addJsonNodeToParent(ItemVariant.OBJECT, context, token);
                    context.currentParent = objectItem;
                    break;

                case VALUE_EMBEDDED_OBJECT:
                    addJsonNodeToParent(ItemVariant.OBJECT, context, token);
                    break;
                case VALUE_FALSE:
                    addJsonNodeToParent(ItemVariant.VALUE, context, token);
                    break;
                case VALUE_NULL:
                    addJsonNodeToParent(ItemVariant.VALUE, context, token);
                    break;
                case VALUE_NUMBER_FLOAT:
                    addJsonNodeToParent(ItemVariant.VALUE, context, token);
                    break;
                case VALUE_NUMBER_INT:
                    addJsonNodeToParent(ItemVariant.VALUE, context, token);
                    break;
                case VALUE_STRING:
                    addJsonNodeToParent(ItemVariant.VALUE, context, token);
                    break;
                case VALUE_TRUE:
                    addJsonNodeToParent(ItemVariant.VALUE, context, token);
                    break;
                default:
                    break;

                }
            }
            model.setRootItem(context.rootItem);
        } catch (IOException e) {
            JsonError error = new JsonError();
            error.message = e.getMessage();
            model.getErrors().add(error);
        }
        return model;
    }

    private String provideArrayNameWithIndexSizeOfCurrentParent(JSONContext context) {
        Item originParent = context.currentParent;
        if (originParent == null) {
            return null;
        }
        List<Item> children = originParent.getChildren();
        String originName = originParent.getName();
        if (originName == null) {
            // we set to empty in this case - looks better
            originName = "";
        }
        originParent.setName(originName + " (" + children.size() + ")");

        return originName;

    }

    private void provideNamesIndexedForChildrenOfCurrentParent(String originParentName, JSONContext context) {
        Item originParent = context.currentParent;
        if (originParent == null) {
            return;
        }
        List<Item> children = originParent.getChildren();

        int index = 0;
        for (Iterator<Item> it = children.iterator(); it.hasNext();) {
            Item item = it.next();
            item.setName(originParentName + " [" + index + "]");
            item.setArrayIndex(index);
            index++;
        }
    }

    private void groupChildrenOfCurrentParent(String originParentName, JSONContext context) {
        Item originParent = context.currentParent;
        if (originParent == null) {
            return;
        }
        List<Item> children = originParent.getChildren();

        int amountOfChildren = children.size();
        int groupAmount = context.tresholdGroupArrays;
        if (amountOfChildren <= groupAmount) {
            /* no grouping necessary */
            return;
        }

        int index = 0;
        int remaining = amountOfChildren;

        List<Item> newchildren = new ArrayList<>();
        Item parent = null;
        originParent.setChildren(newchildren);

        for (Iterator<Item> it = children.iterator(); it.hasNext();) {
            Item child = it.next();
            remaining--;

            if (parent == null || parent.getChildren().size() > groupAmount) {
                parent = new Item();
                parent.setType(ItemType.VIRTUAL_ARRAY_SEGMENT_NODE);
                parent.setItemVariant(ItemVariant.ARRAY_SEGMENT);
                String parentName = "[" + index + "..";
                if (remaining < 100) {
                    parentName += index + remaining;
                } else {
                    parentName += index + groupAmount;
                }
                parentName += "]";
                parent.setName(parentName);
                parent.setOffset(child.getOffset());

                originParent.addChild(parent);
            }
            parent.addChild(child);
            parent.setEndOffset(child.getEndOffset());

            index++;
        }

        children.clear();
    }

    private Item addJsonNodeToParent(ItemVariant variant, JSONContext context, JsonToken token) {
        Item item = context.rootItem == null ? new RootItem(): new Item();
        
        item.setName(context.currentName);
        item.setOriginName(context.currentName);
        item.setType(ItemType.JSON_NODE);
        item.setItemVariant(variant);
        
        if (variant == ItemVariant.VALUE) {
            try {
                item.setContent(context.parser.getValueAsString());
            } catch (IOException e) {
                item.setContent("[FAILED]:" + e.getMessage());
            }
        }

        if (item instanceof RootItem) {
            context.rootItem = (RootItem)item;
            context.currentParent = context.rootItem;
        } else {
            if (context.currentParent != null) {
                context.currentParent.addChild(item);
            }
        }
        calculateOffsetAndLength(context, item);

        // reset text
        context.currentName = null;
        return item;
    }

    private void calculateOffsetAndLength(JSONContext context, Item item) {
        JsonLocation location = context.parser.getCurrentLocation();
        int offset = (int) location.getCharOffset(); // end ...
        int length = -1;

        try {
            if (context.parser.getCurrentToken() == JsonToken.VALUE_STRING) {
                length = context.parser.getTextLength();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (length == -1) {
            length = 0;
        } else {
            offset = offset - length - 1;
        }
        item.setOffset(offset);
        item.setLength(length);
        context.model.getItemOffsetMap().put(offset, item);
    }

}
