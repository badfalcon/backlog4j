package com.nulabinc.backlog4j.internal.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author nulab-inc
 */
public class JacksonCustomBigdecimalDeserializer extends JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonParser jsonparser,
                                  DeserializationContext deserializationcontext) throws IOException {

        String num = jsonparser.getText();
        if (num == null || num.equals("null")) {
            return null;
        }
        return new BigDecimal(num);
    }

}


