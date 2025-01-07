package com.debatetimer.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import java.util.LinkedList;
import java.util.List;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.Snippet;

public class RestDocumentationRequest {

    private final ResourceSnippetParametersBuilder resourceBuilder;
    private final List<Snippet> snippets;

    public RestDocumentationRequest() {
        this.resourceBuilder = new ResourceSnippetParametersBuilder();
        this.snippets = new LinkedList<>();
    }

    public RestDocumentationRequest tag(Tag tag) {
        resourceBuilder.tag(tag.getDisplayName());
        return this;
    }

    public RestDocumentationRequest description(String description) {
        resourceBuilder.description(description)
                .summary(description);
        return this;
    }

    public RestDocumentationRequest pathParameter(ParameterDescriptor... descriptors) {
        snippets.add(pathParameters(descriptors));
        return this;
    }

    public RestDocumentationRequest queryParameter(ParameterDescriptor... descriptors) {
        snippets.add(queryParameters(descriptors));
        return this;
    }

    public RestDocumentationRequest requestHeader(HeaderDescriptor... descriptors) {
        snippets.add(requestHeaders(descriptors));
        return this;
    }

    public RestDocumentationRequest requestBodyField(FieldDescriptor... descriptors) {
        snippets.add(requestFields(descriptors));
        return this;
    }

    public ResourceSnippetParametersBuilder getResourceBuilder() {
        return resourceBuilder;
    }

    public List<Snippet> getSnippets() {
        return List.copyOf(snippets);
    }
}
