package com.edorasware.one.initializr.metadata;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DependencyDeserializer extends StdDeserializer<Dependency> {


    protected DependencyDeserializer(Class<Dependency> vc) {
        super(vc);
    }

    @Override
    public Dependency deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        Dependency dependency = new Dependency();

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node.get("id") != null) {
            dependency.setId(node.get("id").textValue());
        }
        if (node.get("transients") != null ) {
            List<String> z = new ObjectMapper().readValue(node.get("transients").traverse(), new TypeReference<ArrayList<String>>(){});



            System.out.println(z);
        }

        return dependency;
    }
}
