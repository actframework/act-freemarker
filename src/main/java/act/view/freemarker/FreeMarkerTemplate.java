package act.view.freemarker;

/*-
 * #%L
 * ACT FreeMarker
 * %%
 * Copyright (C) 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.Act;
import act.view.TemplateBase;
import freemarker.core.ParseException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.osgl.$;
import org.osgl.http.H;
import org.osgl.util.E;
import org.rythmengine.utils.IO;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class FreeMarkerTemplate extends TemplateBase {

    Template tmpl;

    FreeMarkerTemplate(Template tmpl) {
        this.tmpl = $.requireNotNull(tmpl);
    }

    @Override
    protected void merge(Map<String, Object> renderArgs, H.Response response) {
        if (Act.isDev()) {
            super.merge(renderArgs, response);
            return;
        }
        Writer writer = response.writer();
        try {
            tmpl.process(renderArgs, writer);
        } catch (Exception e) {
            throw E.unexpected(e, "Error output freemarker template");
        } finally {
            IO.close(writer);
        }
    }

    @Override
    public boolean supportCache() {
        return false;
    }

    @Override
    protected String render(Map<String, Object> renderArgs) {
        Writer w = new StringWriter();
        try {
            tmpl.process(renderArgs, w);
        } catch (ParseException e) {
            throw new FreeMarkerTemplateException(e);
        } catch (TemplateException e) {
            throw new FreeMarkerTemplateException(e);
        } catch (IOException e) {
            throw E.ioException(e);
        } catch (Exception e) {
            throw E.unexpected(e);
        }
        return w.toString();
    }
}
