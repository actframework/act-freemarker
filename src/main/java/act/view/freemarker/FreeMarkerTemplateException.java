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
import act.app.SourceInfo;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.lang.reflect.Method;
import java.util.List;

public class FreeMarkerTemplateException extends act.view.TemplateException {

    public FreeMarkerTemplateException(ParseException t) {
        super(t);
    }

    public FreeMarkerTemplateException(freemarker.template.TemplateException t) {
        super(t);
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
        sourceInfo = getJavaSourceInfo(t.getCause());
        if (t instanceof ParseException) {
            templateInfo = new FreeMarkerSourceInfo((ParseException) t);
        } else if (t instanceof freemarker.template.TemplateException) {
            templateInfo = new FreeMarkerSourceInfo((freemarker.template.TemplateException) t);
        } else {
            throw E.unexpected("Unknown exception type: %s", t.getClass());
        }
    }

    @Override
    public String errorMessage() {
        Throwable t = rootCauseOf(this);
        boolean isParseException = t instanceof ParseException;
        boolean isTemplateException = t instanceof TemplateException;
        if (isParseException || isTemplateException) {
            try {
                Method m;
                if (isParseException) {
                    m = ParseException.class.getDeclaredMethod("getDescription");
                } else {
                    m = TemplateException.class.getDeclaredMethod("getDescription");
                }
                m.setAccessible(true);
                return $.invokeVirtual(t, m);
            } catch (NoSuchMethodException e) {
                throw E.unexpected(e);
            }
        }
        String msg = t.getLocalizedMessage();
        return S.blank(msg) ? t.toString() : msg;
    }

    @Override
    public List<String> stackTrace() {
        Throwable t = getCause();
        if (t instanceof ParseException || t instanceof InvalidReferenceException) {
            return C.list();
        }
        return super.stackTrace();
    }

    @Override
    protected boolean isTemplateEngineInvokeLine(String s) {
        return s.contains("freemarker.ext.beans.BeansWrapper.invokeMethod");
    }

    private static class FreeMarkerSourceInfo extends SourceInfo.Base {

        FreeMarkerSourceInfo(ParseException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateName();
            lines = readTemplateSource(fileName);
        }

        FreeMarkerSourceInfo(freemarker.template.TemplateException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateSourceName();
            lines = readTemplateSource(fileName);
        }

        private static List<String> readTemplateSource(String template) {
            FreeMarkerView view = (FreeMarkerView) Act.viewManager().view(FreeMarkerView.ID);
            return view.loadContent(template);
        }
    }
}
