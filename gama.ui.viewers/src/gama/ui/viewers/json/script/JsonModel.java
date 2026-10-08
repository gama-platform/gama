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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import gama.ui.viewers.json.outline.Item;
import gama.ui.viewers.json.outline.RootItem;

public class JsonModel {

    private List<JsonError> errors;
    private JsonNode rootNode;
    private RootItem rootItem;
    private Map<Integer, Item> itemOffsetMap;

    public JsonModel() {
        errors = new ArrayList<>();
        itemOffsetMap = new HashMap<Integer, Item>();
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public List<JsonError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void setRootNode(JsonNode rootNode) {
        this.rootNode = rootNode;
    }

    public void setRootItem(RootItem item) {
        this.rootItem = item;
    }

    public RootItem getRootItem() {
        return rootItem;
    }

    public Map<Integer, Item> getItemOffsetMap() {
        return itemOffsetMap;
    }

}
