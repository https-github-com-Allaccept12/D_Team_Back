package TeamDPlus.code.dto.request;
import TeamDPlus.code.dto.common.CommonDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

public class ArtWorkRequestDto {


    @Getter
    @NoArgsConstructor
    public static class ArtWorkCreateAndUpdate {
        private String scope;

        private String title;

        private String content;

        private List<CommonDto.ImgUrlDto> img;

        private String category;

        private Timestamp work_start;

        private Timestamp work_end;

        private boolean is_master;

    }

    @Getter
    @NoArgsConstructor
    public static class ArtWorkComment {

        private String content;

    }


}
