package act.view.freemarker;

import act.app.App;
import act.app.event.AppEventId;
import act.app.event.AppEventListener;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import org.osgl.util.E;

import java.io.IOException;
import java.util.EventObject;

public class FreeMarkerView extends View {

    private Configuration conf;

    @Override
    public String name() {
        return "freemarker";
    }

    @Override
    protected Template loadTemplate(String resourcePath, ActContext context) {
        try {
            freemarker.template.Template freemarkerTemplate = conf.getTemplate(resourcePath, context.locale());
            return new FreeMarkerTemplate(freemarkerTemplate);
        } catch (TemplateNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    protected void init() {
        initConf();
    }

    private void initConf() {
        conf = new Configuration(Configuration.VERSION_2_3_23);
        conf.setDefaultEncoding("UTF-8");
        conf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    }

    @Override
    protected void reload(final App app) {
        app.eventBus().bind(AppEventId.CLASS_LOADER_INITIALIZED, new AppEventListener() {
            @Override
            public void on(EventObject event) throws Exception {
                conf.setClassLoaderForTemplateLoading(app.classLoader(), "/freemarker");
            }

            @Override
            public String id() {
                return "init-freemarker-conf";
            }

            @Override
            public void destroy() {
            }

            @Override
            public boolean isDestroyed() {
                return false;
            }
        });
    }
}
