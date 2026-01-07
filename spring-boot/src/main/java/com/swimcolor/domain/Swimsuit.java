package com.swimcolor.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Swimsuit {

    @Id
    @SwimsuitId
    @Column(length = 10, nullable = false) // SS-0001
    private String id;

    private Long crawlingLogId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String productUrl;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Integer price;

    @ElementCollection
    @CollectionTable(name = "swimsuit_palette", joinColumns = @JoinColumn(name = "swimsuit_id"))
    private List<String> colors = new ArrayList<>();

    @Builder
    public Swimsuit(String id, Long crawlingLogId, String name, String imageUrl, String productUrl, String brand, Integer price, List<String> colors) {
        this.id = id;
        this.crawlingLogId = crawlingLogId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.brand = brand;
        this.price = price;
        this.colors = colors;
    }
}
