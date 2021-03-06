package com.apicatalog.jsonld.suite;

import java.net.URI;

import javax.json.JsonObject;

import com.apicatalog.jsonld.api.JsonLdOptions;
import com.apicatalog.jsonld.api.JsonLdOptions.RdfDirection;
import com.apicatalog.jsonld.lang.Version;
import com.apicatalog.jsonld.uri.UriResolver;

public class JsonLdTestCaseOptions {

    public Version specVersion;
    public String base;
    public String processingMode;
    public Boolean normative;
    public String expandContext;
    public Boolean compactArrays;
    public Boolean compactToRelative;
    public String rdfDirection;
    public Boolean produceGeneralizedRdf;
    public Boolean useNativeTypes;
    public Boolean useRdfType;
    
    public static final JsonLdTestCaseOptions of(JsonObject o, String baseUri) {
        
        final JsonLdTestCaseOptions options = new JsonLdTestCaseOptions();
        
        if (o.containsKey("specVersion")) {
            options.specVersion = Version.of(o.getString("specVersion"));
        }
        
        options.base = o.getString("base", null);
        options.processingMode = o.getString("processingMode", null);

        if (o.containsKey("normative")) {
            options.normative = o.getBoolean("normative");
        }

        if (o.containsKey("expandContext")) {
            options.expandContext = UriResolver.resolve(URI.create(baseUri), o.getString("expandContext"));
        }

        if (o.containsKey("compactArrays")) {
            options.compactArrays = o.getBoolean("compactArrays");
        }

        if (o.containsKey("compactToRelative")) {
            options.compactToRelative = o.getBoolean("compactToRelative");
        }
        options.rdfDirection = o.getString("rdfDirection", null);
        
        if (o.containsKey("produceGeneralizedRdf")) {
                options.produceGeneralizedRdf = o.getBoolean("produceGeneralizedRdf");
        }

        if (o.containsKey("useNativeTypes")) {
            options.useNativeTypes = o.getBoolean("useNativeTypes");
        }

        if (o.containsKey("useRdfType")) {
            options.useRdfType = o.getBoolean("useRdfType");
        }

        return options;
    }

    public void setup(JsonLdOptions options) {

        if (processingMode != null) {
            options.setProcessingMode(Version.of(processingMode));
        }
                
        if (base != null) {
            options.setBase(URI.create(base));
        }
        
        if (expandContext != null) {
            options.setExpandContext(URI.create(expandContext));
        }
        
        if (compactArrays != null) {
            options.setCompactArrays(compactArrays);
        }
        
        if (compactToRelative != null) {
            options.setCompactToRelative(compactToRelative);
        }
        
        if (rdfDirection != null) {
            options.setRdfDirection(RdfDirection.valueOf(rdfDirection.toUpperCase().replace("-", "_")));
        }
        
        if (produceGeneralizedRdf != null) {
            options.setProduceGeneralizedRdf(produceGeneralizedRdf);
        }
        
        if (useNativeTypes != null) {
            options.setUseNativeTypes(useNativeTypes);
        }
        
        if (useRdfType != null) {
            options.setUseRdfType(useRdfType);
        }
        
    }
}
