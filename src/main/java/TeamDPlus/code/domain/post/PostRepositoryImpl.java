package TeamDPlus.code.domain.post;

import TeamDPlus.code.domain.post.tag.PostTag;
import TeamDPlus.code.domain.post.tag.QPostTag;
import TeamDPlus.code.dto.response.ArtWorkResponseDto;
import TeamDPlus.code.dto.response.PostResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static TeamDPlus.code.domain.account.QAccount.account;
import static TeamDPlus.code.domain.post.QPost.post;
import static TeamDPlus.code.domain.post.bookmark.QPostBookMark.postBookMark;
import static TeamDPlus.code.domain.post.like.QPostLikes.postLikes;
import static TeamDPlus.code.domain.post.tag.QPostTag.postTag;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    // 전체 페이지
    @Override
    public Page<PostResponseDto.PostPageMain> findAllPostOrderByCreatedDesc(Long lastPostId, Pageable pageable, PostBoard board) {
        List<PostResponseDto.PostPageMain> fetch = queryFactory
                .select(Projections.constructor(PostResponseDto.PostPageMain.class,
                        post.id,
                        account.id,
                        account.nickname,
                        account.profileImg,
                        post.title,
                        post.category,
                        post.content,
                        post.created,
                        post.isSelected
                ))
                .from(post)
                .join(account).on(account.id.eq(post.account.id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .where(isLastPostId(lastPostId), post.board.eq(board))
                .groupBy(post.id)
                .orderBy(post.created.desc())
                .fetch();

        return new PageImpl<>(fetch, pageable, fetch.size());
    }

    // 전체 페이지 (좋아요 순)
    @Override
    public Page<PostResponseDto.PostPageMain> findAllPostOrderByPostLikes(Long lastPostId, Pageable pageable, PostBoard board) {
        List<PostResponseDto.PostPageMain> fetch = queryFactory
                .select(Projections.constructor(PostResponseDto.PostPageMain.class,
                        post.id,
                        account.id,
                        account.nickname,
                        account.profileImg,
                        post.title,
                        post.category,
                        post.content,
                        post.created,
                        post.isSelected
                ))
                .from(post)
                .join(account).on(account.id.eq(post.account.id))
                .leftJoin(postLikes).on(postLikes.id.eq(postLikes.post.id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .where(isLastPostId(lastPostId), post.board.eq(board))
                .orderBy(postLikes.count().desc())
                .fetch();

        return new PageImpl<>(fetch, pageable, fetch.size());
    }

    // 상세페이지 서브 정보
    @Override
    public PostResponseDto.PostSubDetail findByPostSubDetail(Long postId) {
        return queryFactory
                .select(Projections.constructor(PostResponseDto.PostSubDetail.class,
                        Expressions.asNumber(postId).as("post_id"),
                        post.title,
                        post.content,
                        post.view,
                        post.category,
                        post.created,
                        post.modified,
                        account.id,
                        account.profileImg,
                        account.nickname,
                        postBookMark.count(),
                        postLikes.count()
                ))
                .from(post)
                .innerJoin(post.account, account)
                .leftJoin(postBookMark).on(postBookMark.post.eq(post))
                .leftJoin(postLikes).on(postLikes.post.eq(post))
                .where(post.id.eq(postId))
                .fetchOne();
    }

    @Override
    public Page<PostResponseDto.PostPageMain> findPostBySearchKeyWord(String keyword, Long lastPostId, Pageable pageable) {
        List<PostResponseDto.PostPageMain> result = queryFactory
                .select(Projections.constructor(PostResponseDto.PostPageMain.class,
                        post.id,
                        account.id,
                        account.nickname,
                        account.profileImg,
                        post.title,
                        post.content,
                        post.category,
                        post.created,
                        postTag.hashTag
                        ))
                .from(post)
                .join(account).on(account.id.eq(post.account.id))
                .leftJoin(postTag).on(postTag.post.eq(post))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .where(isLastPostId(lastPostId),
                        post.title.contains(keyword),
                        post.account.nickname.contains(keyword),
                        post.content.contains(keyword),
                        postTag.hashTag.contains(keyword))
                .orderBy(post.created.desc())
                .fetch();
        return new PageImpl<>(result,pageable,result.size());
    }

   @Override
    public List<PostResponseDto.PostPageMain> findPostByMostViewAndMostLike() {
        return queryFactory
               .select(Projections.constructor(PostResponseDto.PostPageMain.class,
                       post.id,
                       post.title,
                       post.content,
                       post.view,
                       post.category,
                       post.created,
                       account.id,
                       account.nickname,
                       account.profileImg
               ))
               .from(post)
               .join(post.account, account)
               .leftJoin(postLikes).on(postLikes.post.eq(post))
               .offset(0)
               .limit(10)
               .groupBy(post.id)
               .orderBy(postLikes.count().desc(), post.view.desc())
               .fetch();
    }

    public BooleanExpression isLastPostId(Long lastPostId){
        return lastPostId != 0 ? post.id.lt(lastPostId) : null;
    }
}
