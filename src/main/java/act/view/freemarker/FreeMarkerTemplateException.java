package act.view.freemarker;

import act.Act;
import act.app.SourceInfo;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;

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
        Throwable t = getCauseOrThis();
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
        return t.toString();
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
