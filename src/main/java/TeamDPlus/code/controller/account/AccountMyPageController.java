package TeamDPlus.code.controller.account;


import TeamDPlus.code.dto.Success;
import TeamDPlus.code.dto.request.AccountRequestDto.AccountVisit;
import TeamDPlus.code.dto.request.AccountRequestDto.UpdateAccountIntro;
import TeamDPlus.code.dto.request.AccountRequestDto.UpdateSpecialty;
import TeamDPlus.code.dto.request.ArtWorkRequestDto.ArtWorkPortFolioUpdate;
import TeamDPlus.code.dto.request.HistoryRequestDto.HistoryUpdateList;
import TeamDPlus.code.jwt.UserDetailsImpl;
import TeamDPlus.code.service.account.AccountMyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-page")
public class AccountMyPageController {

    private final AccountMyPageService accountMyPageService;


    @GetMapping("")
    public ResponseEntity<Success> accountInfo(@RequestBody AccountVisit accountId,
                                               @AuthenticationPrincipal UserDetailsImpl user) {
        return new ResponseEntity<>(new Success("마이페이지 기본정보 조회",
                accountMyPageService.showAccountInfo(accountId.getAccount_id(), user.getUser().getId())), HttpStatus.OK);
    }

    @GetMapping("/career-feed/{last_artwork_id}")
    public ResponseEntity<Success> accountCareerFeed(@RequestBody AccountVisit accountId,
                                                     @PathVariable String last_artwork_id,
                                                     @AuthenticationPrincipal UserDetailsImpl user) {
        return new ResponseEntity<>(new Success("마이페이지 커리어피드 조회",
                accountMyPageService.showAccountCareerFeed(Long.parseLong(last_artwork_id), accountId.getAccount_id(), user.getUser().getId())), HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<Success> accountHistory(@RequestBody AccountVisit accountId,
                                                  @AuthenticationPrincipal UserDetailsImpl user) {
        return new ResponseEntity<>(new Success("마이페이지 연혁 조회",
                accountMyPageService.showAccountHistory(accountId.getAccount_id())), HttpStatus.OK);
    }

    @RequestMapping(value = "/history", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<Success> accountHistoryUpdate(@RequestBody HistoryUpdateList data,
                                                        @AuthenticationPrincipal UserDetailsImpl user) {
        accountMyPageService.updateAccountHistory(data,user.getUser().getId());
        return new ResponseEntity<>(new Success("연혁 수정",""),HttpStatus.OK);
    }

    @GetMapping("/artwork/{last_artwork_id}")
    public ResponseEntity<Success> accountArtWorkList(@PathVariable Long last_artwork_id,
                                                      @RequestBody AccountVisit accountId,
                                                      @AuthenticationPrincipal UserDetailsImpl user) {
        return new ResponseEntity<>(new Success("유저 작품 목록",
                accountMyPageService.showAccountArtWork(last_artwork_id,accountId.getAccount_id(),user.getUser().getId())),HttpStatus.OK);
    }

    @RequestMapping(value = "/intro", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<Success> accountIntro(@RequestBody UpdateAccountIntro data,
                                                @AuthenticationPrincipal UserDetailsImpl user) {
        accountMyPageService.updateAccountIntro(data,user.getUser().getId());
        return new ResponseEntity<>(new Success("유저 소개 수정",""),HttpStatus.OK);
    }

    @RequestMapping(value = "/specialty", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<Success> accountSpecialty(@RequestBody UpdateSpecialty data,
                                                    @AuthenticationPrincipal UserDetailsImpl user) {
        accountMyPageService.updateAccountSpecialty(data,user.getUser().getId());
        return new ResponseEntity<>(new Success("스킬셋 수정",""),HttpStatus.OK);
    }
    //내 북마크
    @GetMapping("/bookmark/{last_artwork_id}")
    public ResponseEntity<Success> ArtWorkBookmarkList(@PathVariable Long last_artwork_id,
                                                       @AuthenticationPrincipal UserDetailsImpl user) {
        return new ResponseEntity<>(new Success("작품 북마크 목록",
                accountMyPageService.showAccountArtWorkBookMark(last_artwork_id,user.getUser().getId())),HttpStatus.OK);
    }
    //내 디모


    //다건
    @PostMapping(value = {"/career-feed"})
    public ResponseEntity<Success> createAndUpdateCareerFeed(@RequestBody ArtWorkPortFolioUpdate data) {
        accountMyPageService.updateAccountCareerFeedList(data);
        return new ResponseEntity<>(new Success("포트폴리오 선택/수정 성공", ""), HttpStatus.OK);
    }
    //단건
    @PostMapping(value = "/masterpiece/{artwork_id}")
    public ResponseEntity<Success> masterpieceSelect(@PathVariable Long artwork_id,
                                                     @AuthenticationPrincipal UserDetailsImpl user) {
        accountMyPageService.masterAccountCareerFeed(artwork_id,user.getUser());
        return new ResponseEntity<>(new Success("포트폴리오 작품 선택",""),HttpStatus.OK);
    }

    @PatchMapping(value = "/masterpiece/{artwork_id}")
    public ResponseEntity<Success> masterpieceClear(@PathVariable Long artwork_id,
                                                     @AuthenticationPrincipal UserDetailsImpl user) {
        accountMyPageService.nonMasterAccountCareerFeed(artwork_id,user.getUser());
        return new ResponseEntity<>(new Success("포트폴리오 작품 해지",""),HttpStatus.OK);
    }

    //보이기
    @PostMapping(value = "/hidepiece/{artwork_id}")
    public ResponseEntity<Success> hidepieceSelect(@PathVariable Long artwork_id,
                                                   @AuthenticationPrincipal UserDetailsImpl user) {
        accountMyPageService.nonHideArtWorkScope(artwork_id,user.getUser());
        return new ResponseEntity<>(new Success("작품 보이기",""),HttpStatus.OK);
    }
    //숨기기
    @PatchMapping(value = "/hidepiece/{artwork_id}")
    public ResponseEntity<Success> hidepieceClear(@PathVariable Long artwork_id,
                                                   @AuthenticationPrincipal UserDetailsImpl user) {
        accountMyPageService.hideArtWorkScope(artwork_id,user.getUser());
        return new ResponseEntity<>(new Success("작품 숨기기",""),HttpStatus.OK);
    }


}
