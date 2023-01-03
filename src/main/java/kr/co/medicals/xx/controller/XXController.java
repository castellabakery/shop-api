package kr.co.medicals.xx.controller;

import kr.co.medicals.xx.service.XXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/buyer/order")
public class XXController {

    private final XXService XXService;

    @Autowired
    public XXController(XXService XXService) {
        this.XXService = XXService;
    }

    @ResponseBody
    @RequestMapping("/checkHash")
    public Object AuthCheckHash(@RequestParam("buyReqamt") String buyReqamt, @RequestParam("buyItemnm") String buyItemnm, @RequestParam("kindstype") String kinds, @RequestParam("orderno") String orderno) {
        return XXService.CTFCcheckHash(buyReqamt, buyItemnm, orderno);
    }

}
