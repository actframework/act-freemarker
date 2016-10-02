package act.view.freemarker;

import act.Act;
import act.app.ActionContext;
import act.view.TemplateBase;
import freemarker.core.ParseException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.osgl.$;
import org.osgl.http.H;
import org.osgl.util.E;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class FreeMarkerTemplate extends TemplateBase {

    Template tmpl;

    FreeMarkerTemplate(Template tmpl) {
        this.tmpl = $.notNull(tmpl);
    }

    @Override
    protected void merge(Map<String, Object> renderArgs, H.Response response) {
        if (Act.isDev()) {
            super.merge(renderArgs, response);
            return;
        }
        try {
            tmpl.process(renderArgs, response.writer());
        } catch (Exception e) {
            throw E.unexpected(e, "Error output freemarker template");
        }
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
