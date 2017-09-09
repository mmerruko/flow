/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.vaadin.annotations.ClientDelegate;
import com.vaadin.annotations.HtmlImport;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.Tag;
import com.vaadin.data.HasDataProvider;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ArrayUpdater;
import com.vaadin.data.provider.DataCommunicator;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.util.HtmlUtils;
import com.vaadin.flow.util.JsonUtils;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

@Tag("vaadin-grid")
@HtmlImport("frontend://bower_components/vaadin-grid/vaadin-grid.html")
@JavaScript("context://gridConnector.js")
public class Grid<T> extends Component implements HasDataProvider<T> {
    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public Update startUpdate(int currentSize) {
            return new Update() {
                private List<Runnable> queue = new ArrayList<>();
                {
                    enqueue("connectorUpdateSize", currentSize);
                }

                @Override
                public void set(int start, List<JsonValue> items) {
                    enqueue("connectorSet", start,
                            items.stream().collect(JsonUtils.asArray()));
                }

                @Override
                public void clear(int start, int length) {
                    enqueue("connectorClear", start, length);
                }

                @Override
                public void commit(int updateId) {
                    enqueue("connectorConfirm", updateId);
                    queue.forEach(Runnable::run);
                    queue.clear();
                }

                private void enqueue(String name, Serializable... arguments) {
                    queue.add(() -> getElement().callFunction(name, arguments));
                }
            };
        }
    };

    private final Map<String, Function<T, JsonValue>> columnGenerators = new HashMap<>();
    private final DataCommunicator<T> dataCommunicator = new DataCommunicator<>(
            this::generateItemJson, arrayUpdater, getElement().getNode());

    private int nextColumnId = 0;

    public Grid() {
        // No lazy loading for now
        dataCommunicator.setRequestedRange(0, Integer.MAX_VALUE);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        attachEvent.getUI().getPage().executeJavaScript(
                "window.gridConnector.initArray($0)", getElement());
    }

    public void addColumn(String header,
            ValueProvider<T, String> valueProvider) {
        int id = nextColumnId++;
        String columnKey = "col" + id;
        columnGenerators.put(columnKey, valueProvider.andThen(Json::create));
        dataCommunicator.reset();

        // Use innerHTML to set document fragment instead of DOM children
        Element headerTemplate = new Element("template")
                .setAttribute("class", "header")
                .setProperty("innerHTML", HtmlUtils.escape(header));
        Element contentTemplate = new Element("template")
                .setProperty("innerHTML", "[[item." + columnKey + "]]");

        Element colElement = new Element("vaadin-grid-column")
                .setAttribute("id", columnKey)
                .appendChild(headerTemplate, contentTemplate);

        getElement().appendChild(colElement);
    }

    private JsonValue generateItemJson(String key, T item) {
        JsonObject json = Json.createObject();
        json.put("key", key);
        columnGenerators.forEach((columnKey, generator) -> json.put(columnKey,
                generator.apply(item)));
        return json;
    }

    @ClientDelegate
    public void confirmUpdate(int id) {
        dataCommunicator.confirmUpdate(id);
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        dataCommunicator.setDataProvider(dataProvider, null);
    }
}
