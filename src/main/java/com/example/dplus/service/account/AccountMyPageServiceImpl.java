package com.example.dplus.service.account;

import com.example.dplus.advice.ErrorCode;
import com.example.dplus.advice.ErrorCustomException;
import com.example.dplus.domain.account.Account;
import com.example.dplus.domain.account.History;
import com.example.dplus.domain.artwork.ArtWorks;
import com.example.dplus.dto.request.AccountRequestDto.UpdateAccountIntro;
import com.example.dplus.dto.request.AccountRequestDto.UpdateSpecialty;
import com.example.dplus.dto.request.HistoryRequestDto.HistoryUpdateList;
import com.example.dplus.dto.response.AccountResponseDto.AccountInfo;
import com.example.dplus.dto.response.AccountResponseDto.MyAnswer;
import com.example.dplus.dto.response.AccountResponseDto.MyComment;
import com.example.dplus.dto.response.AccountResponseDto.MyPost;
import com.example.dplus.dto.response.ArtWorkResponseDto;
import com.example.dplus.dto.response.ArtWorkResponseDto.MyArtWork;
import com.example.dplus.dto.response.HistoryResponseDto;
import com.example.dplus.repository.account.AccountRepository;
import com.example.dplus.repository.account.follow.FollowRepository;
import com.example.dplus.repository.account.history.HistoryRepository;
import com.example.dplus.repository.artwork.ArtWorkRepository;
import com.example.dplus.repository.post.PostRepository;
import com.example.dplus.repository.post.answer.PostAnswerRepository;
import com.example.dplus.repository.post.comment.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountMyPageServiceImpl implements AccountMyPageService {

    private final AccountRepository accountRepository;
    private final HistoryRepository historyRepository;
    private final ArtWorkRepository artWorkRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PostAnswerRepository postAnswerRepository;
    private final PostCommentRepository postCommentRepository;

    //마이페이지 조회
    @Transactional(readOnly = true)
    public AccountInfo showAccountInfo(Long visitAccountId, Long accountId) {
        final Account findAccount = getAccount(visitAccountId);
        final Long follower = followRepository.countByFollowerId(findAccount.getId());
        final Long following = followRepository.countByFollowingId(findAccount.getId());
        final boolean isFollow= followRepository.existsByFollowerIdAndFollowingId(visitAccountId,accountId);
        return AccountInfo.from(findAccount,follower,following, isFollow,visitAccountId.equals(accountId));
    }
    //연혁 조히
    @Transactional(readOnly = true)
    public List<HistoryResponseDto.History> showAccountHistory(Long accountId) {
        final List<History> history = historyRepository.findAllByAccountId(accountId);
        return history.stream()
                .map(HistoryResponseDto.History::new)
                .collect(Collectors.toList());
    }
    //작업물 - 포트폴리오
    @Transactional(readOnly = true)
    public List<ArtWorkResponseDto.ArtWorkFeed> showAccountCareerFeed(Long visitAccountId) {
        return artWorkRepository.findByMasterArtWorkImageAndAccountId(visitAccountId);
    }
    //포트폴리오 - 기본 소개 수정
    @Transactional
    public void updateAccountIntro(UpdateAccountIntro dto, Long accountId) {
        Account account = getAccount(accountId);
        account.updateIntro(dto);
    }

    //포트폴리오 - 스킬셋 수정
    @Transactional
    public void updateAccountSpecialty(UpdateSpecialty dto, Long accountId) {
        Account account = getAccount(accountId);
        account.updateSpecialty(dto);
    }

    // 히스토리 수정
    @Transactional
    public void updateAccountHistory(HistoryUpdateList dto, Long accountId) {
        //히스토리 전체 삭제 벌크
        Account account = getAccount(accountId);
        historyRepository.deleteAllByAccountId(account.getId());
        //다시셋팅
        List<History> collect = dto.getHistory().stream()
                .map(data -> History.toEntity(data,account))
                .collect(Collectors.toList());
        historyRepository.saveAll(collect);
    }

    //내 작품탭에서 대표작으로 올리기
    @Transactional
    public void masterAccountCareerFeed(Long artWorkId,Account account) {
        ArtWorks artWorks = getArtWorks(artWorkId);
        createValid(account, artWorks);
        artWorks.updateArtWorkIsMaster(true);
        artWorks.updateArtWorkIsScope(true);
    }
    //내 대표작에서 내리기
    @Transactional
    public void nonMasterAccountCareerFeed(Long artWorkId,Account account) {
        ArtWorks artWorks = getArtWorks(artWorkId);
        createValid(account, artWorks);
        artWorks.updateArtWorkIsMaster(false);
    }

    // 내 대표작 수정
    @Transactional
    public void updateMasterAccountCareerFeed(Long artWorkId,Long prevArtWorkId,Account account) {
        ArtWorks artWorks = getArtWorks(artWorkId);
        ArtWorks prevArtWork = getArtWorks(prevArtWorkId);
        createValid(account, artWorks);
        artWorks.updateArtWorkIsMaster(true);
        prevArtWork.updateArtWorkIsMaster(false);
    }
    //작품 보이기
    @Transactional
    public void nonHideArtWorkScope(Long artWorkId, Account account) {
        ArtWorks artWorks = getArtWorks(artWorkId);
        createValid(account,artWorks);
        artWorks.updateArtWorkIsScope(true);
    }
    //작품 숨김
    @Transactional
    public void hideArtWorkScope(Long artWorkId, Account account) {
        ArtWorks artWorks = getArtWorks(artWorkId);
        createValid(account,artWorks);
        artWorks.updateArtWorkIsScope(false);
        artWorks.updateArtWorkIsMaster(false);
    }

    //마이페이지/유저작품
    @Transactional(readOnly = true)
    public List<MyArtWork> showAccountArtWork(final Long lastArtWorkId, final Long visitAccountId, final Long accountId) {
        Pageable pageable = PageRequest.of(0,5);
        return artWorkRepository.findByArtWork(lastArtWorkId, pageable, visitAccountId, accountId);
    }

    //마이페이지/북마크
    @Transactional(readOnly = true)
    public List<ArtWorkResponseDto.ArtWorkBookMark> showAccountArtWorkBookMark(Long lastArtWorkId,final Long accountId) {
        Pageable pageable = PageRequest.of(0,10);
        return artWorkRepository.findArtWorkBookMarkByAccountId(lastArtWorkId,pageable,accountId);
    }

    @Transactional(readOnly = true)
    public List<MyPost> getMyPost(Long accountId, String board,int start) {
        Pageable pageable = PageRequest.of(start,5);
        List<MyPost> myPosts = postRepository.findPostByAccountIdAndBoard(accountId, board, pageable);
        if (board.equals("QNA")) {
            setQnaInfo(myPosts);
        } else if (board.equals("INFO")) {
            setPostInfo(myPosts);
        }
        return myPosts;
    }

    @Transactional(readOnly = true)
    public List<MyPost> getMyBookMarkPost(Long accountId, String board,int start) {
        Pageable pageable = PageRequest.of(start,5);
        List<MyPost> myBookMarkPost = postRepository.findPostBookMarkByAccountId(accountId, board, pageable);
        setPostInfo(myBookMarkPost);
        return myBookMarkPost;
    }

    @Transactional(readOnly = true)
    public List<MyAnswer> getMyAnswer(Long accountId,int start) {
        Pageable pageable = PageRequest.of(start,5);
        return postAnswerRepository.findPostAnswerByAccountId(accountId, pageable);
    }

    @Transactional(readOnly = true)
    public List<MyComment> getMyComment(Long accountId, int start) {
        Pageable pageable= PageRequest.of(start, 5);
        return postCommentRepository.findPostCommentByAccountId(accountId, pageable);
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new ErrorCustomException(ErrorCode.NO_USER_ERROR));
    }

    private ArtWorks getArtWorks(Long artWorkId) {
        return artWorkRepository.findById(artWorkId).orElseThrow(() -> new ErrorCustomException(ErrorCode.NONEXISTENT_ERROR));
    }
    private void createValid(Account account, ArtWorks artWorks) {
        if(!account.getId().equals(artWorks.getAccount().getId())){
            throw new ErrorCustomException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }
    }

    private void setQnaInfo(List<MyPost> myPosts) {
        myPosts.forEach((myPost) -> {
            Long answerCount = postAnswerRepository.countByPostId(myPost.getPost_id());
            myPost.setAnswer_count(answerCount);
        });
    }

    private void setPostInfo(List<MyPost> myPosts) {
        myPosts.forEach((myPost) -> {
            Long commentCount = postCommentRepository.countByPostId(myPost.getPost_id());
            myPost.setAnswer_count(commentCount);
        });
    }

}





















