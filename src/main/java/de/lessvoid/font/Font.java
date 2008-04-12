package de.lessvoid.font;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;


/**
 * OpenGL display list based Font.
 * @author void
 */
public class Font {
  /**
   * the logger.
   */
  private static Log log = LogFactory.getLog(Font.class);

  /**
   * the font reader.
   */
  private AngelCodeFont font;

  /**
   * display list id.
   */
  private int listId;

  /**
   * textures.
   */
  private TexData[] textures;

  private int selectionStart;
  
  private int selectionEnd;
  
  private float selectionBackgroundR;
  private float selectionBackgroundG;
  private float selectionBackgroundB;
  private float selectionBackgroundA;

  private float selectionR;
  private float selectionG;
  private float selectionB;
  private float selectionA;

  /**
   * construct the font.
   */
  public Font() {
    selectionStart = -1;
    selectionEnd = -1;
    selectionR = 1.0f;
    selectionG = 0.0f;
    selectionB = 0.0f;
    selectionA = 1.0f;
    selectionBackgroundR = 0.0f;
    selectionBackgroundG = 1.0f;
    selectionBackgroundB = 0.0f;
    selectionBackgroundA = 1.0f;
  }
  
  /**
   * has selection.
   * @return true or false
   */
  private boolean isSelection() {
    return !(selectionStart == -1 && selectionEnd == -1);
  }

  /**
   * init the font with the given filename.
   * @param filename the filename
   * @return true, when success or false on error
   */
  public final boolean init(final String filename) {
    // get the angel code font from file
    font = new AngelCodeFont();
    if (!font.load(filename)) {
      return false;
    }

    // load textures of font into array
    textures = new TexData[font.getTextures().length];
    for (int i = 0; i < font.getTextures().length; i++) {
      textures[i] = new TexData(font.getTextures()[i]);
    }

    // now build open gl display lists for every character in the font
    initDisplayList();
    return true;
  }

  /**
   *
   */
  private void initDisplayList()
  {
    // create new list
    listId= GL11.glGenLists( 256 );
    Tools.checkGLError( "glGenLists" );
    
    // create the list
    for( int i=0; i<256; i++ )
    {
      GL11.glNewList( listId + i, GL11.GL_COMPILE );
      Tools.checkGLError( "glNewList" );
      
      CharacterInfo charInfo= font.getChar( (char)i );
      if( charInfo != null )
      {
        GL11.glBegin( GL11.GL_QUADS );
        Tools.checkGLError( "glBegin" );
    
          float u0 = charInfo.getX() / (float)font.getWidth();
          float v0 = charInfo.getY() / (float)font.getHeight();
          float u1 = ( charInfo.getX() + charInfo.getWidth() ) / (float)font.getWidth();
          float v1 = ( charInfo.getY() + charInfo.getHeight() ) / (float)font.getHeight();
    
          GL11.glTexCoord2f( u0, v0 );
          GL11.glVertex2f( charInfo.getXoffset(), charInfo.getYoffset() );
          
          GL11.glTexCoord2f( u0, v1 );
          GL11.glVertex2f( charInfo.getXoffset(), charInfo.getYoffset() + charInfo.getHeight() );
    
          GL11.glTexCoord2f( u1, v1 );
          GL11.glVertex2f( charInfo.getXoffset() + charInfo.getWidth(), charInfo.getYoffset() + charInfo.getHeight() );
    
          GL11.glTexCoord2f( u1, v0 );
          GL11.glVertex2f( charInfo.getXoffset() + charInfo.getWidth(), charInfo.getYoffset() );
      
        GL11.glEnd();
        Tools.checkGLError( "glEnd" );
      }

      // end list
      GL11.glEndList();
      Tools.checkGLError( "glEndList" );
    }
  }

  /**
   * 
   * @param x
   * @param y
   * @param text
   */
  public void drawString( int x, int y, String text ) {
    enableBlend();
    internalRenderText( x, y, text, 1.0f, false );
    disableBlend();
  }

  public void drawStringWithSize( int x, int y, String text, float size ) {
    enableBlend();
    internalRenderText( x, y, text, size, false );
    disableBlend();
  }

  public void renderWithSizeAndColor( int x, int y, String text, float size, float r, float g, float b, float a ) {
    enableBlend();
    GL11.glColor4f( r, g, b, a );
    internalRenderText( x, y, text, size, true );
    disableBlend();
  }
  
  /**
   * @param xPos
   * @param yPos
   * @param text
   * @param size TODO
   * @param useAlphaTexture TODO
   */
  private void internalRenderText(
      final int xPos,
      final int yPos,
      final String text,
      final float size,
      final boolean useAlphaTexture) {
    GL11.glMatrixMode( GL11.GL_PROJECTION );
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glOrtho( 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(), 0, -9999, 9999 );
    
    GL11.glMatrixMode( GL11.GL_MODELVIEW );
      GL11.glPushMatrix();
      GL11.glLoadIdentity();

      int originalWidth= getStringWidthInternal( text, 1.0f );
      int sizedWidth= getStringWidthInternal( text, size );
      int x = xPos - ( sizedWidth - originalWidth ) / 2;

      int activeTextureIdx = -1;
      for (int i = 0; i < text.length(); i++) {
        char currentc = text.charAt(i);
        char nextc = getNextCharacter(text, i);

        CharacterInfo charInfoC = font.getChar((char) currentc);

        int kerning = 0;
        float characterWidth = 0;
        if (charInfoC != null) {
          int texId = charInfoC.getPage();
          if (activeTextureIdx != texId) {
            activeTextureIdx= texId;
            textures[ activeTextureIdx ].activate(useAlphaTexture);            
          }

          kerning = getKerning(charInfoC, nextc);
          characterWidth = (float)(charInfoC.getXadvance() * size + kerning );
        }

        GL11.glLoadIdentity();
        GL11.glTranslatef( x, yPos, 0.0f );
        
        GL11.glTranslatef( 0.0f, getHeight()/2, 0.0f );
        GL11.glScalef( size, size, 1.0f );
        GL11.glTranslatef( 0.0f, -getHeight()/2, 0.0f );

        boolean characterDone = false;
          if (isSelection()) {
            if (i >= selectionStart && i < selectionEnd) {
              GL11.glPushAttrib(GL11.GL_CURRENT_BIT);

              disableBlend();
              GL11.glDisable(GL11.GL_TEXTURE_2D);
              
              GL11.glColor4f(selectionBackgroundR, selectionBackgroundG, selectionBackgroundB, selectionBackgroundA);
              GL11.glBegin( GL11.GL_QUADS );
              
                GL11.glVertex2i( 0,         0 );
                GL11.glVertex2i( (int)characterWidth, 0 );
                GL11.glVertex2i( (int)characterWidth, 0 + getHeight() );
                GL11.glVertex2i( 0,         0 + getHeight() );
      
              GL11.glEnd();
              
              GL11.glEnable(GL11.GL_TEXTURE_2D);
              enableBlend();
              
              GL11.glColor4f(selectionR, selectionG, selectionB, selectionA);
              GL11.glCallList( listId + currentc );
              Tools.checkGLError( "glCallList" );
              GL11.glPopAttrib();
              
              characterDone= true;
            }
          }
        
        if (!characterDone) {
          GL11.glCallList( listId + currentc );
          Tools.checkGLError( "glCallList" );
        }

        x += characterWidth;
      }

      GL11.glMatrixMode( GL11.GL_PROJECTION );
        GL11.glPopMatrix();

      GL11.glMatrixMode( GL11.GL_MODELVIEW );
        GL11.glPopMatrix();
  }

  /**
   * 
   */
  private void disableBlend() {
    GL11.glDisable( GL11.GL_BLEND );
  }

  /**
   * 
   */
  private void enableBlend() {
    GL11.glEnable( GL11.GL_BLEND );
    GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
  }
  
  public int getStringWidth( String text ) {
    return getStringWidthInternal( text, 1.0f );
  }

  /**
   * @param text
   * @param size TODO
   * @return
   */
  private int getStringWidthInternal(final String text, final float size) {
    int length = 0;

    for (int i=0; i<text.length(); i++) {
      char currentCharacter = text.charAt(i);
      char nextCharacter = getNextCharacter(text, i);

      CharacterInfo currentCharacterInfo = font.getChar((char)currentCharacter);
      if (currentCharacterInfo != null) {
        length += currentCharacterInfo.getXadvance() * size + getKerning(currentCharacterInfo, nextCharacter);
      }
    }
    return length;
  }

  public int getLengthFittingPixelSize(final String text, final int width, final float size) {
    int widthRemaining = width;

    for (int i=0; i<text.length(); i++) {
      char currentCharacter = text.charAt(i);
      char nextCharacter = getNextCharacter(text, i);

      CharacterInfo currentCharacterInfo = font.getChar((char)currentCharacter);
      if (currentCharacterInfo != null) {
        int w = (int) (currentCharacterInfo.getXadvance() * size + getKerning(currentCharacterInfo, nextCharacter));
        widthRemaining -= w;
        if (widthRemaining < 0) {
          // this character will underflow the width. we return the last save index.
          return i;
        }
      }
    }
    return text.length();
  }

  public int getLengthFittingPixelSizeBackwards(final String text, final int width, final float size) {
    int widthRemaining = width;

    for (int i=text.length()-1; i>=0; i--) {
      char currentCharacter = text.charAt(i);
      char prevCharacter = getPrevCharacter(text, i);

      CharacterInfo currentCharacterInfo = font.getChar((char)currentCharacter);
      if (currentCharacterInfo != null) {
        int w = (int) (currentCharacterInfo.getXadvance() * size + getKerning(currentCharacterInfo, prevCharacter));
        widthRemaining -= w;
        if (widthRemaining < 0) {
          // this character will underflow the width. we return the last save index.
          return i;
        }
      }
    }
    return 0;
  }

  public int getIndexFromPixel(final String text, final int pixel, final float size) {
    if (pixel < 0) {
      return -1;
    }

    int current = 0;
    for (int i=0; i<text.length(); i++) {
      char currentCharacter = text.charAt(i);
      char nextCharacter = getNextCharacter(text, i);

      CharacterInfo currentCharacterInfo = font.getChar((char)currentCharacter);
      if (currentCharacterInfo != null) {
        int w = (int) (currentCharacterInfo.getXadvance() * size + getKerning(currentCharacterInfo, nextCharacter));
        if ((pixel >= current) && (pixel <= current + w)) {
          return i;
        }
        current += w;
      }
    }

    return text.length();
  }

  /**
   * @param text
   * @param i
   * @return
   */
  private char getNextCharacter(final String text, int i) {
    char nextc = 0;
    if (i < text.length() - 1) {
      nextc = text.charAt(i+1);
    }
    return nextc;
  }

  /**
   * @param text
   * @param i
   * @return
   */
  private char getPrevCharacter(final String text, int i) {
    char prevc = 0;
    if (i > 0) {
      prevc = text.charAt(i-1);
    }
    return prevc;
  }

  /**
   * @param charInfoC
   * @param nextc
   * @return
   */
  private int getKerning(final CharacterInfo charInfoC, final char nextc) {
    Integer kern = charInfoC.getKerning().get(Character.valueOf(nextc));
    if (kern != null) {
      return kern.intValue();
    }
    return 0;
  }

  public int getHeight()
  {
    return font.getLineHeight();
  }

  public void setSelectionStart(int selectionStart) {
    this.selectionStart = selectionStart;
  }

  public void setSelectionEnd(int selectionEnd) {
    this.selectionEnd = selectionEnd;
  }

  public void setSelectionColor(final float selectionR, final float selectionG, final float selectionB, final float selectionA) {
    this.selectionR = selectionR;
    this.selectionG = selectionG;
    this.selectionB = selectionB;
    this.selectionA = selectionA;
  }
  
  public void setSelectionBackgroundColor(final float selectionR, final float selectionG, final float selectionB, final float selectionA) {
    this.selectionBackgroundR = selectionR;
    this.selectionBackgroundG = selectionG;
    this.selectionBackgroundB = selectionB;
    this.selectionBackgroundA = selectionA;
  }

}
