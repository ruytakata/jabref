/*  Copyright (C) 2003-2011 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.exporter.layout.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jabref.exporter.layout.LayoutFormatter;

/**
 * Will interpret two consecutive newlines as the start of a new paragraph and thus
 * wrap the paragraph in HTML-p-tags.
 */
public class HTMLParagraphs implements LayoutFormatter {

    private static Pattern beforeNewLines;


    @Override
    public String format(String fieldText) {

        fieldText = fieldText.trim();

        if (fieldText.isEmpty()) {
            return fieldText;
        }

        if (HTMLParagraphs.beforeNewLines == null) {
            HTMLParagraphs.beforeNewLines = Pattern.compile("(.*?)\\n\\s*\\n");
        }

        Matcher m = HTMLParagraphs.beforeNewLines.matcher(fieldText);
        StringBuffer s = new StringBuffer();
        while (m.find()) {
            String middle = m.group(1).trim();
            if (!middle.isEmpty()) {
                s.append("<p>\n");
                m.appendReplacement(s, m.group(1));
                s.append("\n</p>\n");
            }
        }
        s.append("<p>\n");
        m.appendTail(s);
        s.append("\n</p>");

        return s.toString();
    }
}
