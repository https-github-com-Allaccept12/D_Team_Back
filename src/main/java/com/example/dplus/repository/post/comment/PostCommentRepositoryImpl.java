package com.example.dplus.repository.post.comment;

import com.example.dplus.dto.response.AccountResponseDto;
import com.example.dplus.dto.response.PostResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example.dplus.domain.account.QAccount.account;
import static com.example.dplus.domain.post.QPost.post;
import static com.example.dplus.domain.post.QPostComment.postComment;
import static com.example.dplus.domain.post.QPostCommentLikes.postCommentLikes;

@RequiredArgsConstructor
public class PostCommentRepositoryImpl implements PostCommentRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PostResponseDto.PostComment> findPostCommentByPostId(Long postId) {
        return jpaQueryFactory
                .select(Projections.constructor(PostResponseDto.PostComment.class,
                        postComment.account.id,
                        postComment.account.nickname,
                        postComment.account.profileImg,
                        postComment.id,
                        postComment.content,
                        postComment.modified,
                        postCommentLikes.count()
                ))
                .from(postComment)
                .leftJoin(postCommentLikes).on(postComment.id.eq(postCommentLikes.postComment.id))
                .where(postComment.post.id.eq(postId))
                .groupBy(postComment.id)
                .orderBy(postCommentLikes.count().desc())
                .fetch();
    }

    // 나의 댓글
    @Override
    public List<AccountResponseDto.MyComment> findPostCommentByAccountId(Long accountId, Pageable pageable) {
        return jpaQueryFactory
                .select(Projections.constructor(AccountResponseDto.MyComment.class,
                        postComment.id,
                        postComment.content,
                        postCommentLikes.count(),
                        postComment.created,
                        postComment.modified,
                        account.profileImg,
                        post.id
                ))
                .from(postComment)
                .join(postComment.account, account).on(account.id.eq(accountId))
                .join(postComment.post, post).on(post.account.id.eq(accountId))
                .leftJoin(postCommentLikes).on(postComment.id.eq(postCommentLikes.postComment.id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .groupBy(postComment.id)
                .orderBy(postCommentLikes.count().desc())
                .fetch();
    }
}
