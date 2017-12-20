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

import act.app.App;
import act.util.ActContext;
import act.view.Template;
import act.view.View;
import freemarker.cache.StringTemplateLoader;
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
import org.osgl.util.S;
import osgl.version.Version;
import osgl.version.Versioned;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

@Versioned
public class FreeMarkerView extends View {

    public static final Version VERSION = Version.get();

    public static final String ID = "freemarker";

    private Configuration conf;
    private Configuration stringLoaderConf;
    private StringTemplateLoader stringTemplateLoader;
    private String suffix;

    @Override
    public String name() {
        return ID;
    }

    @Override
    protected Template loadTemplate(String resourcePath) {
        ActContext context = ActContext.Base.currentContext();
        try {
            freemarker.template.Template freemarkerTemplate = conf.getTemplate(resourcePath, context.locale());
            return new FreeMarkerTemplate(freemarkerTemplate);
        } catch (TemplateNotFoundException e) {
            if (resourcePath.endsWith(suffix)) {
                return null;
            }
            return loadTemplate(S.concat(resourcePath, suffix));
        } catch (ParseException e) {
            throw new FreeMarkerTemplateException(e);
        } catch (IOException e) {
            Throwable t = e.getCause();
            if (null != t && t instanceof ParseException) {
                throw new FreeMarkerTemplateException((ParseException) t);
            }
            throw E.ioException(e);
        }
    }

    @Override
    protected Template loadInlineTemplate(String s) {
        stringTemplateLoader.putTemplate(s, s);
        try {
            freemarker.template.Template freemarkerTemplate = stringLoaderConf.getTemplate(s);
            return new FreeMarkerTemplate(freemarkerTemplate);
        } catch (ParseException e) {
            throw new FreeMarkerTemplateException(e);
        } catch (IOException e) {
            Throwable t = e.getCause();
            if (null != t && t instanceof ParseException) {
                throw new FreeMarkerTemplateException((ParseException) t);
            }
            throw E.ioException(e);
        }
    }

    @Override
    protected void init(final App app) {
        conf = new Configuration(Configuration.VERSION_2_3_23);
        conf.setDefaultEncoding("UTF-8");
        conf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        conf.setClassLoaderForTemplateLoading(app.classLoader(), templateHome());
        suffix = app.config().get("view.freemarker.suffix");
        if (null == suffix) {
            suffix = ".ftl";
        } else {
            suffix = suffix.startsWith(".") ? suffix : S.concat(".", suffix);
        }
        initStringConf();
    }

    protected void initStringConf() {
        stringTemplateLoader = new StringTemplateLoader();
        stringLoaderConf = new Configuration(Configuration.VERSION_2_3_23);
        stringLoaderConf.setDefaultEncoding("UTF-8");
        stringLoaderConf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        stringLoaderConf.setTemplateLoader(stringTemplateLoader);
    }

    public List<String> loadContent(String template) {
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

}
