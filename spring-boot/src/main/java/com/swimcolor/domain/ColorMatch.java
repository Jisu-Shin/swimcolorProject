package com.swimcolor.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ColorMatch {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String swimsuitId;

    @Column(length = 10, nullable = false)
    private String swimcapId;

    @Column(length = 7, nullable = false)
    private String swimsuitColorHex;

    @Column(length = 7, nullable = false)
    private String swimcapColorHex;

    @Column(nullable = false)
    private Double similarity;

    @Builder
    private ColorMatch(String swimsuitId, String swimcapId, String swimsuitColorHex, String swimcapColorHex, Double similarity) {
        this.swimsuitId = swimsuitId;
        this.swimcapId = swimcapId;
        this.swimsuitColorHex = swimsuitColorHex;
        this.swimcapColorHex = swimcapColorHex;
        this.similarity = similarity;
    }
}
