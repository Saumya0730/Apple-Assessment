package com.mysite.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.core.models.ContentFragmentCategory;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import org.osgi.service.component.annotations.Component;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.Query;
import java.util.*;
import java.util.stream.Collectors;
import javax.jcr.RepositoryException;
import com.day.cq.search.result.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service=Servlet.class,
           property={
                   Constants.SERVICE_DESCRIPTION + "=Category Servlet",
                   "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                   "sling.servlet.paths="+ "/bin/fragmentexamples/categories",
                   "sling.servlet.extensions=" + "json"
           })
public class CategoryServlet extends SlingSafeMethodsServlet {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryServlet.class);
    private static final long serialVersionUID = 1L;
    private static final List<String> reservedParams = Arrays.asList( "search" );
    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp)
                         throws ServletException, IOException {
        final QueryBuilder queryBuilder = req.getResourceResolver().adaptTo(QueryBuilder.class);
        final Map<String, String> map = new HashMap<String, String>();
        map.put("type", "dam:Asset");
        map.put("path", "/content/dam");
        map.put("boolproperty", "jcr:content/contentFragment");
        map.put("boolproperty.value", "true");
        map.put("property", "jcr:content/data/cq:model");
        map.put("property.value", "/conf/content-fragment/settings/dam/cfm/models/categories");
        int paramCount = 1;
        for (final String key : req.getParameterMap().keySet()) {
            paramCount++;
            if (! reservedParams.contains(key)) {
                map.put(paramCount + "_property", "jcr:content/data/master/" + key);
                map.put(paramCount + "_property.value", req.getParameter(key));
            }
        }
        final Query query = queryBuilder.createQuery(PredicateGroup.create(map), req.getResourceResolver().adaptTo(Session.class));
        final SearchResult result = query.getResult();
        final ObjectMapper objectMapper = new ObjectMapper();
        final List<ContentFragmentCategory> categoryList = result.getHits().stream()
        .map(hit -> {
            try {
                return req.getResourceResolver().resolve(hit.getPath()).adaptTo(ContentFragmentCategory.class);
            } catch (RepositoryException e) {
                log.error("Error collecting search results", e);
                return null;
            }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
        resp.setContentType("application/json");
        try {
            resp.getWriter().write(objectMapper.writeValueAsString(categoryList));
        } catch (JsonProcessingException e) {
            resp.getWriter().write("{ \"error\": \"Could not write categories as JSON\" }");
        }
    }
}
