package act.view.freemarker;

import act.app.App;
import act.app.event.AppEventId;
import act.app.event.AppEventListener;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import freemarker.cache.TemplateCache;
import freemarker.cache.TemplateLoader;
import freemarker.cache.TemplateLookupResult;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import org.osgl.$;
import org.osgl.util.E;
import org.osgl.util.IO;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;

public class FreeMarkerView extends View {

    public static final String ID = "freemarker";

    private Configuration conf;

    @Override
    public String name() {
        return ID;
    }

    @Override
    protected Template loadTemplate(String resourcePath, ActContext context) {
        try {
            freemarker.template.Template freemarkerTemplate = conf.getTemplate(resourcePath, context.locale());
            return new FreeMarkerTemplate(freemarkerTemplate);
        } catch (TemplateNotFoundException e) {
            return null;
        } catch (ParseException e) {
            throw new FreeMarkerTemplateException(e);
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    protected void init() {
        initConf();
    }

    List<String> loadResources(String template) {
        TemplateLoader loader = conf.getTemplateLoader();
        try {
            Method lookup = TemplateCache.class.getDeclaredMethod("lookupTemplate", String.class, Locale.class, Object.class);
            lookup.setAccessible(true);
            Field cache = Configuration.class.getDeclaredField("cache");
            cache.setAccessible(true);
            TemplateLookupResult result = $.invokeVirtual(cache.get(conf), lookup, template, Locale.getDefault(), null);
            Method templateSource = TemplateLookupResult.class.getDeclaredMethod("getTemplateSource");
            templateSource.setAccessible(true);
            Reader reader = loader.getReader($.invokeVirtual(result, templateSource), conf.getEncoding(conf.getLocale()));
            return IO.readLines(reader);
        } catch (IOException e1) {
            throw E.ioException(e1);
        } catch (Exception e) {
            throw E.unexpected(e);
        }
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
            public Class<? extends Annotation> scope() {
                return ApplicationScoped.class;
            }

            @Override
            public boolean isDestroyed() {
                return false;
            }
        });
    }
}
