package com.nulabinc.backlog4j.internal.http;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class MimeHelper {

    private static final String DISPOSITION_FILENAME = "filename";

    private static final String MIME_SPECIALS = "()<>@,;:\\\"/[]?=" + "\t ";

    private static final String WHITE = " \t\n\r";

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private static final byte[] HEX_DECODE = new byte[0x80];

    static {
        for (int i = 0; i < HEX_DIGITS.length; i++) {
            HEX_DECODE[HEX_DIGITS[i]] = (byte) i;
            HEX_DECODE[Character.toLowerCase(HEX_DIGITS[i])] = (byte) i;
        }
    }

    private MimeHelper() {
    }

    /**
     * Decodes a filename from the Content-Disposition header value according to
     * RFC 2183 and RFC 2231.
     * <p/>
     * See <a href="http://tools.ietf.org/html/rfc2231">RFC 2231</a> for
     * details.
     *
     * @param value the header value to decode
     * @return the filename
     */
    public static String decodeContentDispositionFilename(String value) {
        Map<String, String> params = new HashMap<>();
        decodeContentDisposition(value, params);
        return params.get(DISPOSITION_FILENAME);
    }

    /**
     * Decodes the Content-Disposition header value according to RFC 2183 and
     * RFC 2231.
     * <p/>
     * Does not deal with continuation lines.
     * <p/>
     * See <a href="http://tools.ietf.org/html/rfc2231">RFC 2231</a> for
     * details.
     *
     * @param value  the header value to decode
     * @param params the map of parameters to fill
     * @return the disposition
     */
    public static String decodeContentDisposition(String value, Map<String, String> params) {
        try {
            HeaderTokenizer tokenizer = new HeaderTokenizer(value);
            // get the first token, which must be an ATOM
            Token token = tokenizer.next();
            if (token.getType() != Token.ATOM) {
                return null;
            }
            String disposition = token.getValue();
            // value ignored in this method

            // the remainder is the parameters
            String remainder = tokenizer.getRemainder();
            if (remainder != null) {
                getParameters(remainder, params);
            }
            return disposition;
        } catch (ParseException e) {
            return null;
        }
    }

    protected static class ParseException extends Exception {
        private static final long serialVersionUID = 1L;

        public ParseException() {
            super();
        }

        public ParseException(String message) {
            super(message);
        }
    }

    /*
     * From geronimo-javamail_1.4_spec-1.7.1. Token
     */
    protected static class Token {
        // Constant values from J2SE 1.4 API Docs (Constant values)
        public static final int ATOM = -1;

        public static final int COMMENT = -3;

        public static final int EOF = -4;

        public static final int QUOTEDSTRING = -2;

        private final int type;

        private final String value;

        public Token(int type, String value) {
            this.type = type;
            this.value = value;
        }

        public int getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    /*
     * Tweaked from geronimo-javamail_1.4_spec-1.7.1. HeaderTokenizer
     */
    protected static class HeaderTokenizer {

        private static final Token EOF = new Token(Token.EOF, null);

        private final String header;

        private final String delimiters;

        private final boolean skipComments;

        private int pos;

        public HeaderTokenizer(String header) {
            this(header, MIME_SPECIALS, true);
        }

        protected HeaderTokenizer(String header, String delimiters, boolean skipComments) {
            this.header = header;
            this.delimiters = delimiters;
            this.skipComments = skipComments;
        }

        public String getRemainder() {
            return header.substring(pos);
        }

        public Token next() throws ParseException {
            return readToken();
        }

        /**
         * Read an ATOM token from the parsed header.
         *
         * @return A token containing the value of the atom token.
         */
        private Token readAtomicToken() {
            // skip to next delimiter
            int start = pos;
            while (++pos < header.length()) {
                // break on the first non-atom character.
                char ch = header.charAt(pos);
                if (delimiters.indexOf(header.charAt(pos)) != -1 || ch < 32 || ch >= 127) {
                    break;
                }
            }
            return new Token(Token.ATOM, header.substring(start, pos));
        }

        /**
         * Read the next token from the header.
         *
         * @return The next token from the header. White space is skipped, and
         * comment tokens are also skipped if indicated.
         */
        private Token readToken() throws ParseException {
            if (pos >= header.length()) {
                return EOF;
            } else {
                char c = header.charAt(pos);
                // comment token...read and skip over this
                if (c == '(') {
                    Token comment = readComment();
                    if (skipComments) {
                        return readToken();
                    } else {
                        return comment;
                    }
                    // quoted literal
                } else if (c == '\"') {
                    return readQuotedString();
                    // white space, eat this and find a real token.
                } else if (WHITE.indexOf(c) != -1) {
                    eatWhiteSpace();
                    return readToken();
                    // either a CTL or special. These characters have a
                    // self-defining token type.
                } else if (c < 32 || c >= 127 || delimiters.indexOf(c) != -1) {
                    pos++;
                    return new Token(c, String.valueOf(c));
                } else {
                    // start of an atom, parse it off.
                    return readAtomicToken();
                }
            }
        }

        /**
         * Extract a substring from the header string and apply any
         * escaping/folding rules to the string.
         *
         * @param start The starting offset in the header.
         * @param end   The header end offset + 1.
         * @return The processed string value.
         */
        private String getEscapedValue(int start, int end) throws ParseException {
            StringBuilder value = new StringBuilder();
            for (int i = start; i < end; i++) {
                char ch = header.charAt(i);
                // is this an escape character?
                if (ch == '\\') {
                    i++;
                    if (i == end) {
                        throw new ParseException("Invalid escape character");
                    }
                    value.append(header.charAt(i));
                } else if (ch == '\r') {
                    // line breaks are ignored, except for naked '\n'
                    // characters, which are consider parts of linear
                    // whitespace.
                    // see if this is a CRLF sequence, and skip the second if it
                    // is.
                    if (i < end - 1 && header.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    // just append the ch value.
                    value.append(ch);
                }
            }
            return value.toString();
        }

        /**
         * Read a comment from the header, applying nesting and escape rules to
         * the content.
         *
         * @return A comment token with the token value.
         */
        private Token readComment() throws ParseException {
            int start = pos + 1;
            int nesting = 1;
            boolean requiresEscaping = false;
            // skip to end of comment/string
            while (++pos < header.length()) {
                char ch = header.charAt(pos);
                if (ch == ')') {
                    nesting--;
                    if (nesting == 0) {
                        break;
                    }
                } else if (ch == '(') {
                    nesting++;
                } else if (ch == '\\') {
                    pos++;
                    requiresEscaping = true;
                } else if (ch == '\r') {
                    // we need to process line breaks also
                    requiresEscaping = true;
                }
            }
            if (nesting != 0) {
                throw new ParseException("Unbalanced comments");
            }
            String value;
            if (requiresEscaping) {
                value = getEscapedValue(start, pos);
            } else {
                value = header.substring(start, pos++);
            }
            return new Token(Token.COMMENT, value);
        }

        /**
         * Parse out a quoted string from the header, applying escaping rules to
         * the value.
         *
         * @return The QUOTEDSTRING token with the value.
         * @throws ParseException
         */
        private Token readQuotedString() throws ParseException {
            int start = pos + 1;
            boolean requiresEscaping = false;
            // skip to end of comment/string
            while (++pos < header.length()) {
                char ch = header.charAt(pos);
                if (ch == '"') {
                    String value;
                    if (requiresEscaping) {
                        value = getEscapedValue(start, pos++);
                    } else {
                        value = header.substring(start, pos++);
                    }
                    return new Token(Token.QUOTEDSTRING, value);
                } else if (ch == '\\') {
                    pos++;
                    requiresEscaping = true;
                } else if (ch == '\r') {
                    // we need to process line breaks also
                    requiresEscaping = true;
                }
            }
            throw new ParseException("Missing '\"'");
        }

        /**
         * Skip white space in the token string.
         */
        private void eatWhiteSpace() {
            // skip to end of whitespace
            while (++pos < header.length() && WHITE.indexOf(header.charAt(pos)) != -1) {
                // just read
            }
        }
    }

    /*
     * Tweaked from geronimo-javamail_1.4_spec-1.7.1. ParameterList
     */
    private static Map<String, String> getParameters(String list, Map<String, String> params) throws ParseException {
        HeaderTokenizer tokenizer = new HeaderTokenizer(list);
        while (true) {
            Token token = tokenizer.next();
            switch (token.getType()) {
                case Token.EOF:
                    // the EOF token terminates parsing.
                    return params;

                case ';':
                    // each new parameter is separated by a semicolon, including
                    // the first, which separates
                    // the parameters from the main part of the header.
                    // the next token needs to be a parameter name
                    token = tokenizer.next();
                    // allow a trailing semicolon on the parameters.
                    if (token.getType() == Token.EOF) {
                        return params;
                    }

                    if (token.getType() != Token.ATOM) {
                        throw new ParseException("Invalid parameter name: " + token.getValue());
                    }

                    // get the parameter name as a lower case version for better
                    // mapping.
                    String name = token.getValue().toLowerCase(Locale.ENGLISH);

                    token = tokenizer.next();

                    // parameters are name=value, so we must have the "=" here.
                    if (token.getType() != '=') {
                        throw new ParseException("Missing '='");
                    }

                    // now the value, which may be an atom or a literal
                    token = tokenizer.next();

                    if (token.getType() != Token.ATOM && token.getType() != Token.QUOTEDSTRING) {
                        throw new ParseException("Invalid parameter value: " + token.getValue());
                    }

                    String value = token.getValue();

                    // we might have to do some additional decoding. A name that
                    // ends with "*" is marked as being encoded, so if requested, we
                    // decode the value.
                    if (name.endsWith("*")) {
                        name = name.substring(0, name.length() - 1);
                        value = decodeRFC2231value(value);
                    }
                    params.put(name, value);
                    break;
                default:
                    throw new ParseException("Missing ';'");
            }
        }
    }

    private static String decodeRFC2231value(String value) {
        int q1 = value.indexOf('\'');
        if (q1 == -1) {
            // missing charset
            return value;
        }
        String mimeCharset = value.substring(0, q1);
        int q2 = value.indexOf('\'', q1 + 1);
        if (q2 == -1) {
            // missing language
            return value;
        }
        byte[] bytes = fromHex(value.substring(q2 + 1));
        try {
            return new String(bytes, Charset.forName(mimeCharset));
        } catch (UnsupportedCharsetException e) {
            // incorrect encoding
            return value;
        }
    }

    private static byte[] fromHex(String data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length());
        for (int i = 0; i < data.length(); ) {
            char c = data.charAt(i++);
            if (c == '%') {
                if (i > data.length() - 2) {
                    break; // unterminated sequence
                }
                byte b1 = HEX_DECODE[data.charAt(i++) & 0x7f];
                byte b2 = HEX_DECODE[data.charAt(i++) & 0x7f];
                out.write((b1 << 4) | b2);
            } else {
                out.write((byte) c);
            }
        }
        return out.toByteArray();
    }

}