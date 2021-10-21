package fi.xeno.aquamarine;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class XText {

    /**
     * Create a clickable text button which runs a command
     */
    public static TextComponent commandButton(String coloredText, String hoverText, String clickCommand){

        TextComponent btn = (new TextComponent(TextComponent.fromLegacyText(coloredText)));
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText))));
        btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));

        return btn;

    }

    /**
     * Create a clickable text button which suggests a command
     */
    public static TextComponent commandSuggestButton(String coloredText, String hoverText, String clickCommand){

        TextComponent btn = (new TextComponent(TextComponent.fromLegacyText(coloredText)));
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText))));
        btn.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));

        return btn;

    }

    /**
     * Create a clickable text button which suggests a command
     */
    public static TextComponent hoverText(String coloredText, String hoverText){

        TextComponent btn = (new TextComponent(TextComponent.fromLegacyText(coloredText)));
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText))));

        return btn;

    }

    /**
     * Create a clickable text button which copies text to clipboard
     */
    public static TextComponent copyButton(String coloredText, String hoverText, String clickCopy){

        TextComponent btn = (new TextComponent(TextComponent.fromLegacyText(coloredText)));
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText))));
        btn.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clickCopy));

        return btn;

    }

    /**
     * Create a clickable text button which opens a URL address
     */
    public static TextComponent linkButton(String coloredText, String hoverText, String url) {

        TextComponent btn = (new TextComponent(TextComponent.fromLegacyText(coloredText)));
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText))));
        btn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        return btn;

    }

    /**
     * Shorthand for TextComponent::fromLegacyText
     */
    public static TextComponent t(String coloredText) {
        return new TextComponent(TextComponent.fromLegacyText(coloredText));
    }

    /**
     * Split a string into rows
     */
    public static String wordWrap(String input, int charsPerLine) {
        
        StringBuilder out = new StringBuilder();
        
        String[] words = input.split(" ");
        int line = 0;
        
        for (String s:words) {
            
            out.append(s).append(' ');
            line += s.length();
            
            if (line >= charsPerLine) {
                out.append('\n');
                line = 0;
            }
            
        }
        
        return out.toString();
        
    }


}
