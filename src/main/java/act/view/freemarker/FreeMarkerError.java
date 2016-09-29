package act.view.freemarker;

import act.Act;
import act.app.SourceInfo;
import freemarker.core.ParseException;
import org.osgl.util.E;

import java.util.List;

public class FreeMarkerError extends act.view.TemplateException {

    public FreeMarkerError(ParseException t) {
        super(t);
    }

    public FreeMarkerError(freemarker.template.TemplateException t) {
        super(t);
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
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
        return getCauseOrThis().getMessage();
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
            return view.loadResources(template);
        }
    }
}
