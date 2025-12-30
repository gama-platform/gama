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
package gama.ui.viewers.json.outline;

public enum ItemType {

    JSON_NODE,

    META_INFO,

    META_ERROR,

    META_DEBUG,

    /**
     * Represents a node containing other nodes - used to handle arrays too big and
     * so slow in tree
     */
    VIRTUAL_ARRAY_SEGMENT_NODE
}
