package TeamDPlus.code.service.post.answer;

import TeamDPlus.code.advice.ApiRequestException;
import TeamDPlus.code.advice.ErrorCode;
import TeamDPlus.code.domain.account.Account;
import TeamDPlus.code.domain.account.AccountRepository;
import TeamDPlus.code.domain.post.Post;
import TeamDPlus.code.domain.post.PostRepository;
import TeamDPlus.code.domain.post.answer.PostAnswer;
import TeamDPlus.code.domain.post.answer.PostAnswerRepository;
import TeamDPlus.code.dto.request.PostRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PostAnswerService {

    private final PostRepository postRepository;
    private final PostAnswerRepository postAnswerRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Long createAnswer(PostRequestDto.PostAnswer dto, Long postId, Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new ApiRequestException(ErrorCode.NO_USER_ERROR));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ApiRequestException(ErrorCode.NONEXISTENT_ERROR));
        PostAnswer postAnswer = PostAnswer.builder().post(post).account(account).content(dto.getContent()).build();
        PostAnswer save = postAnswerRepository.save(postAnswer);
        account.updateExp(3);
        return save.getId();
    }

    @Transactional
    public Long updateAnswer(PostRequestDto.PostAnswer dto, Long answerId, Long accountId) {
        PostAnswer postAnswer = postAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ApiRequestException(ErrorCode.NONEXISTENT_ERROR));

        if (!postAnswer.getAccount().getId().equals(accountId)) {
            throw new ApiRequestException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }

        postAnswer.updateAnswer(dto.getContent());
        return postAnswer.getId();
    }

    @Transactional
    public void deleteAnswer(Long answerId, Long accountId) {
        PostAnswer postAnswer = postAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ApiRequestException(ErrorCode.NONEXISTENT_ERROR));

        if (!postAnswer.getAccount().getId().equals(accountId)) {
            throw new ApiRequestException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }

        postAnswerRepository.deleteById(answerId);
    }

    @Transactional
    public void doIsSelected(Long postAnswerId, Long accountId) {
        PostAnswer postAnswer = postAnswerRepository.findById(postAnswerId)
                .orElseThrow(() -> new ApiRequestException(ErrorCode.NONEXISTENT_ERROR));

        if (!postAnswer.getPost().getAccount().getId().equals(accountId)) {
            throw new ApiRequestException(ErrorCode.NO_AUTHORIZATION_ERROR);
        }

        if (postAnswer.isSelected()) {
            throw new ApiRequestException(ErrorCode.ALREADY_SELECTED_ERROR);
        }

        postAnswer.doIsSelected(true);
        postAnswer.getAccount().updateExp(20);

        if (!postAnswer.getPost().isSelected()) {
            postAnswer.getPost().doIsSelected(true);
        }
    }

}
