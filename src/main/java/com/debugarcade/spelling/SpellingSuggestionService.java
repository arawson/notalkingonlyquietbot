
package com.debugarcade.spelling;

import java.util.List;

/**
 *
 * @author arawson
 */
public interface SpellingSuggestionService {
    String getBest(String input);
    
    List<String> getSuggestions();
}
