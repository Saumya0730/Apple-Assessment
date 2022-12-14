package com.mysite.core.models;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.FragmentTemplate;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.commons.lang.StringUtils;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ContentFragmentProducts {
    
    public static final String MODEL_TITLE = "Products";

    @Inject @Self
    private Resource resource;

    private Optional<ContentFragment> contentFragment;

    @PostConstruct
    public void init() {
        contentFragment = Optional.ofNullable(resource.adaptTo(ContentFragment.class));
    }

    public String getModelTitle() {
        return contentFragment
            .map(ContentFragment::getTemplate)
            .map(FragmentTemplate::getTitle)
            .orElse(StringUtils.EMPTY);
    }
    public String getName() {
        return contentFragment
            .map(cf -> cf.getElement("productsText"))
            .map(ContentElement::getContent)
            .orElse(StringUtils.EMPTY);
    }

    public String getLink() {
        return contentFragment
            .map(cf -> cf.getElement("productsLink"))
            .map(ContentElement::getContent)
            .orElse(StringUtils.EMPTY);
    }

    public String getUUID() {
        UUID uuid = UUID. randomUUID();
        String uuidAsString = uuid. toString();
        return uuidAsString;
    }

    public List<ContentFragmentProductType> getProductType() {
        return Arrays.asList((String[]) contentFragment
            .map(cf -> cf.getElement("productTypeFields"))
            .map(ContentElement::getValue)
            .map(FragmentData::getValue)
            .orElse(new String[0]))
            .stream()
            .map(productTypePath -> resource.getResourceResolver().resolve(productTypePath))
            .filter(Objects::nonNull)
            .map(productTypeResource -> productTypeResource.adaptTo(ContentFragmentProductType.class))
            .collect(Collectors.toList());
    }
}
