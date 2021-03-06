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
package com.vaadin.flow.uitest.ui.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.theme.AbstractTheme;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.uitest.ui.template.ThemedTemplateView.CustomTheme;

@Tag("themed-template")
@HtmlImport("frontend://com/vaadin/flow/uitest/ui/template/ThemedTemplate.html")
@Route(value = "com.vaadin.flow.uitest.ui.template.ThemedTemplateView")
@Theme(CustomTheme.class)
public class ThemedTemplateView extends PolymerTemplate<TemplateModel> {

    public static class CustomTheme implements AbstractTheme {

        @Override
        public String getBaseUrl() {
            String pkg = ThemedTemplateView.class.getPackage().getName();
            int index = pkg.lastIndexOf('.');
            pkg = pkg.substring(0, index);
            return "/" + pkg.replace('.', '/');
        }

        @Override
        public String getThemeUrl() {
            return getBaseUrl() + "/custom-theme";
        }

    }
}
