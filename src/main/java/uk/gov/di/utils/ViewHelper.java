package uk.gov.di.utils;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.Map;

public class ViewHelper {
    public static String render(Map model, String templatePath) {
        return new MustacheTemplateEngine().render(new ModelAndView(model, templatePath));
    }
}
