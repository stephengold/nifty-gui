package de.lessvoid.nifty.loader.xpp3.elements;


import java.util.Map;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.layout.LayoutPart;
import de.lessvoid.nifty.loader.xpp3.Attributes;
import de.lessvoid.nifty.loader.xpp3.SubstitutionGroup;
import de.lessvoid.nifty.loader.xpp3.XmlElementProcessor;
import de.lessvoid.nifty.loader.xpp3.XmlParser;
import de.lessvoid.nifty.screen.Screen;

/**
 * ElementType.
 * @author void
 */
public class ElementType implements XmlElementProcessor {

  /**
   * process.
   * @param xmlParser XmlParser
   * @param attributes attributes
   * @throws Exception exception
   */
  public void process(final XmlParser xmlParser, final Attributes attributes) throws Exception {
    String id = attributes.get("id");
    String width = attributes.get("width");
    String height = attributes.get("height");
    String align = attributes.get("align");
    String valign = attributes.get("valign");
    String childLayout = attributes.get("childLayout");
    String childClip = attributes.get("childClip");
    String backgroundImage = attributes.get("backgroundImage");
    String backgroundColor = attributes.get("backgroundColor");
    String visibleToMouse = attributes.get("visibleToMouse");
  }

  /**
   * @param xmlParser xmlParser
   * @param nifty nifty
   * @param screen screen
   * @param element panel
   * @param registeredEffectsParam registeredEffectsParam
   * @throws Exception exception
   */
  public static void processChildElements(
      final XmlParser xmlParser,
      final Nifty nifty,
      final Screen screen,
      final Element element,
      final Map < String, Class < ? > > registeredEffectsParam) throws Exception {
    xmlParser.nextTag();
    xmlParser.optional("interact", new InteractType());
    xmlParser.optional("hover", new HoverType(element));
    xmlParser.optional("effect", new EffectsType(nifty, registeredEffectsParam, element));
    xmlParser.zeroOrMore(
          new SubstitutionGroup().
            add("panel", new PanelType(nifty, screen, element, registeredEffectsParam)).
            add("text", new TextType(nifty, screen, element, registeredEffectsParam))
            );
    xmlParser.nextTag();
  }
}