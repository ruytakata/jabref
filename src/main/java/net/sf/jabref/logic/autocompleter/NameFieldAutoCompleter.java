/*  Copyright (C) 2003-2012 JabRef contributors.
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
package net.sf.jabref.logic.autocompleter;

import net.sf.jabref.model.entry.AuthorList;
import net.sf.jabref.model.entry.BibtexEntry;

/**
 * Delivers possible completions for a given string.
 * Interprets the given values as names and stores them in different
 * permutations so we can complete by beginning with last name or first name.
 *
 * @author kahlert, cordes
 */
class NameFieldAutoCompleter extends AbstractAutoCompleter {

    private final String[] fieldNames;
    /**
     * true if only last names should be completed and there is NO separation by " and ", but by " "
     */
    private final boolean lastNameOnlyAndSeparationBySpace;
    private final boolean autoCompFF;
    private final boolean autoCompLF;
    private final AutoCompleteFirstNameMode autoCompFirstnameMode;

    private String prefix = "";


    /**
     * @see AutoCompleterFactory
     */
    NameFieldAutoCompleter(String fieldName, AutoCompletePreferences preferences) {
        this(new String[] {fieldName}, false, preferences);
    }

    public NameFieldAutoCompleter(String[] fieldNames, boolean lastNameOnlyAndSeparationBySpace,
            AutoCompletePreferences preferences) {
        super(preferences);

        this.fieldNames = fieldNames;
        this.lastNameOnlyAndSeparationBySpace = lastNameOnlyAndSeparationBySpace;
        if (preferences.getCompleteFirstLast()) {
            autoCompFF = true;
            autoCompLF = false;
        } else if (preferences.getCompleteLastFirst()) {
            autoCompFF = false;
            autoCompLF = true;
        } else {
            autoCompFF = true;
            autoCompLF = true;
        }
        autoCompFirstnameMode = preferences.getFirstnameMode();
    }

    @Override
    public boolean isSingleUnitField() {
        // quick hack
        // when used at entry fields (!this.lastNameOnlyAndSeparationBySpace), this is a single unit field
        // when used at the search form (this.lastNameOnlyAndSeparationBySpace), this is NOT a single unit field
        // reason: search keywords are separated by space.
        //    This is OK for last names without prefix. "Lastname" works perfectly.
        //    querying for "van der Lastname" can be interpreted as
        //      a) "van" "der" "Lastname"
        //      b) "van der Lastname" (autocompletion lastname)
        return !this.lastNameOnlyAndSeparationBySpace;
    }

    @Override
    public void addBibtexEntry(BibtexEntry entry) {
        if (entry == null) {
            return;
        }
        for (String fieldName : fieldNames) {
            String fieldValue = entry.getField(fieldName);
            if (fieldValue != null) {
                AuthorList authorList = AuthorList.getAuthorList(fieldValue);
                for (int j = 0; j < authorList.size(); j++) {
                    AuthorList.Author author = authorList.getAuthor(j);
                    if (lastNameOnlyAndSeparationBySpace) {
                        addItemToIndex(author.getLastOnly());
                    } else {
                        if (autoCompLF) {
                            switch (autoCompFirstnameMode) {
                            case ONLY_ABBREVIATED:
                                    addItemToIndex(author.getLastFirst(true));
                                    break;
                            case ONLY_FULL:
                                    addItemToIndex(author.getLastFirst(false));
                                    break;
                            case BOTH:
                            default:
                                addItemToIndex(author.getLastFirst(true));
                                addItemToIndex(author.getLastFirst(false));
                            }
                        }
                        if (autoCompFF) {
                            switch (autoCompFirstnameMode) {
                            case ONLY_ABBREVIATED:
                                addItemToIndex(author.getFirstLast(true));
                                break;
                            case ONLY_FULL:
                                addItemToIndex(author.getFirstLast(false));
                                break;
                            case BOTH:
                            default:
                                    addItemToIndex(author.getFirstLast(true));
                                    addItemToIndex(author.getFirstLast(false));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * SIDE EFFECT: sets class variable prefix
     * Delimiter: " and " or " "
     *
     * @return String without prefix
     */
    private String determinePrefixAndReturnRemainder(String str, String delimiter) {
        int index = str.toLowerCase().lastIndexOf(delimiter);
        if (index >= 0) {
            prefix = str.substring(0, index + delimiter.length());
            str = str.substring(index + delimiter.length());
        } else {
            prefix = "";
        }
        return str;
    }

    @Override
    public String[] complete(String toComplete) {
        // Normally, one would implement that using
        // class inheritance. But this seemed overengineered
        if (this.lastNameOnlyAndSeparationBySpace) {
            toComplete = determinePrefixAndReturnRemainder(toComplete, " ");
        } else {
            toComplete = determinePrefixAndReturnRemainder(toComplete, " and ");
        }
        return super.complete(toComplete);
    }

    public String getFieldName() {
        return fieldNames[0];
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

}
