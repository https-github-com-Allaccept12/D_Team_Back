package TeamDPlus.code.service.post;

import TeamDPlus.code.advice.ApiRequestException;
import TeamDPlus.code.domain.account.Account;
import TeamDPlus.code.domain.account.follow.FollowRepository;
import TeamDPlus.code.domain.artwork.ArtWorks;
import TeamDPlus.code.domain.artwork.image.ArtWorkImage;
import TeamDPlus.code.domain.post.Post;
import TeamDPlus.code.domain.post.PostRepository;
import TeamDPlus.code.domain.post.answer.PostAnswerRepository;
import TeamDPlus.code.domain.post.bookmark.PostBookMarkRepository;
import TeamDPlus.code.domain.post.comment.PostComment;
import TeamDPlus.code.domain.post.comment.PostCommentRepository;
import TeamDPlus.code.domain.post.image.PostImage;
import TeamDPlus.code.domain.post.image.PostImageRepository;
import TeamDPlus.code.domain.post.like.PostLikesRepository;
import TeamDPlus.code.domain.post.tag.PostTag;
import TeamDPlus.code.domain.post.tag.PostTagRepository;
import TeamDPlus.code.dto.common.CommonDto;
import TeamDPlus.code.dto.request.ArtWorkRequestDto;
import TeamDPlus.code.dto.request.PostRequestDto;
import TeamDPlus.code.dto.response.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostMainPageServiceImpl implements PostMainPageService{

    private final PostRepository postRepository;
    private final PostLikesRepository postLikesRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostImageRepository postImageRepository;
    private final PostBookMarkRepository postBookMarkRepository;
    private final PostTagRepository postTagRepository;
    private final PostAnswerRepository postAnswerRepository;
    private final FollowRepository followRepository;


    // 전체 페이지 (최신순)
    @Transactional(readOnly = true)
    public Page<PostResponseDto.PostPageMain> showPostMain(Long accountId, Long lastPostId) {
        Pageable pageable = PageRequest.of(0,12);
        Page<PostResponseDto.PostPageMain> postList = postRepository.findAllPostOrderByCreatedDesc(lastPostId, pageable);
        setCountList(accountId, postList);
        return postList;
    }

    // 전체 페이지 (좋아요 순)
    @Transactional(readOnly = true)
    public Page<PostResponseDto.PostPageMain> showPostMainByLikes(Long accountId, Long lastPostId) {
        Pageable pageable = PageRequest.of(0,12);
        Page<PostResponseDto.PostPageMain> postList = postRepository.findAllPostOrderByPostLikes(lastPostId, pageable);
        setCountList(accountId, postList);
        return postList;
    }

    // 추천페이지
    @Transactional(readOnly = true)
    public List<PostResponseDto.PostPageMain> showRecommendation(Long accountId, Long postId) {
        List<PostResponseDto.PostPageMain> postList = postRepository.findPostByMostViewAndMostLike();
        postList.forEach((post) -> {
                    Long bookmark_count = postBookMarkRepository.countByPostId(post.getPost_id());
                    Long comment_count = postCommentRepository.countByPostId(post.getPost_id());
                    Long like_count = postLikesRepository.countByPostId(post.getPost_id());
                });
        return postList;
    }

    // 상세 게시글 (디플 - 꿀팁)
    @Transactional(readOnly = true)
    public PostResponseDto.PostDetailPage showPostDetail(Long accountId, Long postId){
        Post post = postValidation(accountId, postId);
        post.addViewCount();
        PostResponseDto.PostSubDetail postSubDetail = postRepository.findByPostSubDetail(accountId, postId);
        List<PostImage> postImageList = postImageRepository.findByPostId(postId);
        List<PostResponseDto.PostComment> postComments = postCommentRepository.findPostCommentByPostId(postId);
        List<PostTag> postTags = postTagRepository.findPostTagsByPostId(postId);
        boolean isLike = postLikesRepository.existByAccountIdAndPostId(accountId, postId);
        boolean isBookmark = postBookMarkRepository.existByAccountIdAndPostId(accountId, postId);
        boolean isFollow = followRepository.existsByFollowerIdAndFollowingId(accountId, postSubDetail.getAccount_id());

//        return PostResponseDto.PostDetailPage.from();
        return null;
    }

    // 게시글 작성
    @Transactional
    public Long createPost(Account account, PostRequestDto.PostCreateAndUpdate dto) {
        Post post = Post.of(account, dto);
        Post savedPost = postRepository.save(post);
        setPostTag(dto.getHashTag(), savedPost);
        setImgUrl(dto.getImg(), savedPost);
        return post.getId();
    }

    // 게시물 수정
    @Transactional
    public Long updatePost(Account account, Long postId, PostRequestDto.PostCreateAndUpdate dto){
        Post post = postValidation(account.getId(), postId);
        post.createAndupdate(dto);
        postImageRepository.deleteAllByPostId(postId);
        setImgUrl(dto.getImg(), post);
        postTagRepository.deleteAllByPostId(postId);
        setPostTag(dto.getHashTag(), post);
        return post.getId();
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long accountId, Long postId){
        Post post = postValidation(accountId, postId);
        postLikesRepository.deleteAllByPostId(postId);
        postTagRepository.deleteAllByPostId(postId);
        postImageRepository.deleteAllByPostId(postId);
        postBookMarkRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
    }

    // 게시글 검색
    @Transactional(readOnly = true)
    public Page<PostResponseDto.PostPageMain> findBySearchKeyWord(String keyword, Long lastArtWorkId) {
        Pageable pageable = PageRequest.of(0,5);
        return postRepository.findPostBySearchKeyWord(keyword,lastArtWorkId,pageable);
    }

    private void setCountList(Long accountId, Page<PostResponseDto.PostPageMain> postList){
        postList.forEach((post) -> {
            Long bookmark_count = postBookMarkRepository.countByPostId(post.getPost_id());
            Long comment_count = postCommentRepository.countByPostId(post.getPost_id());
            Long like_count = postLikesRepository.countByPostId(post.getPost_id());
            post.setCountList(bookmark_count, comment_count, like_count);
        });
    }

    private void setImgUrl(List<CommonDto.ImgUrlDto> dto, Post post) {
        dto.forEach((img) -> {
            PostImage postImage = PostImage.builder()
                    .post(post)
                    .postImg(img.getImg_url())
                    .build();
            postImageRepository.save(postImage);
        });
    }

    // #단위로 끊어서 해쉬태그 들어옴
    private void setPostTag(List<CommonDto.PostTagDto> dto, Post post){
        dto.forEach((tag) -> {
            PostTag postTag = PostTag.builder()
                    .post(post)
                    .hashTag(tag.getHashTag())
                    .build();
            postTagRepository.save(postTag);
        });
    }

    // post 수정, 삭제 권한 확인
    private Post postValidation(Long accountId, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ApiRequestException("해당 게시글은 존재하지 않습니다."));
        if(!post.getAccount().getId().equals(accountId)){
            throw new ApiRequestException("권한이 없습니다.");
        }
        return post;
    }

    // 디모 QnA 상세페이지
    @Transactional(readOnly = true)
    public PostResponseDto.PostAnswerDetailPage detailAnswer(Long accountId, Long postId) {
        // 작품 게시글 존재여부
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiRequestException("해당 게시글은 존재하지 않습니다."));
        // 조회수 + 1
        post.addViewCount();
        // QnA 좋아요 개수와 QnA 기본정보 가져오기
        PostResponseDto.PostAnswerSubDetail postAnswerSubDetail = postRepository.findByPostAnswerSubDetail(postId);
        // 작품 이미지들 가져오기
        List<PostImage> imgList = postImageRepository.findByPostId(postId);
        // 질문 답변 가져오기
        List<PostResponseDto.PostAnswer> postAnswerList = postAnswerRepository.findPostAnswerByPostId(postId);
        // 태그 리스트 가져오기
        List<PostTag> postTagList = postTagRepository.findPostTagsByPostId(postId);
        // 북마크 수
        Long bookMarkCount = postBookMarkRepository.countByPostId(postId);

        boolean isLike = false;
        boolean isBookmark = false;
        boolean isFollow = false;

        if (accountId != null) {
            // 지금 상세페이지를 보고있는 사람이 좋아요를 했는지
            isLike = postLikesRepository.existByAccountIdAndPostId(accountId, postId);
            // 지금 상세페이지를 보고있는 사람이 북마크를 했는지
            isBookmark = postBookMarkRepository.existByAccountIdAndPostId(accountId, postId);
            // 지금 상세페이지를 보고있는 사람이 팔로우를 했는지
            isFollow = followRepository.existsByFollowerIdAndFollowingId(accountId, postAnswerSubDetail.getAccount_id());
        }

        //상세페이지의 답글 개수
        postAnswerSubDetail.setComment_count((long) postAnswerList.size());
        return PostResponseDto.PostAnswerDetailPage.from(imgList, postAnswerList, postTagList, postAnswerSubDetail, isLike, isBookmark, isFollow, bookMarkCount);
    }

    // 디모 QnA 유사한질문
    @Transactional(readOnly = true)
    public PostResponseDto relatedQuestion() {
        return null;
    }
}
