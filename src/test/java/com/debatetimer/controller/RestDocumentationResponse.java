package com.debatetimer.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import java.util.LinkedList;
import java.util.List;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Snippet;

public class RestDocumentationResponse {

    private final List<Snippet> snippets;

    public RestDocumentationResponse() {
        this.snippets = new LinkedList<>();
    }

    public RestDocumentationResponse responseHeader(HeaderDescriptor... descriptors) {
        snippets.add(responseHeaders(descriptors));
        return this;
    }

    public RestDocumentationResponse responseBodyField(FieldDescriptor... descriptors) {
        snippets.add(responseFields(descriptors));
        return this;
    }

    public List<Snippet> getSnippets() {
        return List.copyOf(snippets);
    }
}
