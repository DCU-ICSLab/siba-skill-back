package chatbot.api.textbox;

import chatbot.api.common.services.TimeService;
import chatbot.api.textbox.domain.*;
import chatbot.api.textbox.domain.path.Path;
import chatbot.api.textbox.domain.response.HrdwrControlResult;
import chatbot.api.textbox.repository.BuildRepository;
import chatbot.api.textbox.services.TextBoxResponseService;
import chatbot.api.common.domain.kakao.openbuilder.RequestDTO;
import chatbot.api.common.domain.kakao.openbuilder.responseVer2.ResponseVerTwoDTO;
import chatbot.api.common.services.KakaoSimpleTextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;

@RestController
@Slf4j
public class TextBoxController {

    @Autowired
    private TextBoxResponseService textBoxResponseService;

    @Autowired
    private KakaoSimpleTextService kakaoSimpleTextService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BuildRepository buildRepository;

    @Autowired
    private TimeService timeService;



    // from "시바" to hubs box
    @PostMapping("/textbox/hubs")
    public ResponseVerTwoDTO textBoxHubsFromUtterance(@RequestBody RequestDTO requestDto) {
        log.info("\n\n"); log.info("==================== from \"시바\" to hubs box 시작 ====================");
        log.info(requestDto.toString());
        log.info("INFO >> Utterance -> " + requestDto.getUserRequest().getUtterance());

        String providerId = requestDto.getUserRequest().getUser().getProperties().getAppUserId();
        return textBoxResponseService.responserHubsBox(providerId);
    }


    // from hubs box to hrdwrs box
    @PostMapping("/textbox/hrdwrs")
    public ResponseVerTwoDTO textBoxHrdwrsFromHubs(@RequestBody RequestDTO requestDto) {
        log.info("\n\n"); log.info("==================== from hubs box to hrdwrs box 시작 ====================");
        log.info(requestDto.toString());

        String providerId = requestDto.getUserRequest().getUser().getProperties().getAppUserId();
        int hubSeq = Integer.parseInt(requestDto.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));
        log.info("INFO >> Hub Seq -> " + hubSeq);

        return textBoxResponseService.responserHrdwrsBox(providerId, hubSeq);
    }


    // from hrdwrs box to entry box
    @PostMapping("/textbox/entry")
    public ResponseVerTwoDTO textBoxEntryFromHrdwrs(@RequestBody RequestDTO requestDto) {
        log.info("\n\n"); log.info("==================== from hrdwrs box to entry box 시작 ====================");
        log.info(requestDto.toString());

        String providerId = requestDto.getUserRequest().getUser().getProperties().getAppUserId();
        Integer hrdwrSeq = Integer.parseInt(requestDto.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));
        log.info("INFO >> Hrdwr Seq -> " + hrdwrSeq);

        return textBoxResponseService.responserEntryBox(providerId, hrdwrSeq);
    }


    // 1. (제어) 시나리오
    @PostMapping("/textbox/end/entry")
    public ResponseVerTwoDTO textBoxEndFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. 사용자가 선택한 버튼을 저장해야 한다
        //      1-1. 사용자가 선택한 버튼의 버튼 타입을 저장해야 한다
        //      1-2. 사용자가 선택한 버튼의 이벤트 코드를 저장해야 한다
        log.info("============== textBox (End_Entry) ==============");
        log.info(requestDTO.toString());

        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        Integer btnIdx = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));

        log.info("Block Id -> " + blockId);
        log.info("Button Index -> " + btnIdx);

        return textBoxResponseService.responserEndBoxFrEntry(providerId, btnIdx);
    }


    // 2. (제어->시간->동적) 시나리오
    @PostMapping("/textbox/dynamic/time/entry")
    public ResponseVerTwoDTO textBoxDynamicFrTimeFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. 사용자가 선택한 버튼을 저장해야 한다
        //      1-1. 사용자가 선택한 버튼의 버튼 타입을 저장해야 한다
        //      1-2. 사용자가 선택한 버튼의 이벤트 코드를 저장해야 한다
        // 2. timestamp 를 저장해야 한다

        log.info("\n\n"); log.info("============== textBox (Dynamic_Time_Entry) ==============");
        log.info(requestDTO.toString());

        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        Integer btnIdx = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));

        Object dateTimeParams = requestDTO.getAction().getParams().get("datetime");
        Timestamp timeStamp = timeService.convertTimeStampFromObject(dateTimeParams);

        log.info("Block Id -> " + blockId);
        log.info("Button Index -> " + btnIdx);
        log.info("DataTime -> " + dateTimeParams);
        log.info("TimeStamp -> " + timeStamp);

        // 사용자에게 보여줄 Dynamic Box 를 사용자에게 보여줘야 한다.
        return textBoxResponseService.responserDynamicBoxFrTimeFrEntry(providerId, btnIdx, timeStamp);
    }
    @PostMapping("/textbox/end/dynamic/time/entry")
    public ResponseVerTwoDTO textBoxEndFrDynamicFrTimeFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. 동적 입력 데이터를 Additonal 에 추가해야 한다
        // 2. 하위 박스가 있는지 없는지 체크한다
        //      2-1. 하위 박스가 없다면 -> End Box 라는 의미이니까, 사용자에게 명령을 전달할거냐고 묻는다
        //      2-2. 하위 박스가 있다면 -> End Box 가 아니라는 의미이니까, Entry Box 때 했던것과 비슷한 작업을 수행한다

        log.info("============== textBox (End_Dynamic_Time_Entry) ==============");
        log.info(requestDTO.toString());

        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        Integer dynamicValue = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));

        log.info("Block Id -> " + blockId);
        log.info("INFO >> 사용자가 입력한 dynamicValue -> " + dynamicValue);
        return textBoxResponseService.responserEndBoxFrDynamicFrTimeFrEntry(providerId, dynamicValue);
    }


    // 3. (제어->동적->시간) 시나리오
    @PostMapping("/textbox/_dynamic/entry")
    public ResponseVerTwoDTO textBox_DynamicFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. 사용자가 선택한 버튼을 저장해야 한다
        //      1-1. 사용자가 선택한 버튼의 버튼 타입을 저장해야 한다
        //      1-2. 사용자가 선택한 버튼의 이벤트 코드를 저장해야 한다
        // 2. 동적 입력 박스를 사용자에게 보여줘야 한다.

        log.info("============== textBox (_Dynamic_Entry) ==============");
        log.info(requestDTO.toString());

        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        Integer btnIdx = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));

        log.info("Block Id -> " + blockId);
        return textBoxResponseService.responserUDynamicBoxFrEntry(providerId, btnIdx); // U == underbar
    }
    @PostMapping("/textbox/end/time/_dynamic/entry")
    public ResponseVerTwoDTO textBoxEndFrTimeFr_DynamicFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. timestamp 를 저장해야 한다
        // 2. 동적 입력 데이터를 저장해야 한다
        // 3. 하위 박스가 있는지 없는지 체크한다
        //      3-1. 하위 박스가 없다면 -> End Box 라는 의미이니까, 사용자에게 명령을 전달할거냐고 묻는다
        //      3-2. 하위 박스가 있다면 -> End Box 가 아니라는 의미이니까, Entry Box 때 했던것과 비슷한 작업을 수행한다

        log.info("============== textBox (End_Time__Dynamic_Entry) ==============");
        log.info(requestDTO.toString());

        String blockId = requestDTO.getUserRequest().getBlock().getId();
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        Integer dynamicValue = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));
        Object dateTimeParams = requestDTO.getAction().getParams().get("datetime");
        Timestamp timeStamp = timeService.convertTimeStampFromObject(dateTimeParams);

        log.info("Block Id -> " + blockId);
        log.info("INFO >> 사용자가 입력한 dynamicValue -> " + dynamicValue);
        log.info("DataTime -> " + dateTimeParams);
        log.info("TimeStamp -> " + timeStamp);

        return textBoxResponseService.responserEndBoxFrTimeFrUDynamicFrEntry(providerId, dynamicValue, timeStamp);
    }


    // 4. (제어->시간) 시나리오
    @PostMapping("/textbox/end/time/entry")
    public ResponseVerTwoDTO textBoxEndFrTimeFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. 사용자가 선택한 버튼을 저장해야 한다
        //      1-1. 사용자가 선택한 버튼의 버튼 타입을 저장해야 한다
        //      1-2. 사용자가 선택한 버튼의 이벤트 코드를 저장해야 한다
        // 2. timestamp 를 저장해야 한다
        // 3. 하위 박스가 있는지 없는지 체크한다
        //      3-1. 하위 박스가 없다면 -> End Box 라는 의미이니까, 사용자에게 명령을 전달할거냐고 묻는다
        //      3-2. 하위 박스가 있다면 -> End Box 가 아니라는 의미이니까, Entry Box 때 했던것과 비슷한 작업을 수행한다

        log.info("============== textBox (End_Time_Entry) ==============");
        log.info(requestDTO.toString());

        String blockId = requestDTO.getUserRequest().getBlock().getId();
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        Integer btnIdx = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));
        Object dateTimeParams = requestDTO.getAction().getParams().get("datetime");
        Timestamp timeStamp = timeService.convertTimeStampFromObject(dateTimeParams);

        log.info("Block Id -> " + blockId);
        log.info("Button Index -> " + btnIdx);
        log.info("DataTime -> " + dateTimeParams);
        log.info("TimeStamp -> " + timeStamp);

        return textBoxResponseService.responserEndBoxFrTimeFrEntry(providerId, btnIdx, timeStamp);
    }


    // 5. (제어->동적) 시나리오
    @PostMapping("/textbox/dynamic/entry")
    public ResponseVerTwoDTO textBoxDynamicFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. 사용자가 선택한 버튼을 저장해야 한다
        //      1-1. 사용자가 선택한 버튼의 버튼 타입을 저장해야 한다
        //      1-2. 사용자가 선택한 버튼의 이벤트 코드를 저장해야 한다
        // 2. Cur Box 를 조정해야 한다

        log.info("============== textBox (Dynamic_Entry) ==============");
        log.info(requestDTO.toString());

        String blockId = requestDTO.getUserRequest().getBlock().getId();
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        Integer btnIdx = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));

        log.info("Block Id -> " + blockId);
        log.info("Button Index -> " + btnIdx);

        return textBoxResponseService.responserDynamicBoxFrEntry(providerId, btnIdx);
    }
    @PostMapping("/textbox/end/dynamic/entry")
    public ResponseVerTwoDTO textBoxEndFrDynamicFrEntry(@RequestBody RequestDTO requestDTO) {
        // 1. 동적 입력 데이터를 저장해야 한다
        // 2. curBox 를 조정해야 한다
        // 3. 하위 박스가 있는지 없는지 체크한다
        //      3-1. 하위 박스가 없다면 -> End Box 라는 의미이니까, 사용자에게 명령을 전달할거냐고 묻는다
        //      3-2. 하위 박스가 있다면 -> End Box 가 아니라는 의미이니까, Entry Box 때 했던것과 비슷한 작업을 수행한다

        log.info("============== textBox (End_Dynamic_Entry) ==============");
        log.info(requestDTO.toString());

        String blockId = requestDTO.getUserRequest().getBlock().getId();
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        Integer dynamicValue = Integer.parseInt(requestDTO.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));

        log.info("Block Id -> " + blockId);
        log.info("INFO >> 사용자가 입력한 dynamicValue -> " + dynamicValue);

        return textBoxResponseService.responserEndBoxFrDynamicFrEntry(providerId, dynamicValue);
    }





    // 블록 아이디 : BLOCK_ID_TRANSFER_RESULT,
    // "예" 혹은 "아니오" 버튼을 클릭하면 아래 메소드 호출
    @PostMapping("/complete/builded/codes")
    public ResponseVerTwoDTO transferCompleteBuildedCodes(@RequestBody RequestDTO requestDto) {

        log.info("==================== Transfer Complete Builded Codes 시작 ====================");
        log.info("사용자가 선택한 버튼이 \"예\" 버튼 이여서, 빌드된 코드를 전송할 때 호출되는 메소드 입니다.");
        log.info(requestDto.toString());

        String providerId = requestDto.getUserRequest().getUser().getProperties().getAppUserId();
        String utterance = requestDto.getUserRequest().getUtterance();
        Build reBuild = buildRepository.find(providerId);

        if(utterance.equals("명령 전송")) {
            Path path = reBuild.getPath();

            log.info("INFO >> PATH -> " + path.toString());

            String externalIp = path.getExternalIp();
            int externalPort = path.getExternalPort();
            String hrdwrMacAddr = path.getHrdwrMacAddr();

            //ArrayList<CmdList> btns = reBuild.getCmdLists();
            //CmdList[] arrBtns = btns.toArray(new CmdList[btns.size()]);
            log.info("INFO >> Builed Code 목록 -------");
            //for(CmdList temp : arrBtns) {
            //    log.info("- " + temp.toString());
            //}

            String url = new String("http://" + externalIp + ":" + externalPort + "/dev/" + hrdwrMacAddr);
            log.info("INFO >> URL -> " + url);


            // 밑에 임시 주석
            HrdwrControlResult result = restTemplate.postForObject(url, new Object(){
                    public String requester_id = reBuild.getHProviderId();
             //       public CmdList[] cmd = arrBtns;
                }, HrdwrControlResult.class);
            //HrdwrControlResult result = new HrdwrControlResult();
            result.setStatus(true);
            log.info("INFO >> Result 결과 -> " + result.toString());

            buildRepository.delete(providerId);

            StringBuffer msg = new StringBuffer("");
            if(result.isStatus() == true) {
                msg.append("명령 전송이 성공적으로 수행 되었습니다.\n\n");
            } else if (result.isStatus() == false) {
                msg.append("명령 전송이 실패하였습니다.\n\n");
            }
            msg.append("또 다른 명령을 수행하고 싶으시다면 아래 슬롯을 올려 \"시바\" 버튼을 누르세요.");
            return kakaoSimpleTextService.makerTransferCompleteCard(msg.toString());
        }

        // 사용자가 취소 명령을 눌렀다면
        // 취소 선택 버튼 3가지 경우의 수를 리턴한다.
        return kakaoSimpleTextService.makerCancleSelectCard();
    }


    // 블록 아이디 : BLOCK_ID_BUILDED_CODES,
    // "전송" 버튼을 클릭하면 이 메소드가 호출
    @PostMapping("/builed/codes")
    public ResponseVerTwoDTO transferCodes(@RequestBody RequestDTO requestDto) {

        log.info("============ Transfer Codes 시작 ============");
        log.info("중간에 \"전송\" 버튼을 눌렀을때, 빌딩된 명령을 전송하는 메소드");
        log.info(requestDto.toString());

        return kakaoSimpleTextService.makerTransferSelectCard();
    }
}
/*
    // 이쪽으로 오는거 필요 없을수도 있음
    @PostMapping("/textbox/lookup/reservation")
    public ResponseVerTwoDTO textBoxLookUpReservationFromAny(@RequestBody RequestDTO requestDTO) {
        log.info("\n\n==================== from LookUp Reservation box to control box 시작 ====================");
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        log.info("Block Id -> " + blockId);
        log.info(requestDTO.toString());
        return null;
    }
    // 이쪽으로 오는거 필요 없을수도 있음
    @PostMapping("/textbox/lookup/sensing")
    public ResponseVerTwoDTO textBoxLookUpSensingFromAny(@RequestBody RequestDTO requestDTO) {
        log.info("\n\n==================== from LookUp Sensing box to control box 시작 ====================");
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        log.info("Block Id -> " + blockId);
        log.info(requestDTO.toString());
        return null;
    }
    // 이쪽으로 오는거 필요 없을수도 있음
    @PostMapping("/textbox/lookup/device")
    public ResponseVerTwoDTO textBoxLookUpDeviceFromAny(@RequestBody RequestDTO requestDTO) {
        log.info("\n\n==================== from LookUp Device box to control box 시작 ====================");
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        log.info("Block Id -> " + blockId);
        log.info(requestDTO.toString());
        return null;
    }
    // 이쪽으로 오는거 필요 없을수도 있음
    @PostMapping("/textbox/reservation")
    public ResponseVerTwoDTO textBoxOnlyReservationFromAny(@RequestBody RequestDTO requestDTO) {
        log.info("\n\n==================== from only reservation box to control box 시작 ====================");
        String providerId = requestDTO.getUserRequest().getUser().getProperties().getAppUserId();
        String blockId = requestDTO.getUserRequest().getBlock().getId();
        log.info("Block Id -> " + blockId);
        log.info(requestDTO.toString());
        return null;
    }
    */


/*
    // from entry box to any box
    // entry 박스에서 선택한 버튼의 타입이 무엇인지에 따라서 5개의 시나리오로 나뉘기 때문에 Any Box라고 명명함
    @PostMapping("/textbox/any")
    public ResponseVerTwoDTO textBoxAnyFromEntry(@RequestBody RequestDTO requestDto) {
        log.info("\n\n"); log.info("==================== from entry box to any box 시작 ====================");
        log.info("entry 박스에서 선택한 버튼의 타입이 무엇인지에 따라서 5개의 시나리오로 나뉘기 때문에 Any Box라고 명명함");
        log.info("Any Box를 사용자에게 보여줌");
        log.info("Any Box는 (제어 / 조회-허브-예약 / 조회-허브-센싱 / 조회-디바이스 / 예약) 5가지 중 하나임");
        log.info("시나리오 1 : BUTTON_TYPE_CONTROL");
        log.info("시나리오 2 : BUTTON_TYPE_LOOKUP_RESERVATION");
        log.info("시나리오 3 : BUTTON_TYPE_LOOKUP_SENSING");
        log.info("시나리오 4 : BUTTON_TYPE_LOOKUP_DEVICE");
        log.info("시나리오 5 : BUTTON_TYPE_ONLY_RESERVATION");
        log.info(requestDto.toString());
        String providerId = requestDto.getUserRequest().getUser().getProperties().getAppUserId();
        Integer btnIdx = Integer.parseInt(requestDto.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));
        log.info("INFO >> Btn Idx -> " + btnIdx);
        //return textBoxResponseService.responserBtnBox(providerId, btnSeq);
        return textBoxResponseService.responserAnyBoxFromEntry(providerId, btnIdx);
    }*/

/*
    // from control + time
    // from dynamic_context + time
    // to end
    // to control
    // to dynamic
    // 이 메소드는 control + time 경로를 통해서 오든가 dynamic_context + time 으로 올 수도 있다.
    @PostMapping("/textbox/time")
    public ResponseVerTwoDTO textBoxAnyFromTime(@RequestBody RequestDTO requestDto) {
        log.info("==================== from Anys({control + time} or {dynamic_context + time}) to Any(end or dynamic or control) 시작 ====================");
        log.info(requestDto.toString());
        String providerId = requestDto.getUserRequest().getUser().getProperties().getAppUserId();
        Integer btnSeq = Integer.parseInt(requestDto.getUserRequest().getUtterance().replaceAll("[^0-9]", ""));
        Object datetime = requestDto.getAction().getParams().get("datetime");
        log.info("INFO >> Btn Seq -> " + btnSeq);
        log.info("INFO >> Params -> " + datetime);
        //return textBoxResponseService.responserBtnBoxFromTime(providerId, btnSeq, minute);
        return null;
    }
*/