package org.Bairagi.dump.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarvelCharacters {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarvelCharactersName {
    @JsonProperty("name")
    private String name;
    @JsonProperty("powers")
    private List<String> powers;
    }

    @JsonProperty("marvelCharacters")
    private List<MarvelCharactersName> marvelCharacters;
}
