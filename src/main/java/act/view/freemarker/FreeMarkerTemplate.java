package act.view.freemarker;

import act.view.TemplateBase;
import freemarker.core.ParseException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.osgl.$;
import org.osgl.util.E;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class FreeMarkerTemplate extends TemplateBase {

    Template tmpl;

    FreeMarkerTemplate(Template tmpl) {
        this.tmpl = $.notNull(tmpl);
    }

    @Override
    protected String render(Map<String, Object> renderArgs) {
        Writer w = new StringWriter();
        try {
            tmpl.process(renderArgs, w);
        } catch (ParseException e) {
            throw new FreeMarkerError(e);
        } catch (TemplateException e) {
            throw new FreeMarkerError(e);
        } catch (IOException e) {
            throw E.ioException(e);
        } catch (Exception e) {
            throw E.unexpected(e);
        }
        return w.toString();
    }
}
