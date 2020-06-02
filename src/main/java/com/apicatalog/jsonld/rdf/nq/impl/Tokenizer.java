package com.apicatalog.jsonld.rdf.nq.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * 
 * @see <a href="https://www.w3.org/TR/n-quads/#sec-grammar">N-Quads Grammar</a>
 *
 */
class Tokenizer {

    private final Reader reader;
    
    private Token next;
    
    protected Tokenizer(Reader reader) {
        this.reader = new BufferedReader(reader, 8192); //TODO magic constant
        this.next = null;
    }
    
    public Token next() throws NQuadsReaderError {
        
        if (!hasNext()) {
            return next;
        }
        
        next = doRead();
        return next;
    }
    
    public Token token() throws NQuadsReaderError {        
        hasNext();
        return next;
    }
    
    public boolean accept(TokenType type) throws NQuadsReaderError {
        if (type == token().getType()) {
            next();
            return true;
        }
        return false;
    }
            
    
    private Token doRead() throws NQuadsReaderError {
        
        try {
            int ch = reader.read();
            
            if (ch == -1) {
                return Token.EOI;
            }
            
            // WS
            if (Terminal.isWhitespace(ch)) {                
                return skipWhitespaces();
                
            }
            
            if (ch == '<') {
                return readIriRef();
            }

            if (ch == '"') {
                return readString();
            }
            
            if (ch == '.') {
                return Token.EOS;
            }
            
            if (Terminal.isEol(ch)) {
                return skipEol();
            }
            
            if (ch == '@') {
                return readLangTag();
            }
            
            if (ch == '_') {
                return readBlankNode();
            }

            unexpected(ch);
//
//            } else if (ch == '@') {
//                
//                readLangTag();
//
//            } else if (ch == '^') {
//
//                if ('^' != reader.read()) {
//                    throw new NQuadsReaderError();
//                }
//                
//                lastType = NQuadsTokenType.LITERAL_ATTR;
//                
//            } else if (ch == '_') {
//                
//                readBlankNode();
//
            
        } catch (IOException e) {
            throw new NQuadsReaderError(e);
        }
        
        throw new IllegalStateException();
    }
    
    private void unexpected(int ch) throws NQuadsReaderError {
        throw new NQuadsReaderError(ch != -1 ? "Unexpected character '" + (char)ch + "'." : "Unexpected end of input.");
    }
    
    private Token skipWhitespaces() throws NQuadsReaderError {

        try {
            reader.mark(1);
            int ch = reader.read();
            
            while (Terminal.isWhitespace(ch)) {
                reader.mark(1);
                ch = reader.read();
            }
            
            reader.reset();

            return Token.WS;
            
        } catch (IOException e) {
            throw new NQuadsReaderError(e);
        }
    }

    private Token skipEol() throws NQuadsReaderError {

        try {
            reader.mark(1);
            int ch = reader.read();
            
            while (Terminal.isEol(ch)) {
                reader.mark(1);
                ch = reader.read();
            }
            
            reader.reset();

            return Token.EOL;
            
        } catch (IOException e) {
            throw new NQuadsReaderError(e);
        }
    }

    private Token readIriRef() throws NQuadsReaderError {

        try {

            StringBuilder value = new StringBuilder();
            
            int ch = reader.read();
            
            while (ch != '>'  && ch != -1) {
                
                if ((0x00 <= ch && ch <= 0x20)
                     || ch == '<'
                     || ch == '"'
                     || ch == '{'
                     || ch == '}'
                     || ch == '|'
                     || ch == '^'
                     || ch == '`'
                        ) {
                    unexpected(ch);
                }
                
                if (ch == '\\') {
                    //TODO unicode
                    unexpected(ch);
                }
                
                value.append((char)ch);
                ch = reader.read();
            }
            
            if (ch == -1) {
                unexpected(ch);
            }

            return new Token(TokenType.IRI_REF, value.toString());
            
        } catch (IOException e) {
            throw new NQuadsReaderError(e);
        }        
    }
    
    private Token readString() throws NQuadsReaderError {
        try {

            StringBuilder value = new StringBuilder();
            
            int ch = reader.read();
            
            boolean escape = false;
            
            while ((escape || ch != '"')  && ch != -1) {
                
                if (ch == 0x22
                     || ch == 0x5c
                     || ch == 0xa
                     || ch == 0xd
                        ) {
                    unexpected(ch);
                }
                
                if (ch == '\\') {
                    //TODO unicode & escape
                    unexpected(ch);
                }
                
                value.append((char)ch);
                ch = reader.read();
            }
            
            if (ch == -1) {
                unexpected(ch);
            }

            return new Token(TokenType.STRING_LITERAL_QUOTE, value.toString());
            
        } catch (IOException e) {
            throw new NQuadsReaderError(e);
        }    
    }

    private Token readLangTag() throws NQuadsReaderError {
        try {

            StringBuilder value = new StringBuilder();
            
            int ch = reader.read();
            
            if (!Terminal.isAsciiAlpha(ch)) {
                unexpected(ch);
            }
            value.append((char)ch);
            
            reader.mark(1);
            ch = reader.read();
            
            while (Terminal.isAsciiAlpha(ch)) {
                
                value.append((char)ch);
                
                reader.mark(1);
                ch = reader.read();
            }
            
            if (ch == -1) {
                unexpected(ch);
            }
            
            boolean delim = false;
            
            while (Terminal.isAsciiAlphaNum(ch) || ch == '-') {

                value.append((char)ch);
                
                reader.mark(1);
                ch = reader.read();

                delim = ch == '-';
            }
            
            if (ch == -1 || delim) {
                unexpected(ch);
            }
            
            reader.reset();

            return new Token(TokenType.LANGTAG, value.toString());
            
        } catch (IOException e) {
            throw new NQuadsReaderError(e);
        }    
    }

    private Token readBlankNode() throws NQuadsReaderError {
        try {

            StringBuilder value = new StringBuilder();
            
            int ch = reader.read();
            
            if (ch != ':') {
                unexpected(ch);
            }
            
            reader.mark(1);
            ch = reader.read();
            
            while (Terminal.isPnCharsU(ch) || Terminal.isDigit(ch)) {
                
                value.append((char)ch);
                
                reader.mark(1);
                ch = reader.read();
            }
            
            if (ch == -1) {
                unexpected(ch);
            }
            
            boolean delim = false;
            
            while (Terminal.isPnChars(ch) || ch == '.') {

                value.append((char)ch);
                
                reader.mark(1);
                ch = reader.read();

                delim = ch == '.';
            }
            
            if (ch == -1 || delim) {
                unexpected(ch);
            }

            return new Token(TokenType.BLANK_NODE_LABEL, value.toString());
            
        } catch (IOException e) {
            throw new NQuadsReaderError(e);
        }    
    }

    
    public boolean hasNext() throws NQuadsReaderError {
        if (next == null) {
            next = doRead();
        }
        return TokenType.END_OF_INPUT != next.getType();
    }
    
    protected static class Token {
        
        static final Token EOI = new Token(TokenType.END_OF_INPUT, null);
        static final Token EOS = new Token(TokenType.END_OF_STATEMENT, null);
        static final Token EOL = new Token(TokenType.END_OF_LINE, null);
        static final Token WS = new Token(TokenType.WHITE_SPACE, null);
        
        final TokenType type;
        final String value;
        
        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
        
        TokenType getType() {
            return type;
        }

        String getValue() {
            return value;
        }
    }
    
    protected enum TokenType {
        LANGTAG,
        IRI_REF,
        STRING_LITERAL_QUOTE,
        BLANK_NODE_LABEL,
        WHITE_SPACE,
        LITERAL_ATTR,
        END_OF_STATEMENT,
        END_OF_LINE,
        END_OF_INPUT,
    }
}
