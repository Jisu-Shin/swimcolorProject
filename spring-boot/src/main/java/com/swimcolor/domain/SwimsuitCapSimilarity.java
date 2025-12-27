package com.swimcolor.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SwimsuitCapSimilarity {
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
    private Double similarity;

    @Builder
    private SwimsuitCapSimilarity(String swimsuitId, String swimcapId, String suitHexColor, String capHexColor, Double similarity) {
        this.swimsuitId = swimsuitId;
        this.swimcapId = swimcapId;
        this.suitHexColor = suitHexColor;
        this.capHexColor = capHexColor;
        this.similarity = similarity;
    }
}
