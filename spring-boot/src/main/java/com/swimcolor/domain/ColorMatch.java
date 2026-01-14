package com.swimcolor.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @ToString
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
    private String suitHexColor;

    @Column(length = 7, nullable = false)
    private String capHexColor;

    @Column(nullable = false)
    private Double similarityScore;

    private Integer algorithmVersion;

    @Builder
    private ColorMatch(String swimsuitId, String swimcapId, String suitHexColor, String capHexColor, Double similarityScore, Integer algorithmVersion) {
        this.swimsuitId = swimsuitId;
        this.swimcapId = swimcapId;
        this.suitHexColor = suitHexColor;
        this.capHexColor = capHexColor;
        this.similarityScore = similarityScore;
        this.algorithmVersion = algorithmVersion;
    }
}
