package com.example.dplus.domain.artwork.bookmark;


import com.example.dplus.domain.account.Account;
import com.example.dplus.domain.artwork.ArtWorks;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtWorkBookMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artwork_bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id")
    private ArtWorks artWorks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Builder
    public ArtWorkBookMark(final ArtWorks artWorks,final Account account) {
        this.artWorks = artWorks;
        this.account = account;
    }
}
