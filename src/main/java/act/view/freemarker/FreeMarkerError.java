package act.view.freemarker;

import act.Act;
import act.app.SourceInfo;
import act.view.TemplateError;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import org.osgl.util.E;

import java.util.List;

public class FreeMarkerError extends TemplateError {

    public FreeMarkerError(ParseException t) {
        super(t);
    }

    public FreeMarkerError(TemplateException t) {
        super(t);
    }

    @Override
    protected void populateSourceInfo(Throwable t) {
        if (t instanceof ParseException) {
            templateInfo = new FreeMarkerSourceInfo((ParseException) t);
        } else if (t instanceof TemplateException) {
            templateInfo = new FreeMarkerSourceInfo((TemplateException) t);
        } else {
            throw E.unexpected("Unknown exception type: %s", t.getClass());
        }
    }

    @Override
    public String errorMessage() {
        return getCauseOrThis().getMessage();
    }

    private static class FreeMarkerSourceInfo implements SourceInfo {

        private String fileName;
        private List<String> lines;
        private int lineNumber;

        FreeMarkerSourceInfo(ParseException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateName();
            lines = readTemplateSource(fileName);
        }

        FreeMarkerSourceInfo(TemplateException e) {
            lineNumber = e.getLineNumber();
            fileName = e.getTemplateSourceName();
            lines = readTemplateSource(fileName);
        }

        @Override
        public String fileName() {
            return fileName;
        }

        @Override
        public List<String> lines() {
            return lines;
        }

        @Override
        public Integer lineNumber() {
            return lineNumber;
        }

        @Override
        public boolean isSourceAvailable() {
            return true;
        }

        private static List<String> readTemplateSource(String template) {
            FreeMarkerView view = (FreeMarkerView) Act.viewManager().view(FreeMarkerView.ID);
            return view.loadResources(template);
        }
    }
}
