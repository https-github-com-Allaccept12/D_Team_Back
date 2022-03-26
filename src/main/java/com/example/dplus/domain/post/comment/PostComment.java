package com.example.dplus.domain.post.comment;

import com.example.dplus.domain.BaseEntity;
import com.example.dplus.domain.account.Account;
import com.example.dplus.domain.post.Post;
import com.example.dplus.dto.request.PostRequestDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_comment_id")
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "root_comment_id")
//    private PostComment rootComment;
//
//    @OneToMany(mappedBy = "rootComment", cascade = CascadeType.ALL)
//    private List<PostComment> subComment = new ArrayList<>();
//
//    private Integer level;
//
//    private Boolean isDeleted;

    @Builder
    public PostComment(final String content,final Account account,final Post post) {
        this.content = content;
        this.account = account;
        this.post = post;
    }

    public void updateComment(final PostRequestDto.PostComment dto) {
        this.content = dto.getContent();
    }

    public static PostComment of(Account account, Post post, PostRequestDto.PostComment dto) {
        return PostComment.builder()
                .account(account)
                .post(post)
                .content(dto.getContent())
                .build();
    }
}
