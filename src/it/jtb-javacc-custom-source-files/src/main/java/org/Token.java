/* CUSTOMIZED PARSER FILE - MUST NOT BE OVERWRITTEN BY JAVACC */
package org;

public class Token
{

    public int kind;

    public int beginLine, beginColumn, endLine, endColumn;

    public String image;

    public Token next;

    public Token specialToken;

    public Token()
    {
    }

    public Token( int kind )
    {
       this( kind, null );
    }

    public Token( int kind, String image )
    {
       this.kind = kind;
       this.image = image;
    }

    public Object getValue()
    {
        return null;
    }

    public String toString()
    {
       return image;
    }

    public static Token newToken( int ofKind, String image )
    {
        switch ( ofKind )
        {
            default : return new Token( ofKind, image );
        }
    }

    public static Token newToken( int ofKind )
    {
       return newToken( ofKind, null );
    }

}
