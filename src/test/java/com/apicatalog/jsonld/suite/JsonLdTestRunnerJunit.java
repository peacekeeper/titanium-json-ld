package com.apicatalog.jsonld.suite;

import javax.json.JsonValue;

import org.junit.Assert;

import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.api.JsonLdOptions;
import com.apicatalog.jsonld.document.RemoteDocument;
import com.apicatalog.jsonld.json.JsonLdComparison;
import com.apicatalog.jsonld.loader.LoadDocumentOptions;

public class JsonLdTestRunnerJunit {

    private final JsonLdTestCase testCase;
    
    public JsonLdTestRunnerJunit(JsonLdTestCase testCase) {
        this.testCase = testCase;
    }
    
    public void execute(JsonLdTestCaseMethod method) throws JsonLdError {

        Assert.assertNotNull(testCase.baseUri);
        Assert.assertNotNull(testCase.input);

        JsonLdOptions options = testCase.getOptions();
        
        Assert.assertNotNull(options);
        Assert.assertNotNull(options.getDocumentLoader());
        
        JsonValue result = null;
        
        try {
  
            result = method.invoke(options);
            
            Assert.assertNotNull("A result is expected but got null", result);
        
//            Map<String, Object> properties = new HashMap<>(1);
//            properties.put(JsonGenerator.PRETTY_PRINTING, true);
//
//            JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
//
//            JsonWriter jsonWriter2 = writerFactory.createWriter(System.out);
//            jsonWriter2.write(result);
//            jsonWriter2.close();

        } catch (JsonLdError e) {
            Assert.assertEquals(testCase.expectErrorCode, e.getCode());
            return;

        } catch (Exception e) {            
            Assert.fail(e.getMessage());
        }
        
        Assert.assertNull(testCase.expectErrorCode);
        Assert.assertNotNull(testCase.expect);
        
        RemoteDocument expectedDocument = options.getDocumentLoader().loadDocument(testCase.expect, new LoadDocumentOptions());
                    
        Assert.assertNotNull(expectedDocument);
        Assert.assertNotNull(expectedDocument.getDocument());
        
        // compare expected with the result       
        Assert.assertTrue(
                        "Expected " + expectedDocument.getDocument().asJsonStructure() + ", but was" + result,
                        JsonLdComparison.equals(expectedDocument.getDocument().asJsonStructure(), result)
                        );
    }    
}
